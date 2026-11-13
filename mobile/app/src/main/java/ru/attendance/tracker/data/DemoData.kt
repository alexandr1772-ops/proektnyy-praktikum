package ru.attendance.tracker.data

import androidx.compose.runtime.mutableStateMapOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Демо-данные только для просмотра дизайна. Настоящие данные будут приходить с сервера.

enum class AttendanceStatus { Present, Absent, Late, Excused }

enum class Risk { High, Medium, Low }

enum class SubjectKind { Database, Programming, Math }

class Student(
    val id: Int,
    val fullName: String,
    /** Посещаемость за период в процентах. */
    val percent: Int,
    val risk: Risk = Risk.Low,
    /** Причина, по которой студент в зоне риска. */
    val riskNote: String? = null,
    val riskTrendDown: Boolean = false,
) {
    /** «Иванов Иван Иванович» -> «ИИ». */
    val initials: String
        get() = fullName.split(" ").take(2).joinToString("") { it.take(1) }

    /** «Иванов Иван Иванович» -> «Иванов И.И.». */
    val shortName: String
        get() {
            val parts = fullName.split(" ")
            return parts.first() + " " + parts.drop(1).joinToString("") { it.take(1) + "." }
        }
}

class Subject(val name: String, val kind: SubjectKind, val percent: Int)

class Lesson(
    val id: Int,
    val subject: Subject,
    val group: String,
    val start: String,
    val end: String,
    val pair: Int,
)

class StatsPeriod(val label: String, val average: Int, val delta: Int?, val comparedTo: String)

object DemoData {
    const val DEMO_LOGIN = "admin"
    const val DEMO_PASSWORD = "1234"

    val groups = listOf("ИС-21", "ИС-22")

    val students = listOf(
        Student(1, "Иванов Иван Иванович", 96),
        Student(2, "Петров Пётр Сергеевич", 92),
        Student(3, "Смирнова Анна Олеговна", 32, Risk.High, "Пропущено 4 пары подряд"),
        Student(4, "Кузнецова Мария Андреевна", 45, Risk.High, "Пропущено 3 пары подряд"),
        Student(5, "Васильева Екатерина Сергеевна", 58, Risk.Medium, "Резкое снижение (–20% за 2 недели)", riskTrendDown = true),
        Student(6, "Крылов Роман Олегович", 89),
        Student(7, "Морозова Наталья Викторовна", 87),
        Student(8, "Белов Александр Сергеевич", 85),
        Student(9, "Соколова Дарья Игоревна", 94),
        Student(10, "Попов Дмитрий Андреевич", 91),
        Student(11, "Лебедев Артём Николаевич", 90),
        Student(12, "Козлова Полина Михайловна", 88),
        Student(13, "Новиков Кирилл Денисович", 84),
        Student(14, "Орлова Ксения Павловна", 83),
        Student(15, "Михайлов Егор Алексеевич", 82),
        Student(16, "Фёдорова Алиса Романовна", 80),
        Student(17, "Волков Максим Игоревич", 79),
        Student(18, "Алексеева Виктория Андреевна", 78),
        Student(19, "Захаров Никита Олегович", 77),
        Student(20, "Степанова Юлия Дмитриевна", 75),
        Student(21, "Тарасов Илья Валерьевич", 74),
        Student(22, "Беляева Анастасия Павловна", 72),
        Student(23, "Макаров Тимофей Артёмович", 71),
        Student(24, "Андреева София Кирилловна", 70),
    )

    val databases = Subject("Базы данных", SubjectKind.Database, 82)
    val programming = Subject("Программирование", SubjectKind.Programming, 76)
    val math = Subject("Математика", SubjectKind.Math, 68)
    val subjects = listOf(databases, programming, math)

    val lessons = listOf(
        Lesson(1, databases, "ИС-21", "08:30", "10:00", 1),
        Lesson(2, programming, "ИС-21", "10:10", "11:40", 2),
        Lesson(3, math, "ИС-21", "12:10", "13:40", 3),
        Lesson(4, databases, "ИС-21", "14:00", "15:30", 4),
    )

    /** Посещаемость группы по неделям, последние 8 недель. */
    val weeks = listOf(72, 68, 75, 80, 77, 82, 79, 78)

    val periods = listOf(
        StatsPeriod("Неделя", 78, 5, "по сравнению с прошлой неделей"),
        StatsPeriod("Месяц", 76, 2, "по сравнению с прошлым месяцем"),
        StatsPeriod("Семестр", 74, -1, "по сравнению с прошлым семестром"),
        StatsPeriod("Весь период", 75, null, "за всё время обучения"),
    )

    /** Сколько студентов уже отмечено на каждой паре в начале (как на макете). */
    private val initiallyMarked = mapOf(1 to 18, 2 to 24, 3 to 15, 4 to 0)

    /** Начальные отметки: у первых студентов — те же статусы, что на макете, у остальных в основном «присутствует». */
    fun initialMarks(lessonId: Int): Map<Int, AttendanceStatus> {
        val count = initiallyMarked[lessonId] ?: 0
        val special = mapOf(
            1 to AttendanceStatus.Present,
            2 to AttendanceStatus.Absent,
            3 to AttendanceStatus.Late,
            4 to AttendanceStatus.Excused,
        )
        return students.take(count).associate { student ->
            student.id to when {
                lessonId == 1 && student.id in special -> special.getValue(student.id)
                lessonId == 1 && student.id == 9 -> AttendanceStatus.Late
                student.id == 3 || student.id == 4 -> AttendanceStatus.Absent
                else -> AttendanceStatus.Present
            }
        }
    }

    fun todayText(): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru"))
        return formatter.format(LocalDate.now()).replaceFirstChar { it.uppercase() }
    }
}

/** Отметки посещаемости, сохранённые за время работы приложения (в памяти, без сервера). */
class AttendanceStore {
    private val marks = mutableStateMapOf<Int, Map<Int, AttendanceStatus>>().apply {
        DemoData.lessons.forEach { put(it.id, DemoData.initialMarks(it.id)) }
    }

    fun marksFor(lessonId: Int): Map<Int, AttendanceStatus> = marks[lessonId] ?: emptyMap()

    fun markedCount(lessonId: Int): Int = marksFor(lessonId).size

    fun save(lessonId: Int, newMarks: Map<Int, AttendanceStatus>) {
        marks[lessonId] = newMarks.toMap()
    }
}
