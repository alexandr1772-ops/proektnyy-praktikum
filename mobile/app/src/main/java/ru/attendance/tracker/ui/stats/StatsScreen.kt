package ru.attendance.tracker.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import ru.attendance.tracker.data.Group
import ru.attendance.tracker.data.Journal
import ru.attendance.tracker.data.Risk
import ru.attendance.tracker.data.Subject
import ru.attendance.tracker.data.attendancePercent
import ru.attendance.tracker.ui.common.AppCard
import ru.attendance.tracker.ui.common.Avatar
import ru.attendance.tracker.ui.common.GroupChip
import ru.attendance.tracker.ui.common.OptionChip
import ru.attendance.tracker.ui.common.ProgressBar
import ru.attendance.tracker.ui.common.ScreenHeader
import ru.attendance.tracker.ui.common.ScreenPadding
import ru.attendance.tracker.ui.common.avatarColors
import ru.attendance.tracker.ui.common.percentBarColor
import ru.attendance.tracker.ui.common.percentColor
import ru.attendance.tracker.ui.lessons.subjectStyle
import ru.attendance.tracker.ui.theme.LocalAppColors

private const val SORT_BY_PERCENT = "По проценту"
private const val SORT_BY_NAME = "По имени"
private const val WEEKS_8 = "8 недель"
private const val WEEKS_4 = "4 недели"

@Composable
fun StatsScreen(
    journal: Journal,
    group: Group,
    onGroupChange: (String) -> Unit,
    personal: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    var periodIndex by rememberSaveable { mutableIntStateOf(0) }
    var weeksRange by rememberSaveable { mutableStateOf(WEEKS_8) }
    var sort by rememberSaveable { mutableStateOf(SORT_BY_PERCENT) }
    var showAll by rememberSaveable { mutableStateOf(false) }
    val periods = listOf("Неделя", "Месяц", "Семестр", "Весь период")
    val today = LocalDate.now()
    val start = when (periodIndex) {
        0 -> today.minusDays(6)
        1 -> today.minusDays(29)
        2 -> today.minusMonths(6).plusDays(1)
        else -> null
    }
    val previousStart = when (periodIndex) {
        0 -> today.minusDays(13)
        1 -> today.minusDays(59)
        2 -> start?.minusMonths(6)
        else -> null
    }
    val marks = journal.groupEntries(group.id, start, today)
    val average = attendancePercent(marks)
    val previous = if (previousStart != null && start != null) {
        attendancePercent(journal.groupEntries(group.id, previousStart, start.minusDays(1)))
    } else null
    val delta = if (average != null && previous != null) average - previous else null
    val groupLessons = journal.lessons.filter { it.groupId == group.id }.groupBy { it.subjectId }
    val subjects = journal.subjects.mapNotNull { subject ->
        val lessons = groupLessons[subject.id] ?: return@mapNotNull null
        val ids = lessons.map { it.id }.toSet()
        Subject(subject.name, subject.kind, attendancePercent(marks.filter { it.lessonId in ids }))
    }
    val students = journal.studentCards(group.id, marks)

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Статистика",
            subtitle = if (personal) "Моя посещаемость" else "Посещаемость и анализ группы",
            trailing = { if (!personal) GroupChip(group.name, journal.groups.map { it.name }, onGroupChange) },
        )
        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Период
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                periods.forEachIndexed { index, item ->
                    val selected = index == periodIndex
                    Box(
                        modifier = Modifier
                            .weight(if (index == 3) 1.35f else 1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) colors.primary else colors.soft)
                            .clickable { periodIndex = index }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            item,
                            color = if (selected) colors.onPrimary else colors.title,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                        )
                    }
                }
            }

            AverageCard(
                average, delta, if (delta == null) "Учитываются отметки за выбранный период" else "к предыдущему периоду",
                if (personal) "Моя посещаемость" else "Средняя посещаемость группы",
            )

            // Посещаемость по неделям
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Посещаемость по неделям", color = colors.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    OptionChip(weeksRange, listOf(WEEKS_8, WEEKS_4), { weeksRange = it })
                }
                Spacer(Modifier.height(14.dp))
                val count = if (weeksRange == WEEKS_8) 8 else 4
                val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val weeks = List(count) { monday.minusWeeks((count - 1 - it).toLong()) }
                val values = weeks.map { week ->
                    attendancePercent(journal.groupEntries(group.id, week, minOf(today, week.plusDays(6))))
                }
                if (values.any { it != null }) WeeksChart(values, weeks)
                else Text("За эти недели отметок нет", color = colors.muted)
            }

            // По предметам
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Text("Посещаемость по предметам", color = colors.title, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                if (subjects.isEmpty()) Text("По предметам пока нет данных", color = colors.muted)
                subjects.forEachIndexed { index, subject ->
                    val (icon, tint, tile) = subjectStyle(subject.kind)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(tile),
                            contentAlignment = Alignment.Center,
                        ) { Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp)) }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(subject.name, color = colors.title, fontSize = 15.sp, modifier = Modifier.weight(1f))
                                Spacer(Modifier.width(8.dp))
                                Text(subject.percent?.let { "$it%" } ?: "—", color = subject.percent?.let { percentColor(it) } ?: colors.muted, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(6.dp))
                            subject.percent?.let { ProgressBar(it / 100f, percentBarColor(it), Modifier.fillMaxWidth(), 8.dp) }
                        }
                    }
                    if (index != subjects.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                }
            }

            // Студенты
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (personal) "Мои данные" else "Студенты группы", color = colors.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    if (!personal) OptionChip(sort, listOf(SORT_BY_PERCENT, SORT_BY_NAME), { sort = it })
                }
                Spacer(Modifier.height(6.dp))
                val sorted = if (sort == SORT_BY_PERCENT) {
                    students.sortedByDescending { it.percent }
                } else {
                    students.sortedBy { it.fullName }
                }
                val visible = if (showAll) sorted else sorted.take(5)
                if (visible.isEmpty()) Text("Данных о студентах пока нет", color = colors.muted)
                visible.forEachIndexed { index, student ->
                    val (bg, fg) = avatarColors(student.id, student.risk ?: Risk.Low)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("${index + 1}", color = colors.muted, fontSize = 14.sp, modifier = Modifier.width(22.dp))
                        Avatar(student.initials, bg, fg, size = 40.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(student.shortName, color = colors.title, fontSize = 15.sp, modifier = Modifier.weight(1f))
                                Spacer(Modifier.width(8.dp))
                                Text(student.percent?.let { "$it%" } ?: "—", color = student.percent?.let { percentColor(it) } ?: colors.muted, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(6.dp))
                            student.percent?.let { ProgressBar(it / 100f, percentBarColor(it), Modifier.fillMaxWidth(), 8.dp) }
                        }
                    }
                    if (index != visible.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                }
                Spacer(Modifier.height(8.dp))
                if (students.size > 5) Text(
                    text = if (showAll) "Свернуть" else "Показать всех (${students.size})",
                    color = colors.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showAll = !showAll }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AverageCard(average: Int?, delta: Int?, comparedTo: String, title: String) {
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.soft)
            .border(1.dp, colors.cardBorder, shape)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = colors.label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(average?.let { "$it%" } ?: "Нет данных", color = colors.title, fontSize = 44.sp, fontWeight = FontWeight.Bold)
            FlowRow(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (delta != null && delta != 0) {
                    val up = delta > 0
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (up) colors.successSoft else colors.dangerSoft)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            if (up) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            null,
                            tint = if (up) colors.successText else colors.dangerText,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            (if (up) "+" else "–") + "${kotlin.math.abs(delta)}%",
                            color = if (up) colors.successText else colors.dangerText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Text(comparedTo, color = colors.muted, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.width(12.dp))
        if (average != null) Donut(percent = average)
    }
}

@Composable
private fun Donut(percent: Int) {
    val colors = LocalAppColors.current
    val measurer = rememberTextMeasurer()
    val track = colors.track
    val primary = colors.primary
    val textColor = colors.title
    Canvas(modifier = Modifier.size(92.dp)) {
        val stroke = 11.dp.toPx()
        val inset = stroke / 2
        val arcSize = Size(size.width - stroke, size.height - stroke)
        drawArc(track, -90f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
        drawArc(primary, -90f, 360f * percent / 100f, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        val layout = measurer.measure("$percent%", TextStyle(color = textColor, fontSize = 20.dp.toSp(), fontWeight = FontWeight.Bold))
        drawText(layout, topLeft = Offset((size.width - layout.size.width) / 2, (size.height - layout.size.height) / 2))
    }
}

/** Столбчатая диаграмма посещаемости по неделям. */
@Composable
private fun WeeksChart(values: List<Int?>, weeks: List<LocalDate>) {
    val colors = LocalAppColors.current
    val measurer = rememberTextMeasurer()
    // Подписи на графике не должны расти вместе с системным шрифтом, иначе они налезают друг на друга
    val small = with(LocalDensity.current) { 11.dp.toSp() }
    val axisStyle = TextStyle(color = colors.muted, fontSize = small)
    val valueStyle = TextStyle(color = colors.label, fontSize = small, fontWeight = FontWeight.Medium)
    val grid = colors.divider
    val barTop = colors.primary.copy(alpha = 0.72f)
    val barBottom = colors.primary

    Canvas(modifier = Modifier.fillMaxWidth().height(190.dp)) {
        val leftPad = 38.dp.toPx()
        val bottomPad = 24.dp.toPx()
        val topPad = 18.dp.toPx()
        val chartW = size.width - leftPad
        val chartH = size.height - bottomPad - topPad
        val bottomY = topPad + chartH

        // Сетка и подписи оси
        listOf(0, 25, 50, 75, 100).forEach { mark ->
            val y = bottomY - chartH * mark / 100f
            drawLine(grid, Offset(leftPad, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            val t = measurer.measure("$mark%", axisStyle)
            drawText(t, topLeft = Offset(0f, y - t.size.height / 2f))
        }

        val slot = chartW / values.size
        val barW = slot * 0.62f
        values.forEachIndexed { index, value ->
            val left = leftPad + slot * index + (slot - barW) / 2
            val week = weeks[index]
            val weekLayout = measurer.measure("${week.dayOfMonth}.${week.monthValue}", axisStyle)
            drawText(weekLayout, topLeft = Offset(left + (barW - weekLayout.size.width) / 2, bottomY + 6.dp.toPx()))
            if (value == null) return@forEachIndexed
            val h = chartH * value / 100f
            val top = bottomY - h
            val path = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left, top, left + barW, bottomY,
                        topLeftCornerRadius = CornerRadius(6.dp.toPx()),
                        topRightCornerRadius = CornerRadius(6.dp.toPx()),
                    ),
                )
            }
            drawPath(path, Brush.verticalGradient(listOf(barTop, barBottom), startY = top, endY = bottomY))

            val valueLayout = measurer.measure("$value%", valueStyle)
            drawText(valueLayout, topLeft = Offset(left + (barW - valueLayout.size.width) / 2, top - valueLayout.size.height - 2.dp.toPx()))
        }
    }
}
