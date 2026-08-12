package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.feedback.Physics3DEngine
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.sin

/** Ultra-Rich Volumetric 3D Liquid Drop with Dynamic Water Wave Base & Thinned Surface Tension Necking. */
class DropletModule : NaturalAnimationModule {
    private val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val innerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rimHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val crestPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val glintSpotPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val outerDropPath = Path()
    private val innerCorePath = Path()
    private val rimPath = Path()
    private val baseCrestPath = Path()

    override fun draw(f: AnimationFrame) {
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.25f)
        val timeSec = f.time

        // ── 1. Dynamic Water Wave Base Parameters ──
        val baseSpan = (58f + growth * 145f) * f.size
        val baseRipple = sin(timeSec * 3.5).toFloat() * 5.5f * f.size
        val baseDepth = (14f + growth * 20f + baseRipple).coerceAtLeast(6f * f.size)
        val totalDepth = (baseDepth + f.stretch * 1.08f).coerceAtMost(310f * f.size)

        // Refined droplet bulb head radius
        val dropRadius = (8.5f + growth * 16.5f) * f.size
        // Thinned necking connection ("bağı biraz daha incelt": 1.10f - growth * 0.82f, down to 2.2dp)
        val neckHalfWidth = (dropRadius * (1.10f - growth * 0.82f)).coerceAtLeast(2.2f * f.size)
        val wobbleX = sin(timeSec * PI * 3.8).toFloat() * dropRadius * 0.08f

        val centerTouch = f.touch
        val headDepth = totalDepth - dropRadius

        outerDropPath.reset()
        innerCorePath.reset()
        rimPath.reset()
        baseCrestPath.reset()

        // ── 2. Construct Main Outer Fluid Path with Dynamic Wave Base ──
        when (f.edge) {
            Edge.LEFT -> {
                // Dynamic wave crest along base edge
                baseCrestPath.moveTo(0f, centerTouch - baseSpan)
                baseCrestPath.quadTo(baseDepth * 0.8f, centerTouch - baseSpan * 0.5f, baseDepth, centerTouch - neckHalfWidth * 2.8f)
                baseCrestPath.moveTo(baseDepth, centerTouch + neckHalfWidth * 2.8f)
                baseCrestPath.quadTo(baseDepth * 0.8f, centerTouch + baseSpan * 0.5f, 0f, centerTouch + baseSpan)

                outerDropPath.moveTo(0f, centerTouch - baseSpan)
                outerDropPath.cubicTo(baseDepth * 0.5f, centerTouch - baseSpan, baseDepth, centerTouch - neckHalfWidth * 2.8f, baseDepth, centerTouch - neckHalfWidth * 1.8f)
                outerDropPath.cubicTo(headDepth * 0.5f, centerTouch - neckHalfWidth, headDepth - dropRadius * 0.5f, centerTouch - dropRadius + wobbleX, headDepth, centerTouch - dropRadius)
                
                rimPath.moveTo(baseDepth, centerTouch - neckHalfWidth * 1.8f)
                rimPath.cubicTo(headDepth * 0.5f, centerTouch - neckHalfWidth, headDepth - dropRadius * 0.5f, centerTouch - dropRadius + wobbleX, headDepth, centerTouch - dropRadius)

                outerDropPath.cubicTo(totalDepth + dropRadius * 0.32f, centerTouch - dropRadius, totalDepth + dropRadius * 0.32f, centerTouch + dropRadius, headDepth, centerTouch + dropRadius)
                outerDropPath.cubicTo(headDepth - dropRadius * 0.5f, centerTouch + dropRadius - wobbleX, headDepth * 0.5f, centerTouch + neckHalfWidth, baseDepth, centerTouch + neckHalfWidth * 1.8f)
                outerDropPath.cubicTo(baseDepth, centerTouch + neckHalfWidth * 2.8f, baseDepth * 0.5f, centerTouch + baseSpan, 0f, centerTouch + baseSpan)
                outerDropPath.close()

                val innerR = dropRadius * 0.65f
                val innerNeck = neckHalfWidth * 0.55f
                innerCorePath.moveTo(baseDepth * 0.8f, centerTouch - innerNeck * 1.5f)
                innerCorePath.cubicTo(headDepth * 0.5f, centerTouch - innerNeck, headDepth - innerR, centerTouch - innerR, headDepth, centerTouch - innerR)
                innerCorePath.cubicTo(totalDepth, centerTouch - innerR, totalDepth, centerTouch + innerR, headDepth, centerTouch + innerR)
                innerCorePath.cubicTo(headDepth - innerR, centerTouch + innerR, headDepth * 0.5f, centerTouch + innerNeck, baseDepth * 0.8f, centerTouch + innerNeck * 1.5f)
                innerCorePath.close()
            }
            Edge.RIGHT -> {
                baseCrestPath.moveTo(f.width, centerTouch - baseSpan)
                baseCrestPath.quadTo(f.width - baseDepth * 0.8f, centerTouch - baseSpan * 0.5f, f.width - baseDepth, centerTouch - neckHalfWidth * 2.8f)
                baseCrestPath.moveTo(f.width - baseDepth, centerTouch + neckHalfWidth * 2.8f)
                baseCrestPath.quadTo(f.width - baseDepth * 0.8f, centerTouch + baseSpan * 0.5f, f.width, centerTouch + baseSpan)

                outerDropPath.moveTo(f.width, centerTouch - baseSpan)
                outerDropPath.cubicTo(f.width - baseDepth * 0.5f, centerTouch - baseSpan, f.width - baseDepth, centerTouch - neckHalfWidth * 2.8f, f.width - baseDepth, centerTouch - neckHalfWidth * 1.8f)
                outerDropPath.cubicTo(f.width - headDepth * 0.5f, centerTouch - neckHalfWidth, f.width - headDepth + dropRadius * 0.5f, centerTouch - dropRadius + wobbleX, f.width - headDepth, centerTouch - dropRadius)

                rimPath.moveTo(f.width - baseDepth, centerTouch - neckHalfWidth * 1.8f)
                rimPath.cubicTo(f.width - headDepth * 0.5f, centerTouch - neckHalfWidth, f.width - headDepth + dropRadius * 0.5f, centerTouch - dropRadius + wobbleX, f.width - headDepth, centerTouch - dropRadius)

                outerDropPath.cubicTo(f.width - totalDepth - dropRadius * 0.32f, centerTouch - dropRadius, f.width - totalDepth - dropRadius * 0.32f, centerTouch + dropRadius, f.width - headDepth, centerTouch + dropRadius)
                outerDropPath.cubicTo(f.width - headDepth + dropRadius * 0.5f, centerTouch + dropRadius - wobbleX, f.width - headDepth * 0.5f, centerTouch + neckHalfWidth, f.width - baseDepth, centerTouch + neckHalfWidth * 1.8f)
                outerDropPath.cubicTo(f.width - baseDepth, centerTouch + neckHalfWidth * 2.8f, f.width - baseDepth * 0.5f, centerTouch + baseSpan, f.width, centerTouch + baseSpan)
                outerDropPath.close()

                val innerR = dropRadius * 0.65f
                val innerNeck = neckHalfWidth * 0.55f
                innerCorePath.moveTo(f.width - baseDepth * 0.8f, centerTouch - innerNeck * 1.5f)
                innerCorePath.cubicTo(f.width - headDepth * 0.5f, centerTouch - innerNeck, f.width - headDepth + innerR, centerTouch - innerR, f.width - headDepth, centerTouch - innerR)
                innerCorePath.cubicTo(f.width - totalDepth, centerTouch - innerR, f.width - totalDepth, centerTouch + innerR, f.width - headDepth, centerTouch + innerR)
                innerCorePath.cubicTo(f.width - headDepth + innerR, centerTouch + innerR, f.width - headDepth * 0.5f, centerTouch + innerNeck, f.width - baseDepth * 0.8f, centerTouch + innerNeck * 1.5f)
                innerCorePath.close()
            }
            Edge.BOTTOM -> {
                baseCrestPath.moveTo(centerTouch - baseSpan, f.height)
                baseCrestPath.quadTo(centerTouch - baseSpan * 0.5f, f.height - baseDepth * 0.8f, centerTouch - neckHalfWidth * 2.8f, f.height - baseDepth)
                baseCrestPath.moveTo(centerTouch + neckHalfWidth * 2.8f, f.height - baseDepth)
                baseCrestPath.quadTo(centerTouch + baseSpan * 0.5f, f.height - baseDepth * 0.8f, centerTouch + baseSpan, f.height)

                outerDropPath.moveTo(centerTouch - baseSpan, f.height)
                outerDropPath.cubicTo(centerTouch - baseSpan, f.height - baseDepth * 0.5f, centerTouch - neckHalfWidth * 2.8f, f.height - baseDepth, centerTouch - neckHalfWidth * 1.8f, f.height - baseDepth)
                outerDropPath.cubicTo(centerTouch - neckHalfWidth, f.height - headDepth * 0.5f, centerTouch - dropRadius + wobbleX, f.height - headDepth + dropRadius * 0.5f, centerTouch - dropRadius, f.height - headDepth)

                rimPath.moveTo(centerTouch - neckHalfWidth * 1.8f, f.height - baseDepth)
                rimPath.cubicTo(centerTouch - neckHalfWidth, f.height - headDepth * 0.5f, centerTouch - dropRadius + wobbleX, f.height - headDepth + dropRadius * 0.5f, centerTouch - dropRadius, f.height - headDepth)

                outerDropPath.cubicTo(centerTouch - dropRadius, f.height - totalDepth - dropRadius * 0.32f, centerTouch + dropRadius, f.height - totalDepth - dropRadius * 0.32f, centerTouch + dropRadius, f.height - headDepth)
                outerDropPath.cubicTo(centerTouch + dropRadius - wobbleX, f.height - headDepth + dropRadius * 0.5f, centerTouch + neckHalfWidth, f.height - headDepth * 0.5f, centerTouch + neckHalfWidth * 1.8f, f.height - baseDepth)
                outerDropPath.cubicTo(centerTouch + neckHalfWidth * 2.8f, f.height - baseDepth, centerTouch + baseSpan, f.height - baseDepth * 0.5f, centerTouch + baseSpan, f.height)
                outerDropPath.close()

                val innerR = dropRadius * 0.65f
                val innerNeck = neckHalfWidth * 0.55f
                innerCorePath.moveTo(centerTouch - innerNeck * 1.5f, f.height - baseDepth * 0.8f)
                innerCorePath.cubicTo(centerTouch - innerNeck, f.height - headDepth * 0.5f, centerTouch - innerR, f.height - headDepth + innerR, centerTouch - innerR, f.height - headDepth)
                innerCorePath.cubicTo(centerTouch - innerR, f.height - totalDepth, centerTouch + innerR, f.height - totalDepth, centerTouch + innerR, f.height - headDepth)
                innerCorePath.cubicTo(centerTouch + innerR, f.height - headDepth + innerR, centerTouch + innerNeck, f.height - headDepth * 0.5f, centerTouch + innerNeck * 1.5f, f.height - baseDepth * 0.8f)
                innerCorePath.close()
            }
        }

        // ── 3. LAYER: Ambient Luminous Drop Aura ──
        val centerPt = when (f.edge) {
            Edge.LEFT -> headDepth to centerTouch
            Edge.RIGHT -> (f.width - headDepth) to centerTouch
            Edge.BOTTOM -> centerTouch to (f.height - headDepth)
        }
        auraPaint.shader = RadialGradient(
            centerPt.first, centerPt.second, dropRadius * 2.6f,
            intArrayOf(withAlpha(lighten(f.color, 0.45f), (135 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawCircle(centerPt.first, centerPt.second, dropRadius * 2.6f, auraPaint)

        // ── 4. LAYER: 3D Soft Drop Shadow ──
        Physics3DEngine.drawDropShadow(f.canvas, outerDropPath, dx = 6f, dy = 10f, opacity = f.opacity * 0.50f)

        // ── 5. LAYER: Main Outer Fluid Body Gradient ──
        val brightColor = lighten(f.color, 0.45f)
        val deepColor = darken(f.color, 0.35f)
        mainPaint.shader = gradient(f, totalDepth, withAlpha(brightColor, (245 * f.opacity).toInt()), withAlpha(deepColor, (190 * f.opacity).toInt()))
        f.canvas.drawPath(outerDropPath, mainPaint)

        // ── 6. LAYER: Dynamic Wave Crest along Base Edge ──
        crestPaint.strokeWidth = 2.5f * f.size
        crestPaint.color = withAlpha(lighten(f.color, 0.75f), (210 * f.opacity).toInt())
        f.canvas.drawPath(baseCrestPath, crestPaint)

        // ── 7. LAYER: Inner Volumetric Refraction Core ──
        val innerBright = lighten(f.color, 0.75f)
        innerGlowPaint.shader = gradient(f, totalDepth * 0.85f, withAlpha(innerBright, (180 * f.opacity).toInt()), withAlpha(brightColor, (80 * f.opacity).toInt()))
        f.canvas.drawPath(innerCorePath, innerGlowPaint)

        // ── 8. LAYER: Specular White Curved Rim Line ──
        rimHighlightPaint.strokeWidth = 3.2f * f.size
        rimHighlightPaint.color = withAlpha(Color.WHITE, (245 * f.opacity).toInt())
        f.canvas.drawPath(rimPath, rimHighlightPaint)

        // ── 9. LAYER: Specular Glint Spot on Droplet Bulb ──
        val specular = Physics3DEngine.computeSpecularLight(0.3f, -0.7f, 0.8f, shininess = 26f)
        glintSpotPaint.color = withAlpha(Color.WHITE, ((215 + specular * 40f) * f.opacity).toInt().coerceIn(0, 255))
        f.canvas.drawCircle(centerPt.first - dropRadius * 0.35f, centerPt.second - dropRadius * 0.35f, (dropRadius * 0.30f).coerceAtLeast(2.5f), glintSpotPaint)

        mainPaint.shader = null
        innerGlowPaint.shader = null
        auraPaint.shader = null
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
