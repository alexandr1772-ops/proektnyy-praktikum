package ru.attendance.tracker.ui.login

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.attendance.tracker.api.fetchHealth
import ru.attendance.tracker.ui.theme.LocalAppColors

/** Строка статуса сервера: проверяет GET /health при открытии экрана. Временная, пока нет настоящего входа. */
@Composable
fun HealthStatus(modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    var status by remember { mutableStateOf("Сервер: проверяем…") }
    var ok by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        status = try {
            val body = fetchHealth()
            ok = body.contains("\"ok\"")
            if (ok) "Сервер: ok" else "Сервер ответил: $body"
        } catch (e: Exception) {
            "Нет связи с сервером"
        }
    }

    Text(
        text = status,
        color = if (ok) colors.primary else colors.error,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    )
}
