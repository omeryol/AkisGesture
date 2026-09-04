package io.github.omeryol.akisgesture.feedback

import android.graphics.Canvas
import android.graphics.Bitmap
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
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private var appearanceStartedAtMs = 0L

    fun resetAnimation() {
        appearanceStartedAtMs = 0L
    }

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
        icons: List<Bitmap?>,
        selectedIndex: Int,
        iconScale: Float,
        ringSizeDp: Float,
        ringArc: Float,
    ) {
        if (icons.isEmpty()) return
        val now = SystemClock.uptimeMillis()
        if (appearanceStartedAtMs == 0L) appearanceStartedAtMs = now
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
        val middleLead = spread * 1.45f
        val count = icons.size
        val m = (count - 1) / 2f
        val maxDistFromCenter = if (m > 0f) m else 1f
        val maxBound = if (edge == Edge.BOTTOM) width else height

        val positions = (0 until count).map { i ->
            val u = if (m > 0f) kotlin.math.abs(i - m) / maxDistFromCenter else 0f
            val itemLead = middleLead * (1f - ringArc.coerceIn(0f, 1f) * (u * u))
            val deltaEdge = (i - m) * spread
            val edgePos = (touch + deltaEdge).coerceIn(radius, maxBound - radius)
            when (edge) {
                Edge.LEFT -> (anchor + itemLead) to edgePos
                Edge.RIGHT -> (anchor - itemLead) to edgePos
                Edge.BOTTOM -> edgePos to (anchor - itemLead)
            }
        }
        icons.forEachIndexed { index, icon ->
            val (x, y) = positions[index]
            val selected = index == selectedIndex
            val pulse = if (selected) {
                val phase = (SystemClock.uptimeMillis() % 720L) / 720f
                0.05f * ((sin(phase * Math.PI * 2.0) + 1.0) / 2.0).toFloat()
            } else 0f
            val scale = if (selected) 1.24f + pulse else 1f
            val r = radius * scale
            val staggeredStart = appearanceStartedAtMs + index * 45L
            val appearance = ((now - staggeredStart) / 220f).coerceIn(0f, 1f)
            val easedAppearance = appearance * appearance * (3f - 2f * appearance)
            val reveal = ((0.18f + 0.82f * progress) * easedAppearance).coerceIn(0f, 1f)
            val visualRadius = r * (0.88f + 0.12f * easedAppearance)
            val baseAlpha = ((if (selected) 0.72f else 0.46f) * reveal * opacity * 255).toInt()
            if (selected) {
                val haloRadius = visualRadius * 1.72f
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
            canvas.drawCircle(x, y, visualRadius, fill)
            fill.shader = null
            stroke.strokeWidth = if (selected) 2.2f else 1.2f
            stroke.color = Color.argb((baseAlpha * .95f).toInt(), 255, 255, 255)
            canvas.drawCircle(x, y, visualRadius, stroke)
            if (icon != null) {
                val half = visualRadius * .52f
                iconPaint.alpha = (opacity * reveal * 255).toInt().coerceIn(0, 255)
                canvas.drawBitmap(icon, null, android.graphics.RectF(x - half, y - half, x + half, y + half), iconPaint)
                iconPaint.alpha = 255
            }
        }
    }
}
