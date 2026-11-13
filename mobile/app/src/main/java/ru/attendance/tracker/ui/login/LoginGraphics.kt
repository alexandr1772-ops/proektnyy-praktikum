package ru.attendance.tracker.ui.login

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.min

private const val MOCKUP_WIDTH = 863f
private const val LOGO_WIDTH = 230f
private const val LOGO_HEIGHT = 226f

/** Логотип: синий квадрат со скруглёнными углами и шапочкой выпускника. */
@Composable
fun AppLogo(dark: Boolean, modifier: Modifier = Modifier) {
    val painter = remember(dark) { ArtPainter(LogoShapes, if (dark) LogoDark else LogoLight) }
    Canvas(modifier = modifier) {
        drawIntoCanvas { canvas ->
            val nc = canvas.nativeCanvas
            nc.save()
            nc.scale(size.width / LOGO_WIDTH, size.height / LOGO_HEIGHT)
            painter.draw(nc)
            nc.restore()
        }
    }
}

/** Фон экрана входа: пятно в левом верхнем углу и волны внизу. */
@Composable
fun LoginBackground(dark: Boolean, modifier: Modifier = Modifier) {
    val palette = if (dark) WavesDark else WavesLight
    val top = remember(dark) { ArtPainter(WaveTop, palette) }
    val bottom = remember(dark) { ArtPainter(WaveBottom, palette) }

    Canvas(modifier = modifier) {
        drawRect(Color(palette[0]))
        val byWidth = size.width / MOCKUP_WIDTH
        drawIntoCanvas { canvas ->
            val nc = canvas.nativeCanvas

            // На широком экране (планшет, альбомная ориентация) пятно не растёт выше 45% высоты
            val topScale = min(byWidth, size.height * 0.45f / WAVE_TOP_HEIGHT)
            nc.save()
            nc.scale(topScale, topScale)
            top.draw(nc)
            nc.restore()

            // Волны тянутся по ширине, а по высоте занимают не больше 30% экрана
            val bottomScaleY = min(byWidth, size.height * 0.3f / WAVE_BOTTOM_HEIGHT)
            nc.save()
            nc.translate(0f, size.height - WAVE_BOTTOM_HEIGHT * bottomScaleY)
            nc.scale(byWidth, bottomScaleY)
            bottom.draw(nc)
            nc.restore()
        }
    }
}
