package ru.attendance.tracker.api

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import ru.attendance.tracker.data.Account
import ru.attendance.tracker.data.AttendanceRepository
import ru.attendance.tracker.data.Change
import ru.attendance.tracker.data.Entity
import ru.attendance.tracker.data.Journal
import ru.attendance.tracker.data.JournalException
import ru.attendance.tracker.data.JournalJson
import ru.attendance.tracker.data.enumValue
import ru.attendance.tracker.data.jsonObject
import ru.attendance.tracker.data.optionalString

/**
 * Клиент рабочей версии контракта из README. При подключении командного API
 * сопоставление URL, тел запросов и ответов меняется здесь и в JournalJson.
 */
internal class ApiAttendanceRepository(baseUrl: String, private val allowHttp: Boolean) : AttendanceRepository {
    override val label = "API"
    private val baseUrl = baseUrl.trim().trimEnd('/')

    override suspend fun login(login: String, password: String): Account {
        val reply = JSONObject(request("POST", "/auth/login", body = jsonObject("login" to login, "password" to password)))
        val user = reply.getJSONObject("user")
        val groups = user.optJSONArray("group_ids")
        return Account(
            user.getString("login"),
            enumValue(user.getString("role")),
            groups?.let { ids -> List(ids.length(), ids::getInt).toSet() }.orEmpty(),
            if (user.isNull("student_id")) null else user.getInt("student_id"),
            reply.getString("access_token").takeIf { it.isNotBlank() } ?: throw JournalException("Сервер не вернул токен"),
        )
    }

    override suspend fun load(account: Account): Journal =
        JournalJson.read(JSONObject(request("GET", "/journal", account))).visibleTo(account)

    override suspend fun change(account: Account, change: Change): String = when (change) {
        is Change.SaveGroup -> save(account, Entity.Groups, change.value.id, JournalJson.group(change.value))
        is Change.SaveStudent -> save(account, Entity.Students, change.value.id, JournalJson.student(change.value))
        is Change.SaveSubject -> save(account, Entity.Subjects, change.value.id, JournalJson.subject(change.value))
        is Change.SaveLesson -> save(account, Entity.Lessons, change.value.id, JournalJson.lesson(change.value))
        is Change.Delete -> {
            request("DELETE", "/${change.entity.path}/${change.id}", account)
            "Удалено"
        }
        is Change.SaveMarks -> {
            val path = "/lessons/${change.lessonId}/attendance"
            if (change.bulk) {
                val marks = JSONArray(change.marks.map(JournalJson::mark))
                request("POST", "$path/bulk", account, jsonObject("marks" to marks))
            } else {
                val mark = change.marks.single()
                request("PUT", "$path/${mark.studentId}", account, JournalJson.mark(mark))
            }
            "Отметки сохранены"
        }
        is Change.ImportStudents -> {
            val body = jsonObject("csv" to change.csv)
            val reply = request("POST", "/groups/${change.groupId}/students/import", account, body)
            if (reply.isBlank()) "Импорт завершён"
            else JSONObject(reply).optionalString("message") ?: "Импорт завершён"
        }
    }

    override suspend fun export(account: Account, groupId: Int): String =
        request("GET", "/groups/$groupId/attendance.csv", account, accept = "text/csv")

    private suspend fun save(account: Account, entity: Entity, id: Int, body: JSONObject): String {
        if (id == 0) {
            body.remove("id")
            request("POST", "/${entity.path}", account, body)
        } else {
            request("PUT", "/${entity.path}/$id", account, body)
        }
        return "Сохранено"
    }

    private suspend fun request(
        method: String,
        path: String,
        account: Account? = null,
        body: JSONObject? = null,
        accept: String = "application/json",
    ): String = withContext(Dispatchers.IO) {
        val url = runCatching { URL(baseUrl + path) }.getOrElse { throw JournalException("Проверьте адрес API") }
        if (url.host.isBlank() || !(url.protocol == "https" || (allowHttp && url.protocol == "http"))) {
            throw JournalException("Проверьте адрес API. Для обычной сборки нужен HTTPS")
        }
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 5_000
            connection.readTimeout = 10_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Accept", accept)
            account?.token?.let { connection.setRequestProperty("Authorization", "Bearer $it") }
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val code = connection.responseCode
            if (code !in 200..299) {
                val error = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                val detail = error?.let {
                    runCatching { JSONObject(it).opt("detail") as? String }.getOrNull()?.takeIf(String::isNotBlank)
                }
                throw JournalException(detail ?: when (code) {
                    401 -> if (account == null) "Неверный логин или пароль" else "Сеанс истёк. Войдите снова"
                    403 -> "Недостаточно прав"
                    404 -> "API не нашёл ресурс"
                    409 -> "Запись уже существует или связана с другими данными"
                    422 -> "Сервер отклонил данные. Проверьте поля формы"
                    else -> "Сервер вернул ошибку ($code)"
                })
            }
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (error: IOException) {
            throw JournalException("Не удалось связаться с сервером. Проверьте адрес и подключение")
        } finally {
            connection.disconnect()
        }
    }
}
