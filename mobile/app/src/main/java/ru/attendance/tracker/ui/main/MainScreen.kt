package ru.attendance.tracker.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.GppMaybe
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.attendance.tracker.data.Account
import ru.attendance.tracker.data.AttendanceRepository
import ru.attendance.tracker.data.Change
import ru.attendance.tracker.data.Journal
import ru.attendance.tracker.data.JournalException
import ru.attendance.tracker.data.Role
import ru.attendance.tracker.ui.attendance.AttendanceScreen
import ru.attendance.tracker.ui.manage.ManageScreen
import ru.attendance.tracker.ui.lessons.LessonsScreen
import ru.attendance.tracker.ui.login.LoginBackground
import ru.attendance.tracker.ui.profile.ProfileScreen
import ru.attendance.tracker.ui.risk.RiskScreen
import ru.attendance.tracker.ui.stats.StatsScreen
import ru.attendance.tracker.ui.theme.LocalAppColors

private enum class Tab(val title: String, val selected: ImageVector, val unselected: ImageVector) {
    Lessons("Занятия", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    Stats("Статистика", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    Risk("Риск", Icons.Filled.GppMaybe, Icons.Outlined.GppMaybe),
    Manage("Данные", Icons.Filled.Folder, Icons.Outlined.Folder),
    Profile("Профиль", Icons.Filled.Person, Icons.Outlined.Person),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    repository: AttendanceRepository,
    account: Account,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val scope = rememberCoroutineScope()
    val messages = remember { SnackbarHostState() }
    val personal = account.role == Role.Student
    val tabs = if (personal) listOf(Tab.Stats, Tab.Profile) else Tab.entries
    val firstTab = tabs.first()
    var tab by rememberSaveable(account.login) { mutableStateOf(firstTab) }
    var groupId by rememberSaveable(account.login) { mutableStateOf<Int?>(null) }
    var openLessonId by rememberSaveable(account.login) { mutableStateOf<Int?>(null) }
    var journal by remember { mutableStateOf<Journal?>(null) }
    var busy by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }

    fun notify(message: String) {
        scope.launch { messages.showSnackbar(message) }
    }

    fun perform(operation: suspend () -> Unit) {
        if (busy) return
        busy = true
        scope.launch {
            try {
                operation()
            } finally {
                busy = false
            }
        }
    }

    suspend fun load(): Journal = withContext(Dispatchers.IO) { repository.load(account) }

    fun refresh() = perform {
        try {
            journal = load()
            loadError = null
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            loadError = error.userMessage("Не удалось загрузить журнал")
        }
    }

    fun submit(change: suspend () -> Change, done: (String?) -> Unit = { error -> error?.let(::notify) }) {
        perform {
            val edit = try {
                val input = change()
                withContext(Dispatchers.IO) { repository.change(account, input) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                done(error.userMessage("Не удалось выполнить действие"))
                return@perform
            }
            done(null)
            try {
                journal = load()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                loadError = "Изменение сохранено. Обновите журнал, чтобы продолжить"
            }
            notify(edit)
        }
    }

    LaunchedEffect(repository, account) { refresh() }

    val data = journal
    val group = data?.groups?.firstOrNull { it.id == groupId } ?: data?.groups?.firstOrNull()
    val openLesson = if (!personal && group != null && data != null) {
        data.lessonCards(group.id).firstOrNull { it.id == openLessonId }
    } else null
    val selectGroup: (String) -> Unit = { name ->
        groupId = data?.groups?.firstOrNull { it.name == name }?.id
        openLessonId = null
    }

    BackHandler(enabled = openLessonId != null) { if (!busy) openLessonId = null }
    BackHandler(enabled = openLessonId == null && tab != firstTab) { if (!busy) tab = firstTab }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        LoginBackground(dark = colors.isDark, modifier = Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).imePadding()) {
                val error = loadError
                when {
                    error != null -> Column(Modifier.statusBarsPadding().padding(16.dp)) {
                        Text(error, color = colors.dangerText)
                        Button(onClick = ::refresh, enabled = !busy) { Text("Обновить журнал") }
                        TextButton(onClick = onLogout, enabled = !busy) { Text("Выйти") }
                    }
                    data == null -> Column(Modifier.statusBarsPadding().padding(16.dp)) {
                        Text("Загрузка журнала…", color = colors.title)
                    }
                    openLesson != null && group != null -> AttendanceScreen(
                        lesson = openLesson,
                        students = data.studentCards(group.id),
                        marks = data.marks(openLesson.id),
                        busy = busy,
                        onBack = { if (!busy) openLessonId = null },
                        onSave = { change, done -> submit({ change }, done) },
                    )
                    else -> when (tab) {
                        Tab.Lessons -> if (group != null) {
                            LessonsScreen(data, group, selectGroup, onOpenLesson = { openLessonId = it.id })
                        } else {
                            Column(Modifier.statusBarsPadding().padding(16.dp)) {
                                Text("Нет доступных групп", color = colors.title)
                                if (account.role == Role.Admin) {
                                    TextButton(onClick = { tab = Tab.Manage }) { Text("Открыть группы") }
                                }
                            }
                        }
                        Tab.Stats -> if (group != null) StatsScreen(data, group, selectGroup, personal)
                            else Text("Нет данных о посещаемости", Modifier.statusBarsPadding().padding(16.dp))
                        Tab.Risk -> if (group != null) RiskScreen(data, group, selectGroup, demo = repository.isDemo)
                            else Text("Нет данных для прогноза", Modifier.statusBarsPadding().padding(16.dp))
                        Tab.Manage -> ManageScreen(
                            data, group, account.role, busy, selectGroup,
                            onChange = { change, done -> submit({ change }, done) },
                            onImport = { read -> submit(read) },
                            onExport = { id, write ->
                                perform {
                                    try {
                                        write(withContext(Dispatchers.IO) { repository.export(account, id) })
                                        notify("Отчёт CSV сохранён")
                                    } catch (error: CancellationException) {
                                        throw error
                                    } catch (error: Exception) {
                                        notify(error.userMessage("Не удалось сохранить отчёт CSV"))
                                    }
                                }
                            },
                        )
                        Tab.Profile -> ProfileScreen(account, data, group, repository.label, onLogout = onLogout)
                    }
                }
            }

            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (!WindowInsets.isImeVisible) {
                BottomBar(tabs, tab) {
                    if (!busy) {
                        openLessonId = null
                        tab = it
                    }
                }
            }
        }
        SnackbarHost(
            hostState = messages,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 68.dp),
        )
    }
}

private fun Exception.userMessage(fallback: String): String =
    if (this is JournalException) message ?: fallback else fallback

@Composable
private fun BottomBar(tabs: List<Tab>, selected: Tab, onSelect: (Tab) -> Unit) {
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (colors.isDark) Modifier else Modifier.shadow(12.dp, shape, ambientColor = Color(0x262A5C99), spotColor = Color(0x262A5C99)))
            .clip(shape)
            .background(colors.navBar)
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSelected) colors.soft else Color.Transparent)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelect(tab) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = if (isSelected) tab.selected else tab.unselected,
                    contentDescription = null,
                    tint = if (isSelected) colors.primary else colors.icon,
                    modifier = Modifier.size(26.dp),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = tab.title,
                    color = if (isSelected) colors.primary else colors.muted,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                )
            }
        }
    }
}
