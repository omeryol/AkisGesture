package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.sin

class RainModule : NaturalAnimationModule {
    private val streakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val ripplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val streakPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val depth = (16f + f.stretch * 1.15f).coerceAtMost(360f * f.size)
        val span = (45f + f.progress * 180f) * f.size

        // ── 1. Splash Ripple Rings at Touch Origin ──
        for (ring in 1..3) {
            val phase = ((timeSec * 1.2 + ring * 0.3) % 1.0).toFloat()
            val r = phase * span * 0.75f
            val alpha = ((1f - phase) * 180 * f.opacity).toInt().coerceIn(0, 255)

            val origin = when (f.edge) {
                Edge.LEFT -> (depth * 0.4f) to f.touch
                Edge.RIGHT -> (f.width - depth * 0.4f) to f.touch
                Edge.BOTTOM -> f.touch to (f.height - depth * 0.4f)
            }

            ripplePaint.strokeWidth = (2.5f * (1f - phase)).coerceAtLeast(1f) * f.size
            ripplePaint.color = withAlpha(lighten(f.color, 0.75f), alpha)
            f.canvas.drawCircle(origin.first, origin.second, r, ripplePaint)
        }

        // ── 2. Rainfall Motion Streak Lines (16 Rain Lines) ──
        streakPath.reset()
        val numLines = 16
        for (i in 0 until numLines) {
            val seed = i * 41.7f
            val phase = ((timeSec * 2.2 + seed) % 1.0).toFloat()
            val along = f.touch - span + (i / numLines.toFloat()) * span * 2f
            val len = (25f + phase * 35f) * f.size

            val startD = phase * depth * 1.2f
            val endD = startD + len

            val p1 = when (f.edge) {
                Edge.LEFT -> startD to along
                Edge.RIGHT -> (f.width - startD) to along
                Edge.BOTTOM -> along to (f.height - startD)
            }
            val p2 = when (f.edge) {
                Edge.LEFT -> endD to along
                Edge.RIGHT -> (f.width - endD) to along
                Edge.BOTTOM -> along to (f.height - endD)
            }

            streakPath.moveTo(p1.first, p1.second)
            streakPath.lineTo(p2.first, p2.second)
        }
        streakPaint.strokeWidth = 2.2f * f.size
        streakPaint.color = withAlpha(lighten(f.color, 0.85f), (220 * f.opacity).toInt())
        f.canvas.drawPath(streakPath, streakPaint)
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
    private fun lighten(c: Int, t: Float) = Color.rgb((Color.red(c) + (255 - Color.red(c)) * t).toInt().coerceIn(0, 255), (Color.green(c) + (255 - Color.green(c)) * t).toInt().coerceIn(0, 255), (Color.blue(c) + (255 - Color.blue(c)) * t).toInt().coerceIn(0, 255))
}
