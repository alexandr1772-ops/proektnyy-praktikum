package ru.attendance.tracker.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.GppMaybe
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.delay
import ru.attendance.tracker.data.AttendanceStore
import ru.attendance.tracker.data.DemoData
import ru.attendance.tracker.data.Lesson
import ru.attendance.tracker.ui.attendance.AttendanceScreen
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
    Profile("Профиль", Icons.Filled.Person, Icons.Outlined.Person),
}

/** Главный экран после входа: четыре вкладки и отметка посещаемости внутри «Занятий». */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(onLogout: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    val store = remember { AttendanceStore() }
    var tab by rememberSaveable { mutableStateOf(Tab.Lessons) }
    var group by rememberSaveable { mutableStateOf(DemoData.groups.first()) }
    var openLessonId by rememberSaveable { mutableStateOf<Int?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }
    val openLesson: Lesson? = DemoData.lessons.firstOrNull { it.id == openLessonId }

    // «Назад» закрывает отметку, а с других вкладок возвращает на «Занятия»
    BackHandler(enabled = openLesson != null) { openLessonId = null }
    BackHandler(enabled = openLesson == null && tab != Tab.Lessons) { tab = Tab.Lessons }

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(2200)
            toast = null
        }
    }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        LoginBackground(dark = colors.isDark, modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).imePadding()) {
                if (openLesson != null) {
                    AttendanceScreen(
                        lesson = openLesson,
                        store = store,
                        onBack = { openLessonId = null },
                        onSaved = {
                            openLessonId = null
                            toast = "Отметки сохранены"
                        },
                    )
                } else {
                    Crossfade(targetState = tab, label = "tab") { current ->
                        when (current) {
                            Tab.Lessons -> LessonsScreen(
                                store = store,
                                group = group,
                                onGroupChange = { group = it },
                                onOpenLesson = { openLessonId = it.id },
                            )
                            Tab.Stats -> StatsScreen(group, { group = it })
                            Tab.Risk -> RiskScreen(group, { group = it })
                            Tab.Profile -> ProfileScreen(group, onLogout)
                        }
                    }
                }
            }

            // Когда открыта клавиатура, нижняя панель уступает ей место
            if (!WindowInsets.isImeVisible) {
                BottomBar(
                    selected = tab,
                    onSelect = {
                        openLessonId = null
                        tab = it
                    },
                )
            }
        }

        AnimatedVisibility(
            visible = toast != null,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 110.dp),
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
        ) {
            Text(
                text = toast ?: "",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xE61B2538))
                    .padding(horizontal = 18.dp, vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun BottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
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
        Tab.entries.forEach { tab ->
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
