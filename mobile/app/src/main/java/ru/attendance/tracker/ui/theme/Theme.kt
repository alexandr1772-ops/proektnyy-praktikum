package ru.attendance.tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Цвета экранов приложения. Светлые сняты с макетов в mobile/mockups, тёмные подобраны под них. */
@Immutable
data class AppColors(
    val isDark: Boolean,
    val background: Color,
    val title: Color,
    val subtitle: Color,
    val label: Color,
    val muted: Color,
    val placeholder: Color,
    val input: Color,
    val icon: Color,
    val fieldFill: Color,
    val fieldBorder: Color,
    val primary: Color,
    val onPrimary: Color,
    val error: Color,
    // Карточки и вспомогательные поверхности
    val card: Color,
    val cardBorder: Color,
    val soft: Color,
    val track: Color,
    val divider: Color,
    val navBar: Color,
    val chipOff: Color,
    // Статусы
    val success: Color,
    val successSoft: Color,
    val successText: Color,
    val warning: Color,
    val warningSoft: Color,
    val warningText: Color,
    val danger: Color,
    val dangerSoft: Color,
    val dangerText: Color,
    val orange: Color,
    val orangeSoft: Color,
)

val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFF7FBFE),
    title = Color(0xFF151D33),
    subtitle = Color(0xFF3C4354),
    label = Color(0xFF393D4A),
    muted = Color(0xFF6B7686),
    placeholder = Color(0xFF9197A1),
    input = Color(0xFF151D33),
    icon = Color(0xFF5C687A),
    fieldFill = Color(0xFFFBFDFE),
    fieldBorder = Color(0xFFCDD4DD),
    primary = Color(0xFF2A8BEC),
    onPrimary = Color.White,
    error = Color(0xFFDC2626),
    card = Color.White,
    cardBorder = Color(0xFFE6EDF5),
    soft = Color(0xFFE8F2FD),
    track = Color(0xFFE6EBF1),
    divider = Color(0xFFEDF1F6),
    navBar = Color.White,
    chipOff = Color(0xFFF1F4F8),
    success = Color(0xFF1FA971),
    successSoft = Color(0xFFE3F6EC),
    successText = Color(0xFF15855A),
    warning = Color(0xFFEBB020),
    warningSoft = Color(0xFFFFF3D6),
    warningText = Color(0xFFA9730A),
    danger = Color(0xFFE5484D),
    dangerSoft = Color(0xFFFDE8E8),
    dangerText = Color(0xFFD0252B),
    orange = Color(0xFFFF7A1A),
    orangeSoft = Color(0xFFFFEBD9),
)

val DarkAppColors = AppColors(
    isDark = true,
    background = Color(0xFF0E1522),
    title = Color(0xFFE8EEF7),
    subtitle = Color(0xFF9AA8BC),
    label = Color(0xFFC2CCDA),
    muted = Color(0xFF8F9DB2),
    placeholder = Color(0xFF6E7C91),
    input = Color(0xFFE8EEF7),
    icon = Color(0xFF8D9BB0),
    fieldFill = Color(0xFF152034),
    fieldBorder = Color(0xFF2B3A52),
    primary = Color(0xFF3A8FEA),
    onPrimary = Color.White,
    error = Color(0xFFF87171),
    card = Color(0xFF152034),
    cardBorder = Color(0xFF22304A),
    soft = Color(0xFF16283F),
    track = Color(0xFF26334A),
    divider = Color(0xFF1F2B40),
    navBar = Color(0xFF101A2B),
    chipOff = Color(0xFF1C2A42),
    success = Color(0xFF34C98B),
    successSoft = Color(0xFF12332A),
    successText = Color(0xFF5FDBA9),
    warning = Color(0xFFF2B94A),
    warningSoft = Color(0xFF3A2F12),
    warningText = Color(0xFFF5C969),
    danger = Color(0xFFF87171),
    dangerSoft = Color(0xFF3A1B1F),
    dangerText = Color(0xFFFF9A9A),
    orange = Color(0xFFFF9A52),
    orangeSoft = Color(0xFF3B2616),
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

@Composable
fun AttendanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkAppColors else LightAppColors
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            background = colors.background,
            surface = colors.card,
            onSurface = colors.title,
            error = colors.error,
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            background = colors.background,
            surface = Color.White,
            onSurface = colors.title,
            error = colors.error,
        )
    }
    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
