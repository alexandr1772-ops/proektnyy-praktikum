package ru.attendance.tracker.ui.risk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.attendance.tracker.data.DemoData
import ru.attendance.tracker.data.Risk
import ru.attendance.tracker.data.Student
import ru.attendance.tracker.ui.common.AppCard
import ru.attendance.tracker.ui.common.Avatar
import ru.attendance.tracker.ui.common.Badge
import ru.attendance.tracker.ui.common.GroupChip
import ru.attendance.tracker.ui.common.OptionChip
import ru.attendance.tracker.ui.common.ProgressBar
import ru.attendance.tracker.ui.common.ScreenHeader
import ru.attendance.tracker.ui.common.ScreenPadding
import ru.attendance.tracker.ui.common.avatarColors
import ru.attendance.tracker.ui.common.percentBarColor
import ru.attendance.tracker.ui.common.percentColor
import ru.attendance.tracker.ui.theme.LocalAppColors

private const val SORT_BY_PERCENT = "По проценту"
private const val SORT_BY_NAME = "По имени"

/** «1 студент», «2 студента», «5 студентов». */
private fun studentsText(n: Int): String {
    val mod100 = n % 100
    val mod10 = n % 10
    val word = when {
        mod100 in 11..14 -> "студентов"
        mod10 == 1 -> "студент"
        mod10 in 2..4 -> "студента"
        else -> "студентов"
    }
    return "$n $word"
}

@Composable
fun RiskScreen(group: String, onGroupChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    var sortRisk by rememberSaveable { mutableStateOf(SORT_BY_PERCENT) }
    var sortRest by rememberSaveable { mutableStateOf(SORT_BY_PERCENT) }

    val students = DemoData.students
    val atRisk = students.filter { it.risk != Risk.Low }
    val rest = students.filter { it.risk == Risk.Low }
    val high = students.count { it.risk == Risk.High }
    val medium = students.count { it.risk == Risk.Medium }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Группа риска",
            subtitle = "Студенты с низкой или резко упавшей посещаемостью",
            trailing = { GroupChip(group, DemoData.groups, onGroupChange) },
        )
        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Сводка
            val bannerShape = RoundedCornerShape(20.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(bannerShape)
                    .background(colors.dangerSoft)
                    .border(1.dp, colors.danger.copy(alpha = 0.18f), bannerShape)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(colors.danger.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Filled.Groups, null, tint = colors.dangerText, modifier = Modifier.size(30.dp)) }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("В зоне риска", color = colors.dangerText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(studentsText(atRisk.size), color = colors.dangerText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("Требуют повышенного внимания", color = colors.muted, fontSize = 13.sp)
                }
                Icon(Icons.Filled.ChevronRight, null, tint = colors.icon, modifier = Modifier.size(24.dp))
            }

            // Три счётчика
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricTile(Icons.Filled.ArrowUpward, colors.dangerText, colors.dangerSoft, high, "Высокий риск", Modifier.weight(1f))
                MetricTile(Icons.Filled.PriorityHigh, colors.warningText, colors.warningSoft, medium, "Средний риск", Modifier.weight(1f))
                MetricTile(Icons.Filled.Check, colors.primary, colors.soft, rest.size, "Низкий риск", Modifier.weight(1f))
            }

            // В зоне риска
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Студенты в зоне риска", color = colors.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    OptionChip(sortRisk, listOf(SORT_BY_PERCENT, SORT_BY_NAME), { sortRisk = it })
                }
                Spacer(Modifier.height(6.dp))
                val list = if (sortRisk == SORT_BY_PERCENT) atRisk.sortedBy { it.percent } else atRisk.sortedBy { it.fullName }
                list.forEachIndexed { index, student ->
                    StudentRiskRow(index + 1, student, group)
                    if (index != list.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                }
            }

            // Остальные
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Остальные студенты", color = colors.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    OptionChip(sortRest, listOf(SORT_BY_PERCENT, SORT_BY_NAME), { sortRest = it })
                }
                Spacer(Modifier.height(6.dp))
                val list = if (sortRest == SORT_BY_PERCENT) rest.sortedByDescending { it.percent } else rest.sortedBy { it.fullName }
                list.forEachIndexed { index, student ->
                    StudentRiskRow(atRisk.size + index + 1, student, group)
                    if (index != list.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MetricTile(icon: ImageVector, tint: Color, tile: Color, value: Int, label: String, modifier: Modifier) {
    val colors = LocalAppColors.current
    AppCard(modifier = modifier, shape = RoundedCornerShape(18.dp), contentPadding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(34.dp).clip(CircleShape).background(tile),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.width(8.dp))
            Text("$value", color = colors.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = colors.muted, fontSize = 12.sp, maxLines = 2)
    }
}

@Composable
private fun StudentRiskRow(rank: Int, student: Student, group: String) {
    val colors = LocalAppColors.current
    val (avatarBg, avatarFg) = avatarColors(student.id, student.risk)
    val (badgeText, badgeFg, badgeBg) = when (student.risk) {
        Risk.High -> Triple("Высокий", colors.dangerText, colors.dangerSoft)
        Risk.Medium -> Triple("Средний", colors.warningText, colors.warningSoft)
        Risk.Low -> Triple("Низкий", colors.primary, colors.soft)
    }
    val barColor = when (student.risk) {
        Risk.High -> colors.danger
        Risk.Medium -> colors.warning
        Risk.Low -> percentBarColor(student.percent)
    }
    val percentTextColor = if (student.risk == Risk.Low) percentColor(student.percent) else barColor

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text("$rank", color = colors.muted, fontSize = 14.sp, modifier = Modifier.width(22.dp).padding(top = 12.dp))
        Avatar(student.initials, avatarBg, avatarFg, size = 44.dp)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(student.shortName, color = colors.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.ChevronRight, null, tint = colors.icon, modifier = Modifier.size(22.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(group, color = colors.muted, fontSize = 12.sp)
                Spacer(Modifier.width(8.dp))
                Badge(badgeText, badgeFg, badgeBg)
                Spacer(Modifier.weight(1f))
                Text("${student.percent}%", color = percentTextColor, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 22.dp))
            }
            Spacer(Modifier.height(6.dp))
            ProgressBar(student.percent / 100f, barColor, Modifier.fillMaxWidth(), 6.dp)
            if (student.riskNote != null) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (student.riskTrendDown) Icons.Filled.ArrowDownward else Icons.Outlined.CalendarToday,
                        null,
                        tint = colors.muted,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(student.riskNote, color = colors.muted, fontSize = 13.sp, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
