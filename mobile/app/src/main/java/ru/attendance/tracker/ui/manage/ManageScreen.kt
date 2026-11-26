package ru.attendance.tracker.ui.manage

import android.content.ContentResolver
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.ByteArrayOutputStream
import java.nio.charset.CharacterCodingException
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.attendance.tracker.data.Change
import ru.attendance.tracker.data.Entity
import ru.attendance.tracker.data.Group
import ru.attendance.tracker.data.Journal
import ru.attendance.tracker.data.JournalException
import ru.attendance.tracker.data.LessonRecord
import ru.attendance.tracker.data.Role
import ru.attendance.tracker.data.StudentRecord
import ru.attendance.tracker.data.SubjectKind
import ru.attendance.tracker.data.SubjectRecord
import ru.attendance.tracker.data.dateText
import ru.attendance.tracker.ui.common.AppCard
import ru.attendance.tracker.ui.common.GroupChip
import ru.attendance.tracker.ui.common.OptionChip
import ru.attendance.tracker.ui.common.ScreenHeader
import ru.attendance.tracker.ui.common.ScreenPadding
import ru.attendance.tracker.ui.theme.LocalAppColors

private data class Editor(
    val id: Int = 0,
    val name: String = "",
    val groupId: Int = 0,
    val subjectId: Int = 0,
    val kind: SubjectKind = SubjectKind.Other,
    val date: String = LocalDate.now().toString(),
    val start: String = "08:30",
    val end: String = "10:00",
    val pair: String = "1",
)

private data class Entry(val id: Int, val title: String, val subtitle: String, val editor: Editor)

@Composable
internal fun ManageScreen(
    journal: Journal,
    group: Group?,
    role: Role,
    busy: Boolean,
    onGroupChange: (String) -> Unit,
    onChange: (Change, (String?) -> Unit) -> Unit,
    onImport: (suspend () -> Change.ImportStudents) -> Unit,
    onExport: (Int, suspend (String) -> Unit) -> Unit,
) {
    val colors = LocalAppColors.current
    val resolver = LocalContext.current.contentResolver
    val entities = if (role == Role.Admin) Entity.entries else listOf(Entity.Students, Entity.Lessons)
    var entity by rememberSaveable { mutableStateOf(entities.first()) }
    var editor by remember { mutableStateOf<Editor?>(null) }
    var deleting by remember { mutableStateOf<Entry?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var fileGroupId by rememberSaveable { mutableIntStateOf(0) }
    val selectCsv = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null && fileGroupId != 0) {
            val id = fileGroupId
            onImport { Change.ImportStudents(id, readCsv(resolver, uri)) }
        }
        fileGroupId = 0
    }
    val saveCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null && fileGroupId != 0) {
            onExport(fileGroupId) { csv ->
                withContext(Dispatchers.IO) {
                    val stream = resolver.openOutputStream(uri, "wt")
                        ?: throw JournalException("Не удалось открыть файл для записи")
                    stream.bufferedWriter(Charsets.UTF_8).use { it.write(csv) }
                }
            }
        }
        fileGroupId = 0
    }
    val entries = when (entity) {
        Entity.Groups -> journal.groups.sortedBy { it.name }.map {
            val count = journal.students.count { student -> student.groupId == it.id }
            Entry(it.id, it.name, "Студентов: $count", Editor(id = it.id, name = it.name))
        }
        Entity.Students -> journal.students.filter { it.groupId == group?.id }.sortedBy { it.fullName }.map {
            Entry(it.id, it.fullName, group?.name.orEmpty(), Editor(id = it.id, name = it.fullName, groupId = it.groupId))
        }
        Entity.Subjects -> journal.subjects.sortedBy { it.name }.map {
            Entry(it.id, it.name, "", Editor(id = it.id, name = it.name, kind = it.kind))
        }
        Entity.Lessons -> journal.lessons.filter { it.groupId == group?.id }
            .sortedWith(compareByDescending<LessonRecord> { it.date }.thenBy { it.start }).map { lesson ->
                val subject = journal.subjects.find { it.id == lesson.subjectId }
                Entry(
                    lesson.id,
                    subject?.name ?: "Занятие",
                    "${dateText(lesson.date)} · ${lesson.pair} пара · ${lesson.start}–${lesson.end}",
                    Editor(lesson.id, groupId = lesson.groupId, subjectId = lesson.subjectId,
                        date = lesson.date.toString(), start = lesson.start, end = lesson.end, pair = lesson.pair.toString()),
                )
            }
    }
    val canCreate = when (entity) {
        Entity.Students -> group != null
        Entity.Lessons -> group != null && journal.subjects.isNotEmpty()
        else -> true
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Данные",
            subtitle = "Группы, студенты, предметы и занятия",
            trailing = {
                if (group != null && (entity == Entity.Lessons || entity == Entity.Students)) {
                    GroupChip(group.name, journal.groups.map { it.name }, onGroupChange)
                }
            },
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OptionChip(entity.label, entities.map { it.label }, { name -> entity = entities.first { it.label == name } })
            Button(enabled = !busy && canCreate, onClick = {
                error = null
                editor = Editor(groupId = group?.id ?: 0, subjectId = journal.subjects.firstOrNull()?.id ?: 0)
            }) { Text("Добавить") }
        }
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (group != null && entity == Entity.Students) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(enabled = !busy, onClick = {
                            fileGroupId = group.id
                            selectCsv.launch(arrayOf("text/*", "application/csv", "application/vnd.ms-excel", "application/octet-stream"))
                        }) { Text("Импорт CSV") }
                        TextButton(enabled = !busy, onClick = {
                            fileGroupId = group.id
                            saveCsv.launch("attendance-${group.id}.csv")
                        }) { Text("Экспорт CSV") }
                    }
                }
            }
            if (entries.isEmpty()) {
                item {
                    val message = when {
                        group == null && entity in listOf(Entity.Students, Entity.Lessons) -> "Сначала добавьте группу"
                        entity == Entity.Lessons && journal.subjects.isEmpty() -> "Сначала добавьте предмет"
                        else -> "Здесь пока нет записей"
                    }
                    Text(message, color = colors.muted)
                }
            }
            items(entries, key = { it.id }) { entry ->
                AppCard(Modifier.fillMaxWidth().clickable(enabled = !busy) {
                    error = null
                    editor = entry.editor
                }) {
                    Text(entry.title, color = colors.title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    if (entry.subtitle.isNotBlank()) Text(entry.subtitle, color = colors.muted)
                }
            }
        }
    }

    val draft = editor
    if (draft != null) {
        fun dismiss() {
            if (!busy) editor = null
        }
        EditDialog(entity, draft, journal, busy, error, onUpdate = { editor = it; error = null },
            onDismiss = ::dismiss,
            onDelete = if (draft.id == 0) null else {
                {
                    editor = null
                    deleting = entries.firstOrNull { it.id == draft.id }
                    error = null
                }
            },
            onSave = {
                try {
                    onChange(draft.toChange(entity)) { message ->
                        error = message
                        if (message == null) editor = null
                    }
                } catch (failure: JournalException) {
                    error = failure.message
                }
            },
        )
    }

    val entry = deleting
    if (entry != null) {
        AlertDialog(
            onDismissRequest = { if (!busy) deleting = null },
            title = { Text("Удалить запись?") },
            text = {
                Column {
                    Text(entry.title)
                    error?.let { Text(it, color = colors.dangerText, modifier = Modifier.padding(top = 8.dp)) }
                }
            },
            confirmButton = {
                TextButton(enabled = !busy, onClick = {
                    onChange(Change.Delete(entity, entry.id)) { message ->
                        error = message
                        if (message == null) deleting = null
                    }
                }) { Text("Удалить") }
            },
            dismissButton = { TextButton(enabled = !busy, onClick = { deleting = null }) { Text("Отмена") } },
        )
    }
}

@Composable
private fun EditDialog(
    entity: Entity,
    value: Editor,
    journal: Journal,
    busy: Boolean,
    error: String?,
    onUpdate: (Editor) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSave: () -> Unit,
) {
    val colors = LocalAppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding(),
        title = { Text(if (value.id == 0) "Добавить запись" else "Редактировать запись") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (entity != Entity.Lessons) {
                    Field(if (entity == Entity.Students) "ФИО" else "Название", value.name, busy) {
                        onUpdate(value.copy(name = it))
                    }
                }
                if (entity == Entity.Students || entity == Entity.Lessons) {
                    Choice("Группа", value.groupId, journal.groups.map { it.id to it.name }, busy) {
                        onUpdate(value.copy(groupId = it))
                    }
                }
                if (entity == Entity.Lessons) {
                    Choice("Предмет", value.subjectId, journal.subjects.map { it.id to it.name }, busy) {
                        onUpdate(value.copy(subjectId = it))
                    }
                    Field("Дата (ГГГГ-ММ-ДД)", value.date, busy) { onUpdate(value.copy(date = it)) }
                    Field("Номер пары", value.pair, busy) { onUpdate(value.copy(pair = it)) }
                    Field("Начало (ЧЧ:ММ)", value.start, busy) { onUpdate(value.copy(start = it)) }
                    Field("Конец (ЧЧ:ММ)", value.end, busy) { onUpdate(value.copy(end = it)) }
                }
                error?.let { Text(it, color = colors.dangerText) }
                if (onDelete != null) {
                    TextButton(enabled = !busy, onClick = onDelete) { Text("Удалить", color = colors.dangerText) }
                }
            }
        },
        confirmButton = { TextButton(enabled = !busy, onClick = onSave) { Text(if (busy) "Сохраняем…" else "Сохранить") } },
        dismissButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun Field(label: String, value: String, busy: Boolean, onChange: (String) -> Unit) {
    OutlinedTextField(value, onChange, label = { Text(label) }, enabled = !busy, singleLine = true,
        modifier = Modifier.fillMaxWidth())
}

@Composable
private fun Choice(label: String, id: Int, options: List<Pair<Int, String>>, busy: Boolean, onChoose: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val name = options.firstOrNull { it.first == id }?.second ?: "Выбрать"
    Column {
        TextButton(enabled = !busy && options.isNotEmpty(), onClick = { open = true }) { Text("$label · $name") }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { (key, option) ->
                DropdownMenuItem(text = { Text(option) }, onClick = { open = false; onChoose(key) })
            }
        }
    }
}

private fun Editor.toChange(entity: Entity): Change = when (entity) {
    Entity.Groups -> Change.SaveGroup(Group(id, name))
    Entity.Subjects -> Change.SaveSubject(SubjectRecord(id, name, kind))
    Entity.Students -> Change.SaveStudent(StudentRecord(id, name, groupId))
    Entity.Lessons -> {
        val parsed = runCatching { LocalDate.parse(date.trim()) }
            .getOrElse { throw JournalException("Введите дату в формате ГГГГ-ММ-ДД") }
        val number = pair.trim().toIntOrNull() ?: throw JournalException("Введите номер пары")
        Change.SaveLesson(LessonRecord(id, subjectId, groupId, parsed, start, end, number))
    }
}

private suspend fun readCsv(resolver: ContentResolver, uri: Uri): String = withContext(Dispatchers.IO) {
    val stream = resolver.openInputStream(uri) ?: throw JournalException("Не удалось открыть CSV-файл")
    stream.use {
        val bytes = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val count = it.read(buffer)
            if (count < 0) break
            if (bytes.size() + count > 2 * 1024 * 1024) throw JournalException("CSV-файл больше 2 МБ")
            bytes.write(buffer, 0, count)
        }
        try {
            bytes.toByteArray().decodeToString(throwOnInvalidSequence = true)
        } catch (error: CharacterCodingException) {
            throw JournalException("Сохраните CSV в кодировке UTF-8")
        }
    }
}
