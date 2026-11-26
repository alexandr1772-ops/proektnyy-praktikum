package ru.attendance.tracker

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.attendance.tracker.data.Account
import ru.attendance.tracker.data.AttendanceRepository
import ru.attendance.tracker.data.openRepository
import ru.attendance.tracker.ui.login.LoginScreen
import ru.attendance.tracker.ui.main.MainScreen
import ru.attendance.tracker.ui.theme.AttendanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        val session = ViewModelProvider(this)[JournalSession::class.java]
        setContent {
            AttendanceTheme {
                val source by produceState<Result<AttendanceRepository>?>(null) {
                    value = runCatching { openRepository(applicationContext) }
                }
                val repository = source?.getOrNull()
                val signedIn = session.account
                when {
                    repository == null -> Text(
                        if (source == null) "Загрузка…" else "Не удалось открыть данные приложения",
                        modifier = Modifier.statusBarsPadding().padding(16.dp),
                    )
                    signedIn == null -> LoginScreen(
                        modeHint = if (BuildConfig.ATTENDANCE_SOURCE == "demo") {
                            "Демо: admin, teacher или student. Пароль: 1234"
                        } else "Сервер: ${BuildConfig.ATTENDANCE_API_URL}",
                        onLogin = { login, password -> session.account = repository.login(login, password) },
                    )
                    else -> MainScreen(repository, signedIn, onLogout = { session.account = null })
                }
            }
        }
    }
}

internal class JournalSession : ViewModel() {
    var account by mutableStateOf<Account?>(null)
}
