package io.github.omeryol.akisgesture.feedback

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.SystemClock
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.sin

/** Draws the three small, frosted action bubbles that open after a hold. */
class RingMenuRenderer {
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val halo = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val symbol = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }

    fun draw(
        canvas: Canvas,
        edge: Edge,
        touch: Float,
        width: Float,
        height: Float,
        stretch: Float,
        threshold: Float,
        extraInsetPx: Float,
        spreadPx: Float,
        color: Int,
        opacity: Float,
        symbols: List<String>,
        selectedIndex: Int,
        iconScale: Float,
        ringSizeDp: Float,
    ) {
        if (symbols.isEmpty()) return
        // Keep the bubbles attached to the finger's inward travel instead of
        // pinning them to the trigger edge. The small lead offset keeps the
        // selected bubble visible around the fingertip.
        val radius = ringSizeDp * iconScale
        // Use a stable, screen-relative menu position. The finger's actual
        // inward pixel distance controls reveal only, so the three bubbles
        // never chase the fingertip or collapse on top of one another.
        val edgeSpan = when (edge) {
            Edge.LEFT, Edge.RIGHT -> width
            Edge.BOTTOM -> height
        }.coerceAtLeast(1f)
        val maxInset = if (edge == Edge.BOTTOM) edgeSpan * 0.5f else edgeSpan * 0.9f
        val menuInset = extraInsetPx.coerceIn(radius * 1.2f, maxInset)
        val revealEnd = menuInset.coerceAtLeast(threshold + 1f)
        val progress = ((stretch - threshold) / (revealEnd - threshold)).coerceIn(0f, 1f)
        val anchor = when (edge) {
            Edge.LEFT -> menuInset
            Edge.RIGHT -> width - menuInset
            Edge.BOTTOM -> height - menuInset
        }
        val spread = spreadPx.coerceAtLeast(36f)
        // Make the center bubble visibly lead inward while the outer bubbles
        // stay close to the edge, producing a pronounced half-arc.
        val middleLead = spread * 1.45f
        val sideLead = middleLead * 0.08f
        val middleAnchor = when (edge) {
            Edge.LEFT -> anchor + middleLead
            Edge.RIGHT -> anchor - middleLead
            Edge.BOTTOM -> anchor - middleLead
        }
        val sideY = listOf(
            (touch - spread).coerceIn(radius, height - radius),
            touch.coerceIn(radius, height - radius),
            (touch + spread).coerceIn(radius, height - radius),
        )
        val bottomX = listOf(
            (touch - spread).coerceIn(radius, width - radius),
            touch.coerceIn(radius, width - radius),
            (touch + spread).coerceIn(radius, width - radius),
        )
        val positions = when (edge) {
            Edge.LEFT -> listOf(
                (anchor + sideLead) to sideY[0],
                middleAnchor to sideY[1],
                (anchor + sideLead) to sideY[2],
            )
            Edge.RIGHT -> listOf(
                (anchor - sideLead) to sideY[0],
                middleAnchor to sideY[1],
                (anchor - sideLead) to sideY[2],
            )
            Edge.BOTTOM -> listOf(
                bottomX[0] to (anchor - sideLead),
                bottomX[1] to middleAnchor,
                bottomX[2] to (anchor - sideLead),
            )
        }
        symbols.take(3).forEachIndexed { index, value ->
            val (x, y) = positions[index]
            val selected = index == selectedIndex
            val pulse = if (selected) {
                val phase = (SystemClock.uptimeMillis() % 720L) / 720f
                0.05f * ((sin(phase * Math.PI * 2.0) + 1.0) / 2.0).toFloat()
            } else 0f
            val scale = if (selected) 1.24f + pulse else 1f
            val r = radius * scale
            val reveal = (0.18f + 0.82f * progress).coerceIn(0f, 1f)
            val baseAlpha = ((if (selected) 0.72f else 0.46f) * reveal * opacity * 255).toInt()
            if (selected) {
                val haloRadius = r * 1.72f
                halo.shader = RadialGradient(
                    x - r * .18f,
                    y - r * .20f,
                    haloRadius,
                    intArrayOf(
                        Color.argb((baseAlpha * .42f).toInt(), Color.red(color), Color.green(color), Color.blue(color)),
                        Color.argb((baseAlpha * .18f).toInt(), Color.red(color), Color.green(color), Color.blue(color)),
                        Color.TRANSPARENT,
                    ),
                    floatArrayOf(0f, .52f, 1f),
                    Shader.TileMode.CLAMP,
                )
                canvas.drawCircle(x, y, haloRadius, halo)
                halo.shader = null
            }
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
                symbol.color = Color.argb((opacity * reveal * 255).toInt(), 255, 255, 255)
                canvas.drawText(value, x, y - (symbol.ascent() + symbol.descent()) / 2f, symbol)
            }
        }
    }
}
