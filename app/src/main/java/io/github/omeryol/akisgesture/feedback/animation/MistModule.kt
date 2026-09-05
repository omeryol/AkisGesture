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
 * Volumetric Atmospheric Mist & Rolling Vapor Haze.
 * Renders an ethereal, soft-billowing condensation fog bank expanding from the bezel with
 * layered fluid vapor envelopes, ambient light diffusion, and microscopic humidity dew.
 */
class MistModule : NaturalAnimationModule {
    private val outerMistPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val innerMistPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dewPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val outerPath = Path()
    private val innerPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.3f)
        val depth = (18f + f.stretch * 1.15f).coerceAtMost(380f * f.size)
        val span = (50f + f.progress * 220f) * f.size

        // ── 1. Outer Diffuse Vapor Envelope ──
        buildVaporPath(outerPath, f, depth, span, timeSec, speed = 0.8f, phase = 0.0f)
        val softTone = lighten(f.color, 0.55f)
        outerMistPaint.shader = gradient(
            f, depth,
            alpha(softTone, (140 * f.opacity).toInt()),
            Color.TRANSPARENT
        )
        f.canvas.drawPath(outerPath, outerMistPaint)

        // ── 2. Inner Dense Fog Core ──
        buildVaporPath(innerPath, f, depth * 0.65f, span * 0.75f, timeSec + 0.4, speed = 1.1f, phase = 1.5f)
        innerMistPaint.shader = gradient(
            f, depth * 0.65f,
            alpha(lighten(f.color, 0.75f), (210 * f.opacity).toInt()),
            alpha(softTone, (80 * f.opacity).toInt())
        )
        f.canvas.drawPath(innerPath, innerMistPaint)

        // ── 3. Suspended Micro Dew Droplets (Condensation Moisture) ──
        val dewCount = 10
        for (i in 0 until dewCount) {
            val seed = i * 31.7f
            val phase = ((timeSec * 0.45 + seed) % 1.0).toFloat()
            val along = f.touch - span * 0.7f + (i / (dewCount - 1).toFloat()) * span * 1.4f + sin(timeSec * 1.8 + i).toFloat() * (12f * f.size)
            val d = depth * (0.25f + phase * 0.70f)

            val p = point(f, along, d)
            val dewRadius = (1.8f + (1f - phase) * 2.2f) * f.size
            val dewAlpha = ((sin(phase * PI) * 190) * f.opacity).toInt().coerceIn(0, 255)

            dewPaint.color = alpha(Color.WHITE, dewAlpha)
            f.canvas.drawCircle(p.first, p.second, dewRadius, dewPaint)
        }

        outerMistPaint.shader = null
        innerMistPaint.shader = null
    }

    private fun buildVaporPath(path: Path, f: AnimationFrame, depth: Float, span: Float, timeSec: Double, speed: Float, phase: Float) {
        path.reset()
        val steps = 36
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat().pow(1.3f)
            // Soft billowing atmospheric harmonics
            val billow1 = sin(u * PI * 3.0 + timeSec * 1.8 * speed + phase) * (8.0 + depth * 0.08)
            val billow2 = sin(u * PI * 6.5 - timeSec * 1.2 * speed) * (4.0 + depth * 0.04)
            val totalDepth = (depth + (billow1 + billow2).toFloat()) * env

            val p = point(f, f.touch - span + u * span * 2f, totalDepth)
            if (i == 0) path.moveTo(p.first, p.second) else path.lineTo(p.first, p.second)
        }
        close(path, f, span)
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
