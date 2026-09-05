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
 * Hydro-Kinetic Supersonic Shockwave & Elastic Density Wave.
 * Renders an aerodynamic fluid compression wave expanding from the bezel with
 * harmonic density crests, luminous shock fronts, and high-energy condensation ionization.
 */
class PressureWaveModule : NaturalAnimationModule {
    private val shockBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val waveFrontPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val ionPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val shockPath = Path()
    private val frontPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.3f)
        val depth = (18f + f.stretch * 1.20f).coerceAtMost(380f * f.size)
        val span = (46f + f.progress * 210f) * f.size

        // ── 1. Fluid Compression Shock Body ──
        shockPath.reset()
        val steps = 44
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat().pow(1.6f)
            val flutter = sin(u * PI * 3.5 + timeSec * 3.5) * (3.0 + growth * 5.0)
            val d = (depth + flutter.toFloat()) * env

            val p = point(f, f.touch - span + u * span * 2f, d)
            if (i == 0) shockPath.moveTo(p.first, p.second) else shockPath.lineTo(p.first, p.second)
        }
        close(shockPath, f, span)

        val shockColor = blend(f.color, 0xFF00E5FF.toInt(), 0.40f)
        shockBodyPaint.shader = gradient(
            f, depth,
            alpha(lighten(shockColor, 0.45f), (210 * f.opacity).toInt()),
            alpha(darken(f.color, 0.3f), (80 * f.opacity).toInt())
        )
        f.canvas.drawPath(shockPath, shockBodyPaint)

        // ── 2. Concentric Supersonic Density Crest Wavefronts (3 Crests) ──
        val crestCount = 3
        for (c in 0 until crestCount) {
            frontPath.reset()
            val cFraction = 0.65f + c * 0.18f
            val cDepth = depth * cFraction
            val cSpan = span * (0.60f + c * 0.20f)
            val cSteps = 28

            for (i in 0..cSteps) {
                val u = i / cSteps.toFloat()
                val env = sin(PI * u).toFloat().pow(1.8f)
                val pulse = sin(u * PI * 2.0 - timeSec * 4.0 + c * 1.5) * (2.5f * f.size)
                val d = (cDepth + pulse.toFloat()) * env

                val p = point(f, f.touch - cSpan + u * cSpan * 2f, d)
                if (i == 0) frontPath.moveTo(p.first, p.second) else frontPath.lineTo(p.first, p.second)
            }

            val strokeW = (2.6f - c * 0.6f).coerceAtLeast(1f) * f.size
            val frontAlpha = ((230 - c * 50) * f.opacity).toInt().coerceIn(0, 255)
            waveFrontPaint.strokeWidth = strokeW
            waveFrontPaint.color = alpha(Color.WHITE, frontAlpha)
            f.canvas.drawPath(frontPath, waveFrontPaint)
        }

        // ── 3. High-Energy Condensation Ionization Droplets ──
        val ionCount = 8
        for (i in 0 until ionCount) {
            val seed = i * 37.3f
            val phase = ((timeSec * 0.9 + seed) % 1.0).toFloat()
            val along = f.touch - span * 0.6f + (i / (ionCount - 1).toFloat()) * span * 1.2f
            val env = sin(PI * (i / (ionCount - 1).toFloat())).toFloat().pow(1.6f)
            val d = depth * (0.85f + phase * 0.25f) * env

            val p = point(f, along, d)
            val ionAlpha = ((sin(phase * PI) * 230) * f.opacity).toInt().coerceIn(0, 255)
            val ionRadius = (1.8f + (1f - phase) * 2.2f) * f.size

            ionPaint.color = alpha(Color.WHITE, ionAlpha)
            f.canvas.drawCircle(p.first, p.second, ionRadius, ionPaint)
        }

        shockBodyPaint.shader = null
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
