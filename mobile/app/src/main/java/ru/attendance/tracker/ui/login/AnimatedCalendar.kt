package ru.attendance.tracker.ui.login

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

// Все координаты ниже — в пикселях иллюстрации 548 x 250 (см. LoginArt.kt).
private const val ILLUSTRATION_WIDTH = 548f

// Лист календаря. Верхний край листа — линия сгиба под шапкой, она идёт с небольшим наклоном.
private const val PAGE_LEFT = 120f
private const val PAGE_WIDTH = 191f
private const val PAGE_HEIGHT = 162f
private const val HINGE_Y_LEFT = 72.5f
private const val HINGE_SLOPE = -10f / 186f

private fun hingeY(x: Float) = HINGE_Y_LEFT + HINGE_SLOPE * (x - PAGE_LEFT)

// Верхний край шапки. Часть листа, ушедшая за календарь, видна только выше этой линии.
private fun headerTopY(x: Float) = 31f - 0.0415f * (x - 111f)

// Передняя сторона календаря (шапка и лист): на неё падает тень от поднятого листа
private val CalendarFront = floatArrayOf(111f, 31f, 304f, 23f, 311f, 224f, 141f, 233f)

// Сетка 3 x 4 с перспективой: центры клеток и скос, под который рисуются отметки
private fun cellX(row: Int, col: Int) = 161.2f + 38.2f * col + 4.85f * row
private fun cellY(row: Int, col: Int) = 106.4f - 2.15f * col + 41.5f * row
private const val SHEAR_X = 0.117f
private const val SHEAR_Y = -0.056f
private const val CELL_HALF = 15.5f

private class Mark(val row: Int, val col: Int, val teal: Boolean)

// «Дни»: какие клетки отмечены на каждом листе. Первый — как на макете.
private val Days = listOf(
    listOf(Mark(2, 1, false), Mark(1, 2, false), Mark(0, 3, true)),
    listOf(Mark(2, 3, false), Mark(1, 1, false), Mark(0, 0, true)),
    listOf(Mark(2, 2, false), Mark(0, 1, false), Mark(1, 3, true)),
    listOf(Mark(2, 0, false), Mark(1, 2, false), Mark(0, 2, true)),
)

// Как лист перелистывается во времени (t от 0 до 1):
// угол у линии сгиба, общий изгиб листа и насколько правый нижний угол опережает левый.
private val KeyT = floatArrayOf(0f, 0.18f, 0.42f, 0.62f, 1f)
private val KeyAngle = floatArrayOf(0f, 0.12f, 1.55f, 3.30f, 5.40f)
private val KeyBend = floatArrayOf(0f, 1.10f, 3.00f, 2.40f, 0.60f)
private val KeyTwist = floatArrayOf(0f, 0.70f, 0.35f, 0.10f, 0f)

// Сетка, из которой собирается изогнутый лист: NY полос по высоте, в каждой NX кусочков
private const val NX = 12
private const val NY = 18

// Камера и свет
private const val CAMERA = 650f
private const val CAMERA_X = 215f
private const val CAMERA_Y = 130f
private val Light = normalize(-0.25f, -0.6f, 0.76f)

private const val MAX_FLIPS = 3
private const val PAUSE_BETWEEN_FLIPS_MS = 6000L

/**
 * Календарь на экране входа. При появлении пролистываются три листа («проходят дни»),
 * на новом листе по очереди ставятся отметки. Дальше календарь стоит на месте,
 * бирюзовая клетка «сегодня» пульсирует, а раз в несколько секунд перелистывается следующий день.
 */
@Composable
fun AnimatedCalendar(modifier: Modifier = Modifier, dark: Boolean = false, startDelayMs: Long = 0L) {
    val context = LocalContext.current
    // Если в системе отключены анимации, показываем готовую картинку без движения
    val animationsEnabled = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
    val art = remember(dark) { CalendarArt(if (dark) IllustrationDark else IllustrationLight) }
    val renderer = remember { PageRenderer() }

    val entrance = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val stack = remember { mutableStateListOf<Int>() }
    val flips = remember { mutableStateListOf<FlipItem>() }
    var liveDay by remember { mutableIntStateOf(0) }
    val marks = remember { List(3) { Animatable(if (animationsEnabled) 0f else 1f) } }

    LaunchedEffect(animationsEnabled) {
        if (!animationsEnabled) return@LaunchedEffect

        // Лист попадает в список летящих в том же кадре, в котором уходит со стопки, — без мигания
        fun startFlip(day: Int) = FlipItem(day).also { flips.add(it) }

        suspend fun runFlip(item: FlipItem, durationMs: Int) {
            animate(0f, 1f, animationSpec = tween(durationMs, easing = LinearEasing)) { value, _ ->
                item.progress = value
            }
            flips.remove(item)
        }

        suspend fun placeMarks() = coroutineScope {
            marks.forEachIndexed { i, mark ->
                launch {
                    delay(i * 240L)
                    mark.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 320f))
                }
            }
        }

        // Стопка из трёх «прошедших» дней поверх сегодняшнего листа
        stack.addAll(listOf(1, 2, 3))
        delay(startDelayMs)
        entrance.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        delay(150)

        // Листаем стопку, каждый следующий лист чуть быстрее; новый начинается, пока предыдущий ещё в воздухе
        coroutineScope {
            for (duration in listOf(1050, 900, 800)) {
                val item = startFlip(stack.removeAt(stack.lastIndex))
                launch { runFlip(item, duration) }
                delay((duration * 0.42f).toLong())
            }
        }
        placeMarks()

        var day = 0
        while (true) {
            delay(PAUSE_BETWEEN_FLIPS_MS)
            val next = (day + 1) % Days.size
            // Под перелистываемым листом уже лежит чистый следующий день
            marks.forEach { it.snapTo(0f) }
            liveDay = next
            runFlip(startFlip(day), 1000)
            placeMarks()
            day = next
        }
    }

    val transition = rememberInfiniteTransition(label = "calendar")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )

    Canvas(
        modifier = modifier.graphicsLayer {
            alpha = entrance.value
            val scale = 0.92f + 0.08f * entrance.value
            scaleX = scale
            scaleY = scale
        },
    ) {
        val sc = size.width / ILLUSTRATION_WIDTH
        drawIntoCanvas { composeCanvas ->
            val canvas = composeCanvas.nativeCanvas

            canvas.save()
            canvas.scale(sc, sc)
            // 1. Фон, листья, стенка и шапка календаря, кольца
            art.painter.draw(canvas, ArtLayer.Back)
            art.painter.draw(canvas, ArtLayer.Calendar)
            art.painter.draw(canvas, ArtLayer.Rings)

            // 2. Сегодняшний лист и отметки на нём
            art.painter.draw(canvas, ArtLayer.Page)
            Days[liveDay].forEachIndexed { i, mark ->
                val pulseScale = if (mark.teal && animationsEnabled) 1f + 0.05f * pulse else 1f
                art.drawMark(canvas, mark, marks[i].value, pulseScale)
            }

            // 3. Листы, которые ещё лежат сверху
            for (day in stack) art.drawPage(canvas, day)

            // 4. Книги стоят перед календарём
            art.painter.draw(canvas, ArtLayer.Books)
            canvas.restore()

            // 5. Перелистываемые листы: тень, часть за календарём, кольца, часть перед календарём
            if (flips.isNotEmpty()) renderer.draw(canvas, flips, art, sc)
        }
    }
}

private class FlipItem(val day: Int) {
    var progress by mutableFloatStateOf(0f)
}

/** Векторная графика календаря, отметки и текстуры листа (лицевая и обратная сторона) для каждого дня. */
private class CalendarArt(private val palette: IntArray) {
    val painter = ArtPainter(IllustrationShapes, palette)

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = palette[ArtColor.CHECK]
        style = Paint.Style.STROKE
        strokeWidth = 3.4f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val shear = Matrix().apply {
        setValues(floatArrayOf(1f, SHEAR_X, 0f, SHEAR_Y, 1f, 0f, 0f, 0f, 1f))
    }
    private val checkPath = android.graphics.Path().apply {
        moveTo(-7.5f, 1.5f)
        lineTo(-2f, 7f)
        lineTo(10f, -7f)
    }
    private val checkMeasure = android.graphics.PathMeasure(checkPath, false)
    private val partialCheck = android.graphics.Path()

    // Текстуры строятся под текущий масштаб экрана, чтобы лист в полёте был таким же чётким
    private var textureScale = 0f
    private val frontStrips = arrayOfNulls<Array<Bitmap>>(Days.size)
    private val backStrips = arrayOfNulls<Array<Bitmap>>(Days.size)

    /** Отметка в клетке: клетка вырастает с пружинкой, затем «дорисовывается» галочка. Холст — в координатах иллюстрации. */
    fun drawMark(canvas: android.graphics.Canvas, mark: Mark, progress: Float, pulse: Float) {
        if (progress <= 0.001f) return
        val s = progress * pulse
        canvas.save()
        canvas.translate(cellX(mark.row, mark.col), cellY(mark.row, mark.col))
        canvas.concat(shear)
        canvas.scale(s, s)

        fillPaint.color = palette[if (mark.teal) ArtColor.MARK_TEAL else ArtColor.MARK_DARK]
        canvas.drawRoundRect(-CELL_HALF, -CELL_HALF, CELL_HALF, CELL_HALF, 5f, 5f, fillPaint)

        val drawn = ((progress - 0.35f) / 0.65f).coerceIn(0f, 1f)
        if (drawn > 0f) {
            partialCheck.reset()
            checkMeasure.getSegment(0f, checkMeasure.length * drawn, partialCheck, true)
            canvas.drawPath(partialCheck, checkPaint)
        }
        canvas.restore()
    }

    /** Лист целиком: белая страница, клетки и отметки этого дня. Холст — в координатах иллюстрации. */
    fun drawPage(canvas: android.graphics.Canvas, day: Int) {
        painter.draw(canvas, ArtLayer.Page)
        for (mark in Days[day]) drawMark(canvas, mark, 1f, 1f)
    }

    /** Лист, нарезанный на горизонтальные полосы: каждая полоса изгибается отдельно. */
    fun strips(day: Int, front: Boolean, scale: Float): Array<Bitmap> {
        if (scale != textureScale) {
            frontStrips.fill(null)
            backStrips.fill(null)
            textureScale = scale
        }
        val cache = if (front) frontStrips else backStrips
        return cache[day] ?: run {
            val full = if (front) createFront(day, scale) else createBack(createFront(day, scale))
            val h = full.height / NY
            Array(NY) { j -> Bitmap.createBitmap(full, 0, j * h, full.width, h) }
        }.also { cache[day] = it }
    }

    /**
     * Лицевая сторона: лист «распрямлён» относительно наклонной линии сгиба,
     * чтобы строка текстуры соответствовала расстоянию от сгиба.
     */
    private fun createFront(day: Int, scale: Float): Bitmap {
        val width = ceil(PAGE_WIDTH * scale).toInt()
        val height = ceil(PAGE_HEIGHT * scale / NY).toInt() * NY
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.scale(width / PAGE_WIDTH, height / PAGE_HEIGHT)
        canvas.concat(Matrix().apply {
            setValues(
                floatArrayOf(
                    1f, 0f, -PAGE_LEFT,
                    -HINGE_SLOPE, 1f, -HINGE_Y_LEFT + HINGE_SLOPE * PAGE_LEFT,
                    0f, 0f, 1f,
                ),
            )
        })
        drawPage(canvas, day)
        return bitmap
    }

    /** Обратная сторона: бумага чуть темнее, сквозь неё еле просвечивает лицевая сторона. */
    private fun createBack(front: Bitmap): Bitmap {
        val bitmap = Bitmap.createBitmap(front.width, front.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawBitmap(front, 0f, 0f, Paint().apply {
            colorFilter = PorterDuffColorFilter(palette[ArtColor.PAPER_BACK], PorterDuff.Mode.SRC_IN)
        })
        canvas.drawBitmap(front, 0f, 0f, Paint().apply { alpha = 20 })
        return bitmap
    }
}

/** Строит изогнутый лист из полос с перспективой и рисует его с тенью и освещением. */
private class PageRenderer {
    private val vertexCount = (NX + 1) * (NY + 1)

    // Несколько листов могут быть в воздухе одновременно
    private val px = Array(MAX_FLIPS) { FloatArray(vertexCount) }
    private val py = Array(MAX_FLIPS) { FloatArray(vertexCount) }
    private val pz = Array(MAX_FLIPS) { FloatArray(vertexCount) }
    private val pa = Array(MAX_FLIPS) { FloatArray(vertexCount) }
    private val sx = Array(MAX_FLIPS) { FloatArray(vertexCount) }
    private val sy = Array(MAX_FLIPS) { FloatArray(vertexCount) }

    private val stripKeys = LongArray(MAX_FLIPS * NY)
    private val meshVerts = FloatArray((NX + 1) * 2 * 2)
    private val meshColors = IntArray((NX + 1) * 2)
    private val meshPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

    private val shadowFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF0B2545.toInt() }
    private val shadowEdge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF0B2545.toInt()
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
    }
    private val shadowPath = android.graphics.Path()
    private val frontClip = android.graphics.Path()
    private val aboveHeaderClip = android.graphics.Path()

    fun draw(canvas: android.graphics.Canvas, flips: List<FlipItem>, art: CalendarArt, sc: Float) {
        val count = minOf(flips.size, MAX_FLIPS)
        for (k in 0 until count) buildSheet(k, flips[k].progress)

        buildClips(sc)
        for (k in 0 until count) drawShadow(canvas, k, sc)

        // Полосы, ушедшие за календарь: видны только над шапкой, а кольца остаются перед ними
        drawStrips(canvas, flips, art, count, sc, behind = true)
        canvas.save()
        canvas.scale(sc, sc)
        art.painter.draw(canvas, ArtLayer.Rings)
        canvas.restore()
        drawStrips(canvas, flips, art, count, sc, behind = false)
    }

    /** Форма листа в момент t: каждая точка листа лежит на дуге, которая выходит из линии сгиба. */
    private fun buildSheet(k: Int, t: Float) {
        val angle = spline(KeyAngle, t)
        val bend = spline(KeyBend, t)
        val twist = spline(KeyTwist, t)

        for (j in 0..NY) {
            val s = PAGE_HEIGHT * j / NY
            for (i in 0..NX) {
                val u = i.toFloat() / NX
                // Правый нижний угол отлипает первым
                val side = (u - 0.5f) * 2f
                val a = angle + 0.12f * twist * side
                val b = (bend + twist * side) / PAGE_HEIGHT

                val down: Float
                val toward: Float
                if (abs(b) < 1e-5f) {
                    down = s * cos(a)
                    toward = s * sin(a)
                } else {
                    down = (sin(a + b * s) - sin(a)) / b
                    toward = (cos(a) - cos(a + b * s)) / b
                }

                val v = j * (NX + 1) + i
                val x = PAGE_LEFT + u * PAGE_WIDTH
                val y = hingeY(x) + down
                px[k][v] = x
                py[k][v] = y
                pz[k][v] = toward
                pa[k][v] = a + b * s

                val f = CAMERA / (CAMERA - toward)
                sx[k][v] = CAMERA_X + (x - CAMERA_X) * f
                sy[k][v] = CAMERA_Y + (y - CAMERA_Y) * f
            }
        }
    }

    private fun buildClips(sc: Float) {
        frontClip.reset()
        frontClip.moveTo(CalendarFront[0] * sc, CalendarFront[1] * sc)
        for (p in 1 until 4) frontClip.lineTo(CalendarFront[p * 2] * sc, CalendarFront[p * 2 + 1] * sc)
        frontClip.close()

        aboveHeaderClip.reset()
        aboveHeaderClip.moveTo(0f, -1000f)
        aboveHeaderClip.lineTo(ILLUSTRATION_WIDTH * sc, -1000f)
        aboveHeaderClip.lineTo(ILLUSTRATION_WIDTH * sc, headerTopY(ILLUSTRATION_WIDTH) * sc)
        aboveHeaderClip.lineTo(0f, headerTopY(0f) * sc)
        aboveHeaderClip.close()
    }

    /** Тень поднятого листа на нижний лист и шапку: свет сверху-слева, тень уходит вниз-вправо. */
    private fun drawShadow(canvas: android.graphics.Canvas, k: Int, sc: Float) {
        var maxZ = 0f
        for (v in 0 until vertexCount) maxZ = max(maxZ, pz[k][v])
        val strength = (maxZ / 60f).coerceIn(0f, 1f) * 0.16f
        if (strength < 0.01f) return

        val ox = -Light[0] / Light[2]
        val oy = -Light[1] / Light[2]
        shadowPath.reset()
        var first = true
        fun add(v: Int) {
            val z = max(0f, pz[k][v])
            val x = (px[k][v] + z * ox) * sc
            val y = (py[k][v] + z * oy) * sc
            if (first) shadowPath.moveTo(x, y) else shadowPath.lineTo(x, y)
            first = false
        }
        // Обход контура листа: левый край вниз, нижний вправо, правый вверх, верхний влево
        for (j in 0..NY) add(j * (NX + 1))
        for (i in 1..NX) add(NY * (NX + 1) + i)
        for (j in NY - 1 downTo 0) add(j * (NX + 1) + NX)
        for (i in NX - 1 downTo 1) add(i)
        shadowPath.close()

        canvas.save()
        canvas.clipPath(frontClip)
        // Мягкий край: несколько полупрозрачных обводок разной толщины
        shadowFill.alpha = (255 * strength * 0.5f).toInt()
        canvas.drawPath(shadowPath, shadowFill)
        for (w in floatArrayOf(5f, 11f, 18f)) {
            shadowEdge.strokeWidth = w * sc
            shadowEdge.alpha = (255 * strength * 0.08f).toInt()
            canvas.drawPath(shadowPath, shadowEdge)
        }
        canvas.restore()
    }

    private fun drawStrips(
        canvas: android.graphics.Canvas,
        flips: List<FlipItem>,
        art: CalendarArt,
        count: Int,
        sc: Float,
        behind: Boolean,
    ) {
        // Собираем нужные полосы и сортируем от дальних к ближним
        var n = 0
        for (k in 0 until count) {
            for (j in 0 until NY) {
                var z = 0f
                for (i in 0..NX) z += pz[k][j * (NX + 1) + i] + pz[k][(j + 1) * (NX + 1) + i]
                z /= 2f * (NX + 1)
                if ((z < -1.5f) != behind) continue
                stripKeys[n++] = (java.lang.Float.floatToIntBits(z + 10000f).toLong() shl 32) or
                    (k.toLong() shl 16) or j.toLong()
            }
        }
        if (n == 0) return
        java.util.Arrays.sort(stripKeys, 0, n)

        if (behind) {
            canvas.save()
            canvas.clipPath(aboveHeaderClip)
        }
        for (c in 0 until n) {
            val key = stripKeys[c]
            val k = ((key shr 16) and 0xFFFF).toInt()
            val j = (key and 0xFFFF).toInt()
            drawStrip(canvas, art, flips[k].day, k, j, sc)
        }
        if (behind) canvas.restore()
    }

    private fun drawStrip(canvas: android.graphics.Canvas, art: CalendarArt, day: Int, k: Int, j: Int, sc: Float) {
        val top = j * (NX + 1)
        val bottom = top + NX + 1

        // Какой стороной полоса повёрнута к нам — по направлению обхода её углов на экране
        val area = shoelace(sx[k], sy[k], top, top + NX, bottom + NX, bottom)
        if (abs(area) < 0.5f) return
        val frontSide = area > 0f

        for (r in 0..1) {
            for (i in 0..NX) {
                val v = (if (r == 0) top else bottom) + i
                val m = r * (NX + 1) + i
                meshVerts[m * 2] = sx[k][v] * sc
                meshVerts[m * 2 + 1] = sy[k][v] * sc

                // Плавное освещение по вершинам: нормаль к листу в этой точке
                val a = pa[k][v]
                var ny = -sin(a)
                var nz = cos(a)
                if (!frontSide) { ny = -ny; nz = -nz }
                val lit = max(0f, ny * Light[1] + nz * Light[2]) / Light[2]
                val shade = (0.76f + 0.24f * lit).coerceIn(0f, 1f)
                val c = (255 * shade).toInt()
                meshColors[m] = android.graphics.Color.rgb(c, c, c)
            }
        }

        val strip = art.strips(day, frontSide, sc)[j]
        canvas.drawBitmapMesh(strip, NX, 1, meshVerts, 0, meshColors, 0, meshPaint)
    }
}

private fun shoelace(x: FloatArray, y: FloatArray, a: Int, b: Int, c: Int, d: Int): Float =
    (x[a] * y[b] - x[b] * y[a]) + (x[b] * y[c] - x[c] * y[b]) +
        (x[c] * y[d] - x[d] * y[c]) + (x[d] * y[a] - x[a] * y[d])

/** Плавная кривая через ключевые точки (сплайн Катмулла — Рома). */
private fun spline(values: FloatArray, t: Float): Float {
    val tc = t.coerceIn(0f, 1f)
    var i = 0
    while (i < KeyT.size - 2 && tc > KeyT[i + 1]) i++
    val t0 = KeyT[i]
    val t1 = KeyT[i + 1]
    val h = t1 - t0
    val u = (tc - t0) / h
    fun slope(k: Int): Float = when (k) {
        0 -> (values[1] - values[0]) / (KeyT[1] - KeyT[0])
        KeyT.lastIndex -> (values[k] - values[k - 1]) / (KeyT[k] - KeyT[k - 1])
        else -> (values[k + 1] - values[k - 1]) / (KeyT[k + 1] - KeyT[k - 1])
    }
    val m0 = slope(i) * h
    val m1 = slope(i + 1) * h
    val u2 = u * u
    val u3 = u2 * u
    return (2 * u3 - 3 * u2 + 1) * values[i] + (u3 - 2 * u2 + u) * m0 +
        (-2 * u3 + 3 * u2) * values[i + 1] + (u3 - u2) * m1
}

private fun normalize(x: Float, y: Float, z: Float): FloatArray {
    val l = sqrt(x * x + y * y + z * z)
    return floatArrayOf(x / l, y / l, z / l)
}
