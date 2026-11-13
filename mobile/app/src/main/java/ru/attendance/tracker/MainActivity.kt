package ru.attendance.tracker

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import ru.attendance.tracker.data.DemoData
import ru.attendance.tracker.ui.login.LoginScreen
import ru.attendance.tracker.ui.main.MainScreen
import ru.attendance.tracker.ui.theme.AttendanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Экран рисуется под системными панелями, как на макете; цвет значков панелей следует теме
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent {
            AttendanceTheme {
                var loggedIn by rememberSaveable { mutableStateOf(false) }
                if (loggedIn) {
                    MainScreen(onLogout = { loggedIn = false })
                } else {
                    LoginScreen(
                        // ВРЕМЕННО: вход для просмотра дизайна (admin / 1234). Настоящую проверку сделает сервер.
                        onLogin = { login, password ->
                            val ok = login.equals(DemoData.DEMO_LOGIN, ignoreCase = true) && password == DemoData.DEMO_PASSWORD
                            if (ok) loggedIn = true
                            ok
                        },
                    )
                }
            }
        }
    }
}
