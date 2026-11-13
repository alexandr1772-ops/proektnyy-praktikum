package ru.attendance.tracker.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.attendance.tracker.data.DemoData
import ru.attendance.tracker.ui.common.AppCard
import ru.attendance.tracker.ui.common.Avatar
import ru.attendance.tracker.ui.common.Badge
import ru.attendance.tracker.ui.common.ScreenHeader
import ru.attendance.tracker.ui.common.ScreenPadding
import ru.attendance.tracker.ui.theme.LocalAppColors

@Composable
fun ProfileScreen(group: String, onLogout: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = "Профиль", subtitle = "Учётная запись")
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar("А", colors.soft, colors.primary, size = 64.dp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Администратор", color = colors.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(DemoData.DEMO_LOGIN, color = colors.muted, fontSize = 14.sp)
                        Spacer(Modifier.height(6.dp))
                        Badge("Демо-режим", colors.primary, colors.soft)
                    }
                }
            }

            AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 4.dp) {
                InfoRow("Группа", group)
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                InfoRow("Занятий сегодня", "${DemoData.lessons.size}")
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                InfoRow("Студентов в группе", "${DemoData.students.size}")
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                InfoRow("Версия приложения", "0.1.0")
            }

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, colors.danger),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.dangerText),
            ) {
                Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text("Выйти", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = colors.muted, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Text(value, color = colors.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}
