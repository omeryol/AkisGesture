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
 * Kinetic Rainfall & Glass Water Sheen.
 * Renders an organic water-slick film on the screen edge with slanted rainfall streaks,
 * fluid velocity variations, and micro impact ripples on the display glass.
 */
class RainModule : NaturalAnimationModule {
    private val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val streakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val ripplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val sheenPath = Path()
    private val streakPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.3f)
        val depth = (16f + f.stretch * 1.15f).coerceAtMost(360f * f.size)
        val span = (46f + f.progress * 200f) * f.size

        // ── 1. Water Slick Film (Rain Sheen on Glass) ──
        sheenPath.reset()
        val steps = 36
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat().pow(1.6f)
            val wave = sin(u * PI * 5.0 + timeSec * 3.2) * (4.0 + depth * 0.05)
            val d = (depth * 0.45f + wave.toFloat()) * env

            val p = point(f, f.touch - span + u * span * 2f, d)
            if (i == 0) sheenPath.moveTo(p.first, p.second) else sheenPath.lineTo(p.first, p.second)
        }
        close(sheenPath, f, span)

        val rainTone = blend(f.color, 0xFF80D8FF.toInt(), 0.6f)
        sheenPaint.shader = gradient(
            f, depth * 0.45f,
            alpha(rainTone, (160 * f.opacity).toInt()),
            alpha(darken(f.color, 0.3f), (60 * f.opacity).toInt())
        )
        f.canvas.drawPath(sheenPath, sheenPaint)

        // ── 2. Slanted Kinetic Rain Streaks (14 Rain Droplet Trails) ──
        streakPath.reset()
        val streakCount = 14
        for (i in 0 until streakCount) {
            val seed = i * 29.3f
            val speed = 2.2f + (i % 4) * 0.6f
            val phase = ((timeSec * speed + seed) % 1.0).toFloat()
            val along = f.touch - span * 0.8f + (i / (streakCount - 1).toFloat()) * span * 1.6f
            val streakLen = (20f + phase * 40f) * f.size

            val startD = phase * depth * 1.1f
            val endD = startD + streakLen
            // Slight downward slant angle across screen
            val slant = streakLen * 0.35f

            val p1 = point(f, along, startD)
            val p2 = point(f, along + slant, endD)

            streakPath.moveTo(p1.first, p1.second)
            streakPath.lineTo(p2.first, p2.second)
        }
        streakPaint.strokeWidth = 2.0f * f.size
        streakPaint.color = alpha(Color.WHITE, (200 * f.opacity).toInt())
        f.canvas.drawPath(streakPath, streakPaint)

        // ── 3. Droplet Impact Splash Rings on Glass ──
        val impactCount = 3
        for (r in 0 until impactCount) {
            val seed = r * 0.33f
            val phase = ((timeSec * 1.4 + seed) % 1.0).toFloat()
            val ringRadius = phase * (32f + r * 14f) * f.size
            val ringAlpha = ((1f - phase) * 195 * f.opacity).toInt().coerceIn(0, 255)
            val ringOrigin = point(f, f.touch - span * 0.3f + r * (span * 0.3f), depth * 0.3f)

            ripplePaint.strokeWidth = (2.0f * (1f - phase * 0.6f)).coerceAtLeast(0.8f) * f.size
            ripplePaint.color = alpha(lighten(rainTone, 0.7f), ringAlpha)
            f.canvas.drawCircle(ringOrigin.first, ringOrigin.second, ringRadius, ripplePaint)
        }

        sheenPaint.shader = null
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
