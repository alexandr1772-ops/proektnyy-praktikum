package ru.attendance.tracker.ui.lessons

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import ru.attendance.tracker.data.AttendanceStore
import ru.attendance.tracker.data.DemoData
import ru.attendance.tracker.data.Lesson
import ru.attendance.tracker.data.SubjectKind
import ru.attendance.tracker.ui.common.AppCard
import ru.attendance.tracker.ui.common.GroupChip
import ru.attendance.tracker.ui.common.ProgressBar
import ru.attendance.tracker.ui.common.ScreenHeader
import ru.attendance.tracker.ui.common.ScreenPadding
import ru.attendance.tracker.ui.theme.LocalAppColors

/** Значок и цвета плитки предмета. */
@Composable
fun subjectStyle(kind: SubjectKind): Triple<ImageVector, Color, Color> {
    val colors = LocalAppColors.current
    return when (kind) {
        SubjectKind.Database -> Triple(Icons.Filled.Storage, colors.primary, colors.soft)
        SubjectKind.Programming -> Triple(Icons.Filled.Code, colors.success, colors.successSoft)
        SubjectKind.Math -> Triple(Icons.Filled.Calculate, colors.orange, colors.orangeSoft)
    }
}

@Composable
fun LessonsScreen(
    store: AttendanceStore,
    group: String,
    onGroupChange: (String) -> Unit,
    onOpenLesson: (Lesson) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val today = remember { DemoData.todayText() }

    val lessons = DemoData.lessons.filter { it.subject.name.contains(query.trim(), ignoreCase = true) }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Занятия",
            subtitle = today,
            trailing = {
                GroupChip(group = group, groups = DemoData.groups, onSelect = onGroupChange)
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.soft)
                        .clickable {
                            searchOpen = !searchOpen
                            if (!searchOpen) query = ""
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Search, contentDescription = "Поиск", tint = colors.icon, modifier = Modifier.size(22.dp))
                }
            },
        )

        if (searchOpen) {
            Row(
                modifier = Modifier
                    .padding(horizontal = ScreenPadding, vertical = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.soft)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = colors.placeholder, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) Text("Поиск по предметам", color = colors.placeholder, fontSize = 15.sp)
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(color = colors.input, fontSize = 15.sp),
                        cursorBrush = SolidColor(colors.primary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        } else {
            Spacer(Modifier.height(16.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (lessons.isEmpty()) {
                Text("Ничего не найдено", color = colors.muted, fontSize = 15.sp, modifier = Modifier.padding(top = 24.dp))
            }
            lessons.forEach { lesson ->
                LessonCard(lesson, marked = store.markedCount(lesson.id), total = DemoData.students.size) {
                    onOpenLesson(lesson)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LessonCard(lesson: Lesson, marked: Int, total: Int, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val (icon, tint, tile) = subjectStyle(lesson.subject.kind)
    val fraction = marked / total.toFloat()
    val percent = (fraction * 100).roundToInt()
    val barColor = when {
        marked == 0 -> colors.track
        marked == total -> colors.success
        fraction >= 0.7f -> colors.primary
        else -> colors.orange
    }

    AppCard(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(tile),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.subject.name, color = colors.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Groups, null, tint = colors.muted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(lesson.group, color = colors.muted, fontSize = 14.sp)
                }
                Spacer(Modifier.height(4.dp))
                // Время и номер пары: если не помещаются в одну строку, пара переносится целиком
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AccessTime, null, tint = colors.muted, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${lesson.start} – ${lesson.end}", color = colors.muted, fontSize = 14.sp, softWrap = false)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.School, null, tint = colors.muted, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${lesson.pair} пара", color = colors.muted, fontSize = 14.sp, softWrap = false)
                    }
                }
            }
            Icon(Icons.Filled.ChevronRight, null, tint = colors.icon, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(14.dp))
        Row {
            Text("Отмечено $marked из $total", color = colors.label, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text("$percent%", color = colors.label, fontSize = 14.sp)
        }
        Spacer(Modifier.height(8.dp))
        ProgressBar(fraction = fraction, color = barColor, modifier = Modifier.fillMaxWidth(), height = 8.dp)
    }
}
