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
        insetPx: Float,
        spacingPx: Float,
        radiusPx: Float,
        arc: Float,
        edgeGapPx: Float,
        minIconGapPx: Float,
        color: Int,
        opacity: Float,
        icons: List<Bitmap?>,
        selectedIndex: Int,
    ) {
        if (icons.isEmpty()) return
        val now = SystemClock.uptimeMillis()
        if (appearanceStartedAtMs == 0L) appearanceStartedAtMs = now

        // Yerleşim tek kaynaktan (RingLayout) gelir: kenar boyunca simetri, üst
        // üste binmeme ve tetik kenarına olan en küçük mesafe orada garanti
        // edilir. Parmak yalnızca ne kadar içeri girildiğini (reveal) belirler.
        val layout = RingLayout.compute(
            RingLayout.Spec(
                edge = edge,
                alongExtent = if (edge == Edge.BOTTOM) width else height,
                depthExtent = if (edge == Edge.BOTTOM) height else width,
                touchAlong = touch,
                count = icons.size,
                radius = radiusPx,
                spacing = spacingPx,
                baseDepth = insetPx,
                arc = arc,
                edgeGap = edgeGapPx,
                minIconGap = minIconGapPx,
            ),
        )
        val radius = layout.radius
        val revealEnd = layout.baseDepth.coerceAtLeast(threshold + 1f)
        val progress = ((stretch - threshold) / (revealEnd - threshold)).coerceIn(0f, 1f)
        icons.forEachIndexed { index, icon ->
            val (x, y) = layout.center(index, edge, width, height)
            val selected = index == selectedIndex
            val pulse = if (selected) {
                val phase = (SystemClock.uptimeMillis() % 720L) / 720f
                RingLayout.SELECTED_PULSE * ((sin(phase * Math.PI * 2.0) + 1.0) / 2.0).toFloat()
            } else 0f
            val scale = if (selected) RingLayout.SELECTED_SCALE + pulse else 1f
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
