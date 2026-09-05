package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

/**
 * Frosted Glass Refraction & Chromatic Dispersion Ripple.
 * Renders an organic liquid-glass meniscus stretching across the display with
 * chromatic dispersion fringing (cyan/magenta split), frosted caustic rings, and prism glints.
 */
class GlassRefractionModule : NaturalAnimationModule {
    private val glassBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cyanFringePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val magentaFringePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val glintPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val glassPath = Path()
    private val cyanPath = Path()
    private val magentaPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.3f)
        val depth = (18f + f.stretch * 1.15f).coerceAtMost(380f * f.size)
        val span = (48f + f.progress * 210f) * f.size

        // ── 1. Frosted Liquid Glass Body ──
        glassPath.reset()
        cyanPath.reset()
        magentaPath.reset()

        val steps = 44
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat().pow(1.5f)
            val ripple = sin(u * PI * 4.0 + timeSec * 2.8) * (5.0 + growth * 8.0)
            val d = (depth + ripple.toFloat()) * env

            val p = point(f, f.touch - span + u * span * 2f, d)
            // Slight chromatic aberration spatial offsets
            val pCyan = point(f, f.touch - span + u * span * 2f, d + 2.5f * f.size)
            val pMagenta = point(f, f.touch - span + u * span * 2f, (d - 2.5f * f.size).coerceAtLeast(0f))

            if (i == 0) {
                glassPath.moveTo(p.first, p.second)
                cyanPath.moveTo(pCyan.first, pCyan.second)
                magentaPath.moveTo(pMagenta.first, pMagenta.second)
            } else {
                glassPath.lineTo(p.first, p.second)
                cyanPath.lineTo(pCyan.first, pCyan.second)
                magentaPath.lineTo(pMagenta.first, pMagenta.second)
            }
        }
        close(glassPath, f, span)

        // Frosted Glass Core: Translucent White/Ice-Blue Gradient
        val iceTone = blend(f.color, 0xFFE0F7FA.toInt(), 0.5f)
        glassBodyPaint.shader = gradient(
            f, depth,
            alpha(iceTone, (190 * f.opacity).toInt()),
            alpha(darken(f.color, 0.2f), (100 * f.opacity).toInt())
        )
        f.canvas.drawPath(glassPath, glassBodyPaint)

        // ── 2. Chromatic Dispersion Fringing (Cyan & Magenta Light Split) ──
        cyanFringePaint.strokeWidth = (1.8f + growth * 1.4f) * f.size
        cyanFringePaint.color = alpha(0xFF00E5FF.toInt(), (175 * f.opacity).toInt())
        f.canvas.drawPath(cyanPath, cyanFringePaint)

        magentaFringePaint.strokeWidth = (1.8f + growth * 1.4f) * f.size
        magentaFringePaint.color = alpha(0xFFFF4081.toInt(), (160 * f.opacity).toInt())
        f.canvas.drawPath(magentaPath, magentaFringePaint)

        // ── 3. Sparkling Prism Glints Along the Glass Crest ──
        val glintCount = 6
        for (i in 0 until glintCount) {
            val seed = i * 29.7f
            val phase = ((timeSec * 0.85 + seed) % 1.0).toFloat()
            val along = f.touch - span * 0.7f + (i / (glintCount - 1).toFloat()) * span * 1.4f
            val env = sin(PI * (i / (glintCount - 1).toFloat())).toFloat().pow(1.5f)
            val d = (depth * 0.95f) * env

            val p = point(f, along, d)
            val glintAlpha = ((sin(phase * PI) * 240) * f.opacity).toInt().coerceIn(0, 255)
            val glintRadius = (2.0f + sin(phase * PI).toFloat() * 2.5f) * f.size

            glintPaint.color = alpha(Color.WHITE, glintAlpha)
            f.canvas.drawCircle(p.first, p.second, glintRadius, glintPaint)
        }

        glassBodyPaint.shader = null
    }

    private fun point(f: AnimationFrame, along: Float, depth: Float) = when (f.edge) {
        Edge.LEFT -> depth to along
        Edge.RIGHT -> (f.width - depth) to along
        Edge.BOTTOM -> along to (f.height - depth)
    }

    private fun close(p: Path, f: AnimationFrame, span: Float) {
        when (f.edge) {
            Edge.LEFT -> { p.lineTo(0f, f.touch + span); p.lineTo(0f, f.touch - span) }
            Edge.RIGHT -> { p.lineTo(f.width, f.touch + span); p.lineTo(f.width, f.touch - span) }
            Edge.BOTTOM -> { p.lineTo(f.touch + span, f.height); p.lineTo(f.touch - span, f.height) }
        }
        p.close()
    }

    private fun gradient(f: AnimationFrame, depth: Float, startColor: Int, endColor: Int) = when (f.edge) {
        Edge.LEFT -> LinearGradient(0f, f.touch, depth, f.touch, startColor, endColor, Shader.TileMode.CLAMP)
        Edge.RIGHT -> LinearGradient(f.width, f.touch, f.width - depth, f.touch, startColor, endColor, Shader.TileMode.CLAMP)
        Edge.BOTTOM -> LinearGradient(f.touch, f.height, f.touch, f.height - depth, startColor, endColor, Shader.TileMode.CLAMP)
    }
}
