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
 * Cosmic Starfield & Deep Stellar Parallax Stream.
 * Renders an interstellar nebula envelope expanding from the bezel with
 * depth-projected twinkling stars, luminous comet tails, and cosmic dust clouds.
 */
class StarsModule : NaturalAnimationModule {
    private val nebulaPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val nebulaPath = Path()
    private val tailPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.3f)
        val depth = (18f + f.stretch * 1.22f).coerceAtMost(380f * f.size)
        val span = (48f + f.progress * 210f) * f.size

        // ── 1. Interstellar Nebula Dust Cloud ──
        nebulaPath.reset()
        val steps = 36
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat().pow(1.4f)
            val wave = sin(u * PI * 3.5 + timeSec * 2.2) * (7.0 + depth * 0.08)
            val d = (depth * 0.90f + wave.toFloat()) * env

            val p = point(f, f.touch - span + u * span * 2f, d)
            if (i == 0) nebulaPath.moveTo(p.first, p.second) else nebulaPath.lineTo(p.first, p.second)
        }
        close(nebulaPath, f, span)

        val cosmicViolet = 0xFF7C4DFF.toInt()
        val cosmicCyan = 0xFF00E5FF.toInt()
        nebulaPaint.shader = gradient(
            f, depth * 0.90f,
            alpha(cosmicViolet, (160 * f.opacity).toInt()),
            alpha(cosmicCyan, (60 * f.opacity).toInt())
        )
        f.canvas.drawPath(nebulaPath, nebulaPaint)

        // ── 2. Parallax Cosmic Stars & Streaming Tails ──
        val starCount = 18
        for (i in 0 until starCount) {
            val seed = i * 23.9f
            val phase = ((timeSec * 0.7 + seed) % 1.0).toFloat()
            // Radial dispersion outward from the edge
            val alongSpread = (i / (starCount - 1).toFloat() - 0.5f) * 2f
            val along = f.touch + alongSpread * (span * (0.4f + phase * 0.6f))
            val starDepth = depth * (0.15f + phase * 0.95f)

            val p = point(f, along, starDepth)
            val twinkle = (sin(phase * PI * 2.0 + seed) * 0.4 + 0.6).toFloat()
            val starRadius = (1.5f + twinkle * (2.2f + (i % 3) * 0.8f)) * f.size
            val starAlpha = ((twinkle * 245) * f.opacity).toInt().coerceIn(0, 255)

            // Star Core Glow
            starPaint.color = alpha(if (i % 3 == 0) Color.WHITE else 0xFF80D8FF.toInt(), starAlpha)
            f.canvas.drawCircle(p.first, p.second, starRadius, starPaint)

            // Stream Tail for Fast Stars (pointing back toward the edge)
            if (i % 3 == 0 && phase > 0.25f) {
                tailPath.reset()
                tailPath.moveTo(p.first, p.second)
                val tailLen = (12f + phase * 22f) * f.size
                val tailOrigin = point(f, along - alongSpread * 4f * f.size, (starDepth - tailLen).coerceAtLeast(0f))
                tailPath.lineTo(tailOrigin.first, tailOrigin.second)

                tailPaint.strokeWidth = (1.6f * (1f - phase * 0.5f)) * f.size
                tailPaint.color = alpha(Color.WHITE, (starAlpha * 0.65f).toInt())
                f.canvas.drawPath(tailPath, tailPaint)
            }
        }

        nebulaPaint.shader = null
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
