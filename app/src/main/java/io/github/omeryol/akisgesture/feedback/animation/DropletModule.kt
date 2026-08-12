package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import io.github.omeryol.akisgesture.feedback.Physics3DEngine
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.sin
import kotlin.math.PI

/** Single Continuous Vector Path Liquid Drop Hanging & Stretching from Base Edge (Sünme Fiziği). */
class DropletModule : NaturalAnimationModule {
    private val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rimHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val dropPath = Path()
    private val rimPath = Path()

    override fun draw(f: AnimationFrame) {
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.25f)
        val timeSec = f.time

        // ── Dimensions matching vector reference artwork ──
        val baseSpan = (55f + growth * 140f) * f.size
        val baseDepth = (12f + growth * 18f) * f.size
        val totalDepth = (baseDepth + f.stretch * 1.05f).coerceAtMost(300f * f.size)

        // Refined droplet head radius (scaled down 1 notch for elegance: 8dp to 17dp)
        val dropRadius = (8f + growth * 17f) * f.size
        // Elastic Necking: Connection thins out smoothly as stretched ("bağları incelsin ama kopmasın")
        val neckHalfWidth = (dropRadius * (1.20f - growth * 0.70f)).coerceAtLeast(4.0f * f.size)
        val wobble = sin(timeSec * PI * 2.5).toFloat() * dropRadius * 0.05f

        val centerTouch = f.touch
        val headDepth = totalDepth - dropRadius

        dropPath.reset()
        rimPath.reset()

        // Construct 1 Continuous Liquid Vector Path with Concave Neck Curves
        when (f.edge) {
            Edge.LEFT -> {
                // 1. Base start
                dropPath.moveTo(0f, centerTouch - baseSpan)
                // 2. Curve along fluid base to neck start
                dropPath.cubicTo(baseDepth * 0.5f, centerTouch - baseSpan, baseDepth, centerTouch - neckHalfWidth * 2.8f, baseDepth, centerTouch - neckHalfWidth * 1.8f)
                // 3. Concave neck stretch curve to droplet head top
                dropPath.cubicTo(headDepth * 0.5f, centerTouch - neckHalfWidth, headDepth - dropRadius * 0.5f, centerTouch - dropRadius + wobble, headDepth, centerTouch - dropRadius)
                
                // Track Rim Highlight along left/upper edge
                rimPath.moveTo(baseDepth, centerTouch - neckHalfWidth * 1.8f)
                rimPath.cubicTo(headDepth * 0.5f, centerTouch - neckHalfWidth, headDepth - dropRadius * 0.5f, centerTouch - dropRadius + wobble, headDepth, centerTouch - dropRadius)

                // 4. Rounded droplet head bulb tip
                dropPath.cubicTo(totalDepth + dropRadius * 0.3f, centerTouch - dropRadius, totalDepth + dropRadius * 0.3f, centerTouch + dropRadius, headDepth, centerTouch + dropRadius)
                // 5. Concave neck return curve back to fluid base end
                dropPath.cubicTo(headDepth - dropRadius * 0.5f, centerTouch + dropRadius - wobble, headDepth * 0.5f, centerTouch + neckHalfWidth, baseDepth, centerTouch + neckHalfWidth * 1.8f)
                dropPath.cubicTo(baseDepth, centerTouch + neckHalfWidth * 2.8f, baseDepth * 0.5f, centerTouch + baseSpan, 0f, centerTouch + baseSpan)
                dropPath.close()
            }
            Edge.RIGHT -> {
                dropPath.moveTo(f.width, centerTouch - baseSpan)
                dropPath.cubicTo(f.width - baseDepth * 0.5f, centerTouch - baseSpan, f.width - baseDepth, centerTouch - neckHalfWidth * 2.8f, f.width - baseDepth, centerTouch - neckHalfWidth * 1.8f)
                dropPath.cubicTo(f.width - headDepth * 0.5f, centerTouch - neckHalfWidth, f.width - headDepth + dropRadius * 0.5f, centerTouch - dropRadius + wobble, f.width - headDepth, centerTouch - dropRadius)

                rimPath.moveTo(f.width - baseDepth, centerTouch - neckHalfWidth * 1.8f)
                rimPath.cubicTo(f.width - headDepth * 0.5f, centerTouch - neckHalfWidth, f.width - headDepth + dropRadius * 0.5f, centerTouch - dropRadius + wobble, f.width - headDepth, centerTouch - dropRadius)

                dropPath.cubicTo(f.width - totalDepth - dropRadius * 0.3f, centerTouch - dropRadius, f.width - totalDepth - dropRadius * 0.3f, centerTouch + dropRadius, f.width - headDepth, centerTouch + dropRadius)
                dropPath.cubicTo(f.width - headDepth + dropRadius * 0.5f, centerTouch + dropRadius - wobble, f.width - headDepth * 0.5f, centerTouch + neckHalfWidth, f.width - baseDepth, centerTouch + neckHalfWidth * 1.8f)
                dropPath.cubicTo(f.width - baseDepth, centerTouch + neckHalfWidth * 2.8f, f.width - baseDepth * 0.5f, centerTouch + baseSpan, f.width, centerTouch + baseSpan)
                dropPath.close()
            }
            Edge.BOTTOM -> {
                dropPath.moveTo(centerTouch - baseSpan, f.height)
                dropPath.cubicTo(centerTouch - baseSpan, f.height - baseDepth * 0.5f, centerTouch - neckHalfWidth * 2.8f, f.height - baseDepth, centerTouch - neckHalfWidth * 1.8f, f.height - baseDepth)
                dropPath.cubicTo(centerTouch - neckHalfWidth, f.height - headDepth * 0.5f, centerTouch - dropRadius + wobble, f.height - headDepth + dropRadius * 0.5f, centerTouch - dropRadius, f.height - headDepth)

                rimPath.moveTo(centerTouch - neckHalfWidth * 1.8f, f.height - baseDepth)
                rimPath.cubicTo(centerTouch - neckHalfWidth, f.height - headDepth * 0.5f, centerTouch - dropRadius + wobble, f.height - headDepth + dropRadius * 0.5f, centerTouch - dropRadius, f.height - headDepth)

                dropPath.cubicTo(centerTouch - dropRadius, f.height - totalDepth - dropRadius * 0.3f, centerTouch + dropRadius, f.height - totalDepth - dropRadius * 0.3f, centerTouch + dropRadius, f.height - headDepth)
                dropPath.cubicTo(centerTouch + dropRadius - wobble, f.height - headDepth + dropRadius * 0.5f, centerTouch + neckHalfWidth, f.height - headDepth * 0.5f, centerTouch + neckHalfWidth * 1.8f, f.height - baseDepth)
                dropPath.cubicTo(centerTouch + neckHalfWidth * 2.8f, f.height - baseDepth, centerTouch + baseSpan, f.height - baseDepth * 0.5f, centerTouch + baseSpan, f.height)
                dropPath.close()
            }
        }

        // ── 3D Soft Drop Shadow for Fluid Drop Body ──
        Physics3DEngine.drawDropShadow(f.canvas, dropPath, dx = 6f, dy = 10f, opacity = f.opacity * 0.50f)

        // ── Fill Liquid Body with Gradient Shading matching reference ──
        val brightColor = lighten(f.color, 0.48f)
        val deepColor = darken(f.color, 0.32f)
        mainPaint.shader = gradient(f, totalDepth, withAlpha(brightColor, (245 * f.opacity).toInt()), withAlpha(deepColor, (190 * f.opacity).toInt()))
        f.canvas.drawPath(dropPath, mainPaint)

        // ── Specular White Curved Rim Line along the neck & drop ──
        rimHighlightPaint.strokeWidth = 3.2f * f.size
        rimHighlightPaint.color = withAlpha(Color.WHITE, (245 * f.opacity).toInt())
        f.canvas.drawPath(rimPath, rimHighlightPaint)

        mainPaint.shader = null
    }

    private fun gradient(f: AnimationFrame, depth: Float, a: Int, b: Int) = when (f.edge) {
        Edge.LEFT -> LinearGradient(0f, f.touch, depth, f.touch, a, b, Shader.TileMode.CLAMP)
        Edge.RIGHT -> LinearGradient(f.width, f.touch, f.width - depth, f.touch, a, b, Shader.TileMode.CLAMP)
        Edge.BOTTOM -> LinearGradient(f.touch, f.height, f.touch, f.height - depth, a, b, Shader.TileMode.CLAMP)
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
    private fun lighten(c: Int, t: Float) = Color.rgb((Color.red(c) + (255 - Color.red(c)) * t).toInt().coerceIn(0, 255), (Color.green(c) + (255 - Color.green(c)) * t).toInt().coerceIn(0, 255), (Color.blue(c) + (255 - Color.blue(c)) * t).toInt().coerceIn(0, 255))
    private fun darken(c: Int, t: Float) = Color.rgb((Color.red(c) * (1f - t)).toInt().coerceIn(0, 255), (Color.green(c) * (1f - t)).toInt().coerceIn(0, 255), (Color.blue(c) * (1f - t)).toInt().coerceIn(0, 255))
}
