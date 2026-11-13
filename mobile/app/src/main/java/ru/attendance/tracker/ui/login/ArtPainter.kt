package ru.attendance.tracker.ui.login

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader

/** Рисует векторные фигуры из LoginArt.kt в их собственных координатах (масштаб задаёт холст). */
internal class ArtPainter(shapes: List<ArtShape>, palette: IntArray) {
    private class Item(val layer: ArtLayer, val path: Path, val fill: Paint?, val stroke: Paint?)

    private val items = shapes.map { shape ->
        Item(
            layer = shape.layer,
            path = shape.path.toPath(),
            fill = shape.fill?.let { fillPaint(it, palette) },
            stroke = if (shape.stroke >= 0) strokePaint(palette[shape.stroke], shape.strokeWidth) else null,
        )
    }

    fun draw(canvas: Canvas, layer: ArtLayer? = null) {
        for (item in items) {
            if (layer != null && item.layer != layer) continue
            item.fill?.let { canvas.drawPath(item.path, it) }
            item.stroke?.let { canvas.drawPath(item.path, it) }
        }
    }

    private fun fillPaint(fill: ArtFill, palette: IntArray) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        if (fill.from == fill.to) {
            color = palette[fill.from]
        } else {
            shader = LinearGradient(
                fill.x1, fill.y1, fill.x2, fill.y2,
                palette[fill.from], palette[fill.to],
                Shader.TileMode.CLAMP,
            )
        }
    }

    private fun strokePaint(color: Int, width: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.STROKE
        strokeWidth = width
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
}

/** Собирает путь из команд: 0 — moveTo, 1 — lineTo, 2 — quadTo, 3 — cubicTo, 4 — close. */
internal fun FloatArray.toPath(): Path {
    val path = Path()
    var i = 0
    while (i < size) {
        when (this[i].toInt()) {
            0 -> { path.moveTo(this[i + 1], this[i + 2]); i += 3 }
            1 -> { path.lineTo(this[i + 1], this[i + 2]); i += 3 }
            2 -> { path.quadTo(this[i + 1], this[i + 2], this[i + 3], this[i + 4]); i += 5 }
            3 -> { path.cubicTo(this[i + 1], this[i + 2], this[i + 3], this[i + 4], this[i + 5], this[i + 6]); i += 7 }
            else -> { path.close(); i += 1 }
        }
    }
    return path
}
