package ru.attendance.tracker.ui.attendance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.attendance.tracker.data.AttendanceStatus
import ru.attendance.tracker.data.AttendanceStore
import ru.attendance.tracker.data.DemoData
import ru.attendance.tracker.data.Lesson
import ru.attendance.tracker.data.Student
import ru.attendance.tracker.ui.common.AppCard
import ru.attendance.tracker.ui.common.Avatar
import ru.attendance.tracker.ui.common.ProgressBar
import ru.attendance.tracker.ui.common.ScreenPadding
import ru.attendance.tracker.ui.common.avatarColors
import ru.attendance.tracker.ui.lessons.subjectStyle
import ru.attendance.tracker.ui.theme.AppColors
import ru.attendance.tracker.ui.theme.LocalAppColors

private val AllStatuses = listOf(
    AttendanceStatus.Present,
    AttendanceStatus.Absent,
    AttendanceStatus.Late,
    AttendanceStatus.Excused,
)

private fun AttendanceStatus.label() = when (this) {
    AttendanceStatus.Present -> "Присутствует"
    AttendanceStatus.Absent -> "Отсутствует"
    AttendanceStatus.Late -> "Опоздал"
    AttendanceStatus.Excused -> "Уважительная"
}

/** Цвета выбранной кнопки статуса: рамка, фон, текст. */
private fun AttendanceStatus.palette(c: AppColors): Triple<Color, Color, Color> = when (this) {
    AttendanceStatus.Present -> Triple(c.success, c.successSoft, c.successText)
    AttendanceStatus.Absent -> Triple(c.danger, c.dangerSoft, c.dangerText)
    AttendanceStatus.Late -> Triple(c.warning, c.warningSoft, c.warningText)
    AttendanceStatus.Excused -> Triple(c.primary, c.soft, c.primary)
}

@Composable
fun AttendanceScreen(
    lesson: Lesson,
    store: AttendanceStore,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val students = DemoData.students
    val draft: SnapshotStateMap<Int, AttendanceStatus> = remember(lesson.id) {
        mutableStateMapOf<Int, AttendanceStatus>().apply { putAll(store.marksFor(lesson.id)) }
    }
    var query by rememberSaveable { mutableStateOf("") }
    val shown = students.filter { it.fullName.contains(query.trim(), ignoreCase = true) }

    val marked = draft.size
    val fraction = marked / students.size.toFloat()

    Column(modifier = modifier.fillMaxSize()) {
        // Верхняя строка с кнопкой «назад»
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 8.dp, end = ScreenPadding, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Назад", tint = colors.title, modifier = Modifier.size(22.dp))
            }
            Text("Отметка посещаемости", color = colors.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = ScreenPadding, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { LessonSummary(lesson) }
            item {
                Column {
                    Row {
                        Text("Отмечено $marked из ${students.size}", color = colors.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${(fraction * 100).toInt()}%", color = colors.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    ProgressBar(fraction, colors.primary, Modifier.fillMaxWidth())
                }
            }
            item {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("Студенты группы", color = colors.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("${students.size} человека", color = colors.muted, fontSize = 14.sp)
                }
            }
            item { SearchField(query) { query = it } }
            if (shown.isEmpty()) {
                item { Text("Студент не найден", color = colors.muted, fontSize = 15.sp, modifier = Modifier.padding(vertical = 12.dp)) }
            }
            items(shown, key = { it.id }) { student ->
                StudentRow(
                    student = student,
                    group = lesson.group,
                    selected = draft[student.id],
                    onSelect = { status -> draft[student.id] = status },
                )
            }
        }

        // Нижняя панель с кнопкой сохранения
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.navBar)
                .padding(horizontal = ScreenPadding, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = {
                    store.save(lesson.id, draft)
                    onSaved()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
            ) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text("Сохранить отметки", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(6.dp))
            Text("Отмечено $marked из ${students.size} студентов", color = colors.muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun LessonSummary(lesson: Lesson) {
    val colors = LocalAppColors.current
    val (icon, tint, tile) = subjectStyle(lesson.subject.kind)
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)).background(tile),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(34.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(lesson.subject.name, color = colors.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text("${lesson.group} • ${DemoData.todayText()}", color = colors.muted, fontSize = 14.sp)
                Text("${lesson.pair} пара • ${lesson.start}–${lesson.end}", color = colors.muted, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.soft)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = colors.placeholder, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) Text("Поиск студента", color = colors.placeholder, fontSize = 16.sp)
            BasicTextField(
                value = query,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(color = colors.input, fontSize = 16.sp),
                cursorBrush = SolidColor(colors.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StudentRow(student: Student, group: String, selected: AttendanceStatus?, onSelect: (AttendanceStatus) -> Unit) {
    val colors = LocalAppColors.current
    val (avatarBg, avatarFg) = avatarColors(student.id)
    AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14.dp) {
        // Четыре кнопки с полными подписями помещаются в один ряд только на широком экране (планшет)
        val oneRow = LocalConfiguration.current.screenWidthDp >= 600
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(student.initials, avatarBg, avatarFg, size = 46.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(student.fullName, color = colors.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Студент • $group", color = colors.muted, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        if (!oneRow) {
            AllStatuses.chunked(2).forEachIndexed { index, pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = if (index == 0) 0.dp else 8.dp)) {
                    pair.forEach { status -> StatusChip(status, selected == status, Modifier.weight(1f)) { onSelect(status) } }
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AllStatuses.forEach { status -> StatusChip(status, selected == status, Modifier.weight(1f)) { onSelect(status) } }
            }
        }
    }
}

@Composable
private fun StatusChip(status: AttendanceStatus, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val (accent, soft, text) = status.palette(colors)
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(if (selected) soft else colors.chipOff)
            .then(if (selected) Modifier.border(BorderStroke(1.5.dp, accent), shape) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (selected) accent else colors.placeholder),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = status.label(),
            color = if (selected) text else colors.muted,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
