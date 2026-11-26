package ru.attendance.tracker.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class AttendanceStatus(val label: String) {
    Present("Присутствует"), Absent("Отсутствует"), Late("Опоздал"), Excused("Уважительная"),
}
enum class Risk { High, Medium, Low }
enum class SubjectKind { Database, Programming, Math, Other }

data class Student(
    val id: Int,
    val fullName: String,
    val percent: Int?,
    val risk: Risk? = null,
    val riskNote: String? = null,
) {
    private val nameParts get() = fullName.split(Regex("\\s+"))
    val initials: String get() = nameParts.take(2).joinToString("") { it.take(1) }
    val shortName: String get() = (nameParts.take(1) + nameParts.drop(1).map { it.take(1) + "." }).joinToString(" ")
}

data class Subject(val name: String, val kind: SubjectKind, val percent: Int?)
data class Lesson(
    val id: Int,
    val subject: Subject,
    val group: String,
    val start: String,
    val end: String,
    val pair: Int,
    val date: LocalDate,
)

fun dateText(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru")))

internal object DemoData {
    const val password = "1234"
    val accounts = listOf(
        Account("admin", Role.Admin),
        Account("teacher", Role.Teacher, setOf(1)),
        Account("student", Role.Student, studentId = 1),
    )

    fun initialJournal(today: LocalDate = LocalDate.now()): Journal {
        val groups = listOf(Group(1, "ИС-21"), Group(2, "ИС-22"))
        val students = listOf(
            StudentRecord(1, "Иванов Иван Иванович", 1),
            StudentRecord(2, "Петров Пётр Сергеевич", 1),
            StudentRecord(3, "Смирнова Анна Олеговна", 1),
            StudentRecord(4, "Кузнецова Мария Андреевна", 1),
            StudentRecord(5, "Соколова Дарья Игоревна", 2),
            StudentRecord(6, "Белов Александр Сергеевич", 2),
        )
        val subjects = listOf(
            SubjectRecord(1, "Базы данных", SubjectKind.Database),
            SubjectRecord(2, "Программирование", SubjectKind.Programming),
            SubjectRecord(3, "Математика", SubjectKind.Math),
        )
        val lessons = listOf(
            LessonRecord(1, 1, 1, today.minusDays(1), "08:30", "10:00", 1),
            LessonRecord(2, 2, 1, today, "10:10", "11:40", 2),
            LessonRecord(3, 3, 2, today, "12:10", "13:40", 3),
            LessonRecord(4, 2, 1, today.minusWeeks(1), "10:10", "11:40", 2),
        )
        val history = listOf(
            JournalEntry(1, Mark(1, AttendanceStatus.Present)),
            JournalEntry(1, Mark(2, AttendanceStatus.Late, "На 10 минут")),
            JournalEntry(1, Mark(3, AttendanceStatus.Absent)),
            JournalEntry(1, Mark(4, AttendanceStatus.Excused)),
            JournalEntry(4, Mark(1, AttendanceStatus.Present)),
            JournalEntry(4, Mark(2, AttendanceStatus.Present)),
            JournalEntry(4, Mark(3, AttendanceStatus.Absent)),
        )
        return Journal(groups, students, subjects, lessons, history)
    }
}
