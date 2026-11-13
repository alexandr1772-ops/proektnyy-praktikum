package ru.attendance.tracker.ui.login

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.attendance.tracker.ui.theme.AppColors
import ru.attendance.tracker.ui.theme.AttendanceTheme
import ru.attendance.tracker.ui.theme.LocalAppColors

// Размеры заданы в пикселях макета mobile/mockups/01-login.png (ширина 863) и пересчитываются
// под ширину колонки. Текст задан в sp, поэтому растёт вместе с системным размером шрифта.
private const val MOCKUP_WIDTH = 863f
private const val MOCKUP_HEIGHT = 1822f

// На планшете и в альбомной ориентации форма не растягивается шире этого
private val MaxContentWidth = 440.dp

// Элементы появляются по очереди сверху вниз: каждый следующий через REVEAL_STEP_MS после предыдущего
private const val REVEAL_STEP_MS = 110L
private const val REVEAL_DURATION_MS = 460

/** Размеры в пикселях макета, пересчитанные под текущую ширину колонки. */
private class Metrics(private val k: Float, private val squeeze: Float) {
    /** Размер элемента. */
    fun u(px: Float): Dp = (px * k).dp

    /** Промежуток между блоками: на низком экране сжимается. */
    fun gap(px: Float): Dp = (px * k * squeeze).dp

    /** Размер текста: в sp, то есть с учётом системного размера шрифта. */
    fun sp(px: Float): TextUnit = (px * k).sp
}

@Composable
fun LoginScreen(
    /** Возвращает true, если вход выполнен. При false под полем пароля появляется ошибка. */
    onLogin: (login: String, password: String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    var login by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var loginFailed by rememberSaveable { mutableStateOf(false) }

    val colors = LocalAppColors.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    // Если в системе отключены анимации, всё показывается сразу
    val animate = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }

    fun submit() {
        submitted = true
        if (login.isNotBlank() && password.isNotBlank()) {
            loginFailed = !onLogin(login.trim(), password)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        LoginBackground(dark = colors.isDark, modifier = Modifier.fillMaxSize())

        val contentWidth = minOf(maxWidth, MaxContentWidth)
        val k = contentWidth.value / MOCKUP_WIDTH
        // Если экран ниже макета, промежутки между блоками сжимаются (но не меньше чем до 60%)
        val squeeze = (maxHeight.value / (MOCKUP_HEIGHT * k)).coerceIn(0.6f, 1f)
        val m = Metrics(k, squeeze)
        val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navigationBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(maxOf(m.gap(206f), statusBar + 16.dp)))

            AppLogo(
                dark = colors.isDark,
                modifier = Modifier
                    .size(m.u(230f), m.u(226f))
                    .appear(0, animate, m.u(36f), fromScale = 0.82f),
            )

            Text(
                text = "Трекер посещаемости",
                color = colors.title,
                fontSize = m.sp(57.6f),
                letterSpacing = m.sp(-2.5f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(contentWidth)
                    .padding(horizontal = m.u(30f))
                    .paddingFromBaseline(top = m.u(68f), bottom = 0.dp)
                    .appear(1, animate, m.u(36f)),
            )
            Text(
                text = "Учёба под контролем",
                color = colors.subtitle,
                fontSize = m.sp(35f),
                letterSpacing = m.sp(-1.1f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(contentWidth)
                    .padding(horizontal = m.u(30f))
                    .paddingFromBaseline(top = m.u(54f), bottom = 0.dp)
                    .appear(2, animate, m.u(36f)),
            )

            Spacer(Modifier.height(m.gap(76f)))

            // Иллюстрация на макете сдвинута чуть правее центра
            AnimatedCalendar(
                dark = colors.isDark,
                startDelayMs = if (animate) 3 * REVEAL_STEP_MS else 0L,
                modifier = Modifier
                    .offset(x = m.u(17.5f))
                    .size(m.u(548f), m.u(250f)),
            )

            Column(
                modifier = Modifier
                    .width(contentWidth)
                    .padding(horizontal = m.u(51f)),
            ) {
                FieldLabel(
                    text = "Логин",
                    colors = colors,
                    fontSize = m.sp(32.3f),
                    letterSpacing = m.sp(-2.0f),
                    modifier = Modifier
                        .paddingFromBaseline(top = m.gap(66f), bottom = 0.dp)
                        .appear(4, animate, m.u(36f)),
                )
                Spacer(Modifier.height(m.u(15f)))
                InputField(
                    value = login,
                    onValueChange = { login = it; loginFailed = false },
                    placeholder = "Введите логин",
                    leadingIcon = Icons.Outlined.Person,
                    isError = submitted && login.isBlank() || loginFailed,
                    errorText = "Введите логин",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    ),
                    colors = colors,
                    m = m,
                    modifier = Modifier.appear(5, animate, m.u(36f)),
                )

                FieldLabel(
                    text = "Пароль",
                    colors = colors,
                    fontSize = m.sp(32.3f),
                    letterSpacing = m.sp(-1.4f),
                    modifier = Modifier
                        .paddingFromBaseline(top = m.u(80f), bottom = 0.dp)
                        .appear(6, animate, m.u(36f)),
                )
                Spacer(Modifier.height(m.u(22f)))
                InputField(
                    value = password,
                    onValueChange = { password = it; loginFailed = false },
                    placeholder = "Введите пароль",
                    leadingIcon = Icons.Outlined.Lock,
                    isError = submitted && password.isBlank() || loginFailed,
                    errorText = if (loginFailed) "Неверный логин или пароль" else "Введите пароль",
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    trailingDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль",
                    onTrailingClick = { passwordVisible = !passwordVisible },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            submit()
                        },
                    ),
                    colors = colors,
                    m = m,
                    modifier = Modifier.appear(7, animate, m.u(36f)),
                )

                Spacer(Modifier.height(m.gap(48f)))

                // Кнопка «Войти»
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        submit()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = maxOf(m.u(103f), 48.dp))
                        .appear(8, animate, m.u(36f), fromScale = 0.94f),
                    shape = RoundedCornerShape(m.u(27f)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary,
                    ),
                    contentPadding = PaddingValues(vertical = m.u(16f)),
                ) {
                    Text(
                        text = "Войти",
                        fontSize = m.sp(35f),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Временно: статус бэкенда (GET /health), уберём при появлении настоящего входа
            HealthStatus(modifier = Modifier.width(contentWidth))

            // Запас снизу, чтобы кнопка не лежала на волнах и не пряталась под панелью жестов
            Spacer(Modifier.height(m.gap(140f) + navigationBar))
        }
    }
}

@Composable
private fun FieldLabel(
    text: String,
    colors: AppColors,
    fontSize: TextUnit,
    letterSpacing: TextUnit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = colors.label,
        fontSize = fontSize,
        letterSpacing = letterSpacing,
        fontWeight = FontWeight.Medium,
        modifier = modifier,
    )
}

@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    isError: Boolean,
    errorText: String,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    colors: AppColors,
    m: Metrics,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: ImageVector? = null,
    trailingDescription: String? = null,
    onTrailingClick: (() -> Unit)? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val borderColor = when {
        isError -> colors.error
        focused -> colors.primary
        else -> colors.fieldBorder
    }
    val shape = RoundedCornerShape(m.u(27f))
    val fontSize = m.sp(31.6f)
    val letterSpacing = fontSize * (-1.15f / 31.6f)

    Column(modifier = modifier) {
        // Поле растёт по высоте, если в системе включён крупный шрифт
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = maxOf(m.u(103f), 48.dp))
                .background(colors.fieldFill, shape)
                .border(m.u(2.2f), borderColor, shape)
                .padding(start = m.u(34.5f), top = m.u(14f), bottom = m.u(14f)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = colors.icon,
                modifier = Modifier.size(m.u(50f)),
            )
            Spacer(Modifier.width(m.u(31.5f)))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = colors.placeholder,
                        fontSize = fontSize,
                        letterSpacing = letterSpacing,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focused = it.isFocused },
                    textStyle = TextStyle(color = colors.input, fontSize = fontSize, letterSpacing = letterSpacing),
                    singleLine = true,
                    cursorBrush = SolidColor(colors.primary),
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                )
            }

            if (trailingIcon != null && onTrailingClick != null) {
                Box(
                    modifier = Modifier
                        // Глазок не берёт фокус, чтобы «Далее» на клавиатуре вело из логина прямо в пароль
                        .focusProperties { canFocus = false }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onTrailingClick,
                        )
                        .minimumInteractiveComponentSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = trailingDescription,
                        tint = colors.icon,
                        modifier = Modifier.size(m.u(50f)),
                    )
                }
            } else {
                Spacer(Modifier.width(m.u(30f)))
            }
        }

        if (isError) {
            Text(
                text = errorText,
                color = colors.error,
                fontSize = m.sp(26f),
                modifier = Modifier.padding(start = m.u(19f), top = m.u(10f)),
            )
        }
    }
}

/**
 * Элемент плавно появляется: проявляется, всплывает снизу на [slide] и при необходимости растёт из [fromScale].
 * Порядок [order] задаёт задержку: элементы идут по очереди сверху вниз.
 */
@Composable
private fun Modifier.appear(order: Int, enabled: Boolean, slide: Dp, fromScale: Float = 1f): Modifier {
    val progress = remember { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(enabled) {
        if (!enabled) return@LaunchedEffect
        delay(order * REVEAL_STEP_MS)
        progress.animateTo(1f, tween(REVEAL_DURATION_MS, easing = FastOutSlowInEasing))
    }
    val slidePx = with(LocalDensity.current) { slide.toPx() }
    return graphicsLayer {
        val p = progress.value
        alpha = p
        translationY = (1f - p) * slidePx
        val scale = fromScale + (1f - fromScale) * p
        scaleX = scale
        scaleY = scale
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 823)
@Composable
private fun LoginScreenPreview() {
    AttendanceTheme(darkTheme = false) {
        LoginScreen(onLogin = { _, _ -> false })
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 823)
@Composable
private fun LoginScreenDarkPreview() {
    AttendanceTheme(darkTheme = true) {
        LoginScreen(onLogin = { _, _ -> false })
    }
}
