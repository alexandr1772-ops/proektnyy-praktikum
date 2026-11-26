package ru.attendance.tracker.api

import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.concurrent.thread
import java.util.concurrent.LinkedBlockingQueue
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.attendance.tracker.data.AttendanceStatus
import ru.attendance.tracker.data.Change
import ru.attendance.tracker.data.DemoData
import ru.attendance.tracker.data.JournalException
import ru.attendance.tracker.data.JournalJson
import ru.attendance.tracker.data.Mark
import ru.attendance.tracker.data.Role
import ru.attendance.tracker.data.Group

class ApiAttendanceRepositoryTest {
    @Test
    fun exchangesLoginAndJournalAndFiltersPersonalView() = runBlocking {
        val demo = DemoData.initialJournal()
        withServer(
            Reply(200, """{"access_token":"mock-token","user":{"login":"student","role":"student","student_id":1}}"""),
            Reply(200, JournalJson.write(demo).toString()),
        ) { repository, calls ->
            val account = repository.login("student", "password")
            assertEquals(Role.Student, account.role)
            val data = repository.load(account)
            assertEquals(listOf(1), data.students.map { it.id })
            assertTrue(data.entries.all { it.mark.studentId == 1 })
            val login = calls.remove()
            assertEquals("POST /auth/login", login.methodAndPath)
            assertEquals("student", JSONObject(login.body).getString("login"))
            val journal = calls.remove()
            assertEquals("GET /journal", journal.methodAndPath)
            assertEquals("Bearer mock-token", journal.authorization)
        }
    }

    @Test
    fun usesOneBulkRequestAndASeparatePointUpdateAndSurfacesConflict() = runBlocking {
        withServer(Reply(204), Reply(204), Reply(409, """{"detail":"Отметка уже есть"}""")) { repository, calls ->
            val account = DemoData.accounts.first().copy(token = "token")
            val marks = listOf(Mark(1, AttendanceStatus.Present), Mark(2, AttendanceStatus.Late, "10 мин"))
            repository.change(account, Change.SaveMarks(2, marks, bulk = true))
            repository.change(account, Change.SaveMarks(2, listOf(marks.last()), bulk = false))
            val rejection = runCatching { repository.change(account, Change.SaveMarks(2, marks, bulk = true)) }.exceptionOrNull()
            assertTrue(rejection is JournalException)
            assertEquals("Отметка уже есть", rejection?.message)

            val batch = calls.remove()
            assertEquals("POST /lessons/2/attendance/bulk", batch.methodAndPath)
            assertEquals(2, JSONObject(batch.body).getJSONArray("marks").length())
            val point = calls.remove()
            assertEquals("PUT /lessons/2/attendance/2", point.methodAndPath)
            assertEquals("late", JSONObject(point.body).getString("status"))
            assertEquals("10 мин", JSONObject(point.body).getString("note"))
            assertEquals(batch.methodAndPath, calls.remove().methodAndPath)
            assertTrue(calls.isEmpty())
        }
    }

    @Test
    fun exchangesCreateImportAndExportAndConservesUtf8() = runBlocking {
        val csv = "full_name\nНовый Студент\n"
        withServer(Reply(201, "{}"), Reply(200, """{"message":"Импортировано: 1"}"""), Reply(200, csv)) { repository, calls ->
            val account = DemoData.accounts.first()
            repository.change(account, Change.SaveGroup(Group(name = "Новая группа")))
            assertEquals("Импортировано: 1", repository.change(account, Change.ImportStudents(1, csv)))
            assertEquals(csv, repository.export(account, 1))
            val created = calls.remove()
            assertEquals("POST /groups", created.methodAndPath)
            assertFalse(JSONObject(created.body).has("id"))
            val imported = calls.remove()
            assertEquals("POST /groups/1/students/import", imported.methodAndPath)
            assertEquals(csv, JSONObject(imported.body).getString("csv"))
            val exported = calls.remove()
            assertEquals("GET /groups/1/attendance.csv", exported.methodAndPath)
            assertEquals("text/csv", exported.accept)
        }
    }

    @Test
    fun roundTripsTheLocalJournalIncludingIdentifiersAndNotes() {
        val demo = DemoData.initialJournal().copy(nextId = 300)
        assertEquals(demo, JournalJson.read(JSONObject(JournalJson.write(demo).toString())))
    }

    private suspend fun withServer(
        vararg replies: Reply,
        test: suspend (ApiAttendanceRepository, LinkedBlockingQueue<Call>) -> Unit,
    ) {
        val responses = LinkedBlockingQueue(replies.toList())
        val calls = LinkedBlockingQueue<Call>()
        ServerSocket(0, 10, InetAddress.getByName("127.0.0.1")).use { server ->
            val worker = thread(isDaemon = true, name = "attendance-http-test") {
                while (responses.isNotEmpty()) {
                    val client = runCatching { server.accept() }.getOrNull() ?: break
                    client.use {
                        it.soTimeout = 5000
                        val input = BufferedInputStream(it.getInputStream())
                        val request = input.httpLine().split(' ')
                        val headers = mutableMapOf<String, String>()
                        var line = input.httpLine()
                        while (line.isNotEmpty()) {
                            headers[line.substringBefore(':').lowercase()] = line.substringAfter(':').trim()
                            line = input.httpLine()
                        }
                        val length = headers["content-length"]?.toInt() ?: 0
                        val body = input.readNBytes(length).toString(Charsets.UTF_8)
                        calls.add(Call("${request[0]} ${request[1]}", body, headers["authorization"], headers["accept"]))
                        val response = responses.remove()
                        val bytes = response.body.toByteArray(Charsets.UTF_8)
                        val header = "HTTP/1.1 ${response.status} Test\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n"
                        it.getOutputStream().apply {
                            write(header.toByteArray(Charsets.US_ASCII))
                            write(bytes)
                            flush()
                        }
                    }
                }
            }
            try {
                test(ApiAttendanceRepository("http://127.0.0.1:${server.localPort}", allowHttp = true), calls)
                assertTrue(responses.isEmpty())
            } finally {
                server.close()
                worker.join(6000)
            }
        }
    }

    private fun BufferedInputStream.httpLine(): String {
        val line = ByteArrayOutputStream()
        while (true) {
            val value = read()
            if (value < 0 || value == '\n'.code) break
            if (value != '\r'.code) line.write(value)
        }
        return line.toString(Charsets.US_ASCII.name())
    }

    private data class Reply(val status: Int, val body: String = "")
    private data class Call(val methodAndPath: String, val body: String, val authorization: String?, val accept: String?)
}
