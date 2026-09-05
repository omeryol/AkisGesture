package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

/**
 * Bioluminescent Water Bubbles & Thin-Film Iridescence.
 * Renders an organic liquid meniscus at the bezel giving rise to buoyant effervescent
 * bubbles with thin-film interference rims, surface-tension wobbling, and ethereal pop rings.
 */
class BubbleModule : NaturalAnimationModule {
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bubbleShellPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bubbleRimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val glintPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val popPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val wavePath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.3f)
        val depth = (16f + f.stretch * 1.15f).coerceAtMost(360f * f.size)
        val span = (46f + f.progress * 200f) * f.size

        // ── 1. Gentle Base Liquid Meniscus Wave ──
        wavePath.reset()
        val steps = 36
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat().pow(1.5f)
            val wave = sin(u * PI * 4.0 + timeSec * 3.0) * (4.0 + growth * 6.0)
            val d = (depth * 0.38f + wave.toFloat()) * env

            val p = point(f, f.touch - span + u * span * 2f, d)
            if (i == 0) wavePath.moveTo(p.first, p.second) else wavePath.lineTo(p.first, p.second)
        }
        close(wavePath, f, span)

        val aquaTone = blend(f.color, 0xFF80D8FF.toInt(), 0.5f)
        wavePaint.shader = gradient(
            f, depth * 0.38f,
            alpha(aquaTone, (160 * f.opacity).toInt()),
            alpha(darken(f.color, 0.3f), (60 * f.opacity).toInt())
        )
        f.canvas.drawPath(wavePath, wavePaint)

        // ── 2. Buoyant Effervescent Bubbles (10 Bubbles) ──
        val bubbleCount = 10
        for (i in 0 until bubbleCount) {
            val seed = i * 43.19f
            val cycle = 1.2 + (i % 3) * 0.35
            val life = ((timeSec * 0.85 + seed) % cycle) / cycle
            val phase = life.toFloat()

            // Natural Archimedes buoyancy drift with subtle lateral sine sway
            val bDepth = depth * (0.28f + phase * 0.85f)
            val sway = sin(phase * PI * 2.5 + seed).toFloat() * (span * 0.25f)
            val along = f.touch - span * 0.45f + (i / bubbleCount.toFloat()) * span * 0.90f + sway

            val center = point(f, along, bDepth)
            val baseRadius = (5.5f + (i % 4) * 3.0f + phase * 6f) * f.size
            // Surface tension harmonic breathing wobble
            val wobble = sin(timeSec * 5.0 + seed).toFloat() * 0.08f
            val radius = baseRadius * (1f + wobble)

            val alphaVal = ((1f - phase) * 230 * f.opacity).toInt().coerceIn(0, 255)

            if (phase < 0.86f) {
                // Translucent Liquid Bubble Interior
                val brightAqua = lighten(f.color, 0.65f)
                bubbleShellPaint.shader = RadialGradient(
                    center.first - radius * 0.28f, center.second - radius * 0.28f, radius * 1.5f,
                    intArrayOf(alpha(brightAqua, (alphaVal * 0.45f).toInt()), alpha(f.color, (alphaVal * 0.18f).toInt()), Color.TRANSPARENT),
                    floatArrayOf(0f, 0.70f, 1f),
                    Shader.TileMode.CLAMP
                )
                f.canvas.drawCircle(center.first, center.second, radius, bubbleShellPaint)

                // Iridescent Thin-Film Rim Highlight
                bubbleRimPaint.strokeWidth = (1.5f * f.size).coerceAtLeast(1.0f)
                val rimTone = if (i % 2 == 0) 0xFF80D8FF.toInt() else 0xFFE040FB.toInt()
                bubbleRimPaint.color = alpha(rimTone, (alphaVal * 0.75f).toInt())
                f.canvas.drawCircle(center.first, center.second, radius, bubbleRimPaint)

                // Crescent Specular Light Glint
                glintPaint.color = alpha(Color.WHITE, (alphaVal * 0.90f).toInt())
                f.canvas.drawCircle(center.first - radius * 0.35f, center.second - radius * 0.35f, (radius * 0.26f).coerceAtLeast(1.2f), glintPaint)
            } else {
                // Natural Pop: Dissolving Ethereal Ripple Ring
                val popPhase = (phase - 0.86f) / 0.14f
                val popRadius = radius + popPhase * 9f * f.size
                val popAlpha = ((1f - popPhase) * 190 * f.opacity).toInt().coerceIn(0, 255)

                popPaint.strokeWidth = (1.6f * (1f - popPhase)).coerceAtLeast(0.6f)
                popPaint.color = alpha(Color.WHITE, popAlpha)
                f.canvas.drawCircle(center.first, center.second, popRadius, popPaint)
            }
        }

        wavePaint.shader = null
        bubbleShellPaint.shader = null
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
