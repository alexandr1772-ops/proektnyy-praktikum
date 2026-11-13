package ru.attendance.tracker.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.attendance.tracker.data.Risk
import ru.attendance.tracker.ui.theme.LocalAppColors

val ScreenPadding = 20.dp

/** Белая карточка с тонкой рамкой и мягкой тенью (в тёмной теме без тени). */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .then(
                if (colors.isDark) {
                    Modifier
                } else {
                    Modifier.shadow(
                        elevation = 6.dp,
                        shape = shape,
                        ambientColor = Color(0x1A2A5C99),
                        spotColor = Color(0x1A2A5C99),
                    )
                },
            )
            .clip(shape)
            .background(colors.card)
            .border(BorderStroke(1.dp, colors.cardBorder), shape)
            .padding(contentPadding),
        content = content,
    )
}

/** Заголовок экрана: крупное название, подпись и элементы справа. Сам учитывает строку состояния. */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = ScreenPadding, end = ScreenPadding, top = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                color = colors.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            trailing?.invoke()
        }
        Spacer(Modifier.height(2.dp))
        Text(text = subtitle, color = colors.muted, fontSize = 16.sp)
    }
}

/** Выбор группы: «ИС-21 ˅». Открывает список групп. */
@Composable
fun GroupChip(group: String, groups: List<String>, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    var open by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(colors.card)
                .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
                .clickable { open = true }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Groups, contentDescription = null, tint = colors.icon, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(group, color = colors.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = colors.icon, modifier = Modifier.size(20.dp))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            groups.forEach { item ->
                DropdownMenuItem(text = { Text(item) }, onClick = { onSelect(item); open = false })
            }
        }
    }
}

/** Маленькая «таблетка» с выпадающим списком, например «По посещаемости ˅». */
@Composable
fun OptionChip(selected: String, options: List<String>, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    var open by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colors.soft)
                .clickable { open = true }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(selected, color = colors.title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = colors.icon, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { item ->
                DropdownMenuItem(text = { Text(item) }, onClick = { onSelect(item); open = false })
            }
        }
    }
}

/** Круглый аватар с инициалами. */
@Composable
fun Avatar(initials: String, background: Color, foreground: Color, size: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, color = foreground, fontSize = (size.value * 0.34f).sp, fontWeight = FontWeight.Bold)
    }
}

/** Цвета аватара для студента без риска: чередуются по номеру. */
@Composable
fun avatarColors(id: Int, risk: Risk = Risk.Low): Pair<Color, Color> {
    val colors = LocalAppColors.current
    return when (risk) {
        Risk.High -> colors.dangerSoft to colors.dangerText
        Risk.Medium -> colors.warningSoft to colors.warningText
        Risk.Low -> when (id % 4) {
            0 -> colors.successSoft to colors.successText
            1 -> colors.soft to colors.primary
            2 -> colors.soft to colors.primary
            else -> (if (colors.isDark) Color(0xFF211F3F) else Color(0xFFEDEBFF)) to
                (if (colors.isDark) Color(0xFFA9A5FF) else Color(0xFF5B52D6))
        }
    }
}

/** Горизонтальная полоска прогресса. */
@Composable
fun ProgressBar(fraction: Float, color: Color, modifier: Modifier = Modifier, height: Dp = 8.dp) {
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(height)
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(colors.track),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(height)
                .clip(shape)
                .background(color),
        )
    }
}

/** Цветная метка: «Высокий», «Низкий» и т. п. */
@Composable
fun Badge(text: String, foreground: Color, background: Color) {
    Text(
        text = text,
        color = foreground,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** Цвет процента посещаемости: зелёный, жёлтый или красный. */
@Composable
fun percentColor(percent: Int): Color {
    val colors = LocalAppColors.current
    return when {
        percent >= 80 -> colors.successText
        percent >= 60 -> colors.warningText
        else -> colors.dangerText
    }
}

/** Цвет заполнения полоски по проценту. */
@Composable
fun percentBarColor(percent: Int): Color {
    val colors = LocalAppColors.current
    return when {
        percent >= 80 -> colors.success
        percent >= 60 -> colors.warning
        else -> colors.danger
    }
}

/** Нажимаемый фон без «волны» (для строк списка). */
@Composable
fun Modifier.plainClickable(onClick: () -> Unit): Modifier =
    clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)

/** Пустой блок-обёртка для центрирования содержимого. */
@Composable
fun CenterBox(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = modifier, contentAlignment = Alignment.Center, content = content)
}
