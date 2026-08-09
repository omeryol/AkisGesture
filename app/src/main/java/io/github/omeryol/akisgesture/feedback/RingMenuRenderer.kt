package io.github.omeryol.akisgesture.feedback

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.abs

/** Draws the three small, frosted action bubbles that open after a hold. */
class RingMenuRenderer {
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val symbol = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }

    fun draw(
        canvas: Canvas,
        edge: Edge,
        touch: Float,
        width: Float,
        height: Float,
        color: Int,
        opacity: Float,
        symbols: List<String>,
        selectedIndex: Int,
        iconScale: Float,
    ) {
        if (symbols.isEmpty()) return
        val radius = 25f * iconScale
        val depth = 78f * iconScale
        val spread = 43f * iconScale
        val positions = when (edge) {
            Edge.LEFT -> listOf(
                depth - 14f to touch - spread,
                depth to touch,
                depth - 14f to touch + spread,
            )
            Edge.RIGHT -> listOf(
                width - depth + 14f to touch - spread,
                width - depth to touch,
                width - depth + 14f to touch + spread,
            )
            Edge.BOTTOM -> listOf(
                touch - spread to height - depth + 14f,
                touch to height - depth,
                touch + spread to height - depth + 14f,
            )
        }
        symbols.take(3).forEachIndexed { index, value ->
            val (x, y) = positions[index]
            val selected = index == selectedIndex
            val scale = if (selected) 1.18f else 1f
            val r = radius * scale
            val baseAlpha = ((if (selected) 0.70f else 0.42f) * opacity * 255).toInt()
            fill.shader = RadialGradient(
                x - r * .32f,
                y - r * .38f,
                r * 1.55f,
                intArrayOf(
                    Color.argb((baseAlpha * .78f).toInt(), Color.red(color), Color.green(color), Color.blue(color)),
                    Color.argb(baseAlpha, Color.red(color), Color.green(color), Color.blue(color)),
                    Color.argb((baseAlpha * .50f).toInt(), Color.red(color), Color.green(color), Color.blue(color)),
                ),
                floatArrayOf(0f, .52f, 1f),
                Shader.TileMode.CLAMP,
            )
            canvas.drawCircle(x, y, r, fill)
            fill.shader = null
            stroke.strokeWidth = if (selected) 2.2f else 1.2f
            stroke.color = Color.argb((baseAlpha * .95f).toInt(), 255, 255, 255)
            canvas.drawCircle(x, y, r, stroke)
            if (value.isNotEmpty()) {
                symbol.textSize = r * 1.00f
                symbol.color = Color.argb((opacity * 255).toInt(), 255, 255, 255)
                canvas.drawText(value, x, y - (symbol.ascent() + symbol.descent()) / 2f, symbol)
            }
        }
    }
}
