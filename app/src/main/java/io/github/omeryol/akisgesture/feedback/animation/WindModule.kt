package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.sin

class WindModule : NaturalAnimationModule {
    private val windPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val windPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val depth = (16f + f.stretch * 1.20f).coerceAtMost(380f * f.size)
        val span = (45f + f.progress * 200f) * f.size

        // ── 1. Aerodynamic Wind Stream Lines (5 Ribbons) ──
        windPath.reset()
        for (line in 0..4) {
            val offset = (line - 2) * (span * 0.35f)
            val steps = 30
            for (i in 0..steps) {
                val u = i / steps.toFloat()
                val d = depth * u
                val turbulence = sin(u * PI * 3.0 + timeSec * 4.0 + line) * (6.0 * (1f - u))
                val along = f.touch + offset + turbulence.toFloat()

                val p = when (f.edge) {
                    Edge.LEFT -> d to along
                    Edge.RIGHT -> (f.width - d) to along
                    Edge.BOTTOM -> along to (f.height - d)
                }

                if (i == 0) windPath.moveTo(p.first, p.second) else windPath.lineTo(p.first, p.second)
            }
        }
        windPaint.strokeWidth = 2.8f * f.size
        windPaint.color = withAlpha(lighten(f.color, 0.75f), (210 * f.opacity).toInt())
        f.canvas.drawPath(windPath, windPaint)

        // ── 2. Fast Comet Particles (12 Particles) ──
        for (i in 0..11) {
            val phase = ((timeSec * 1.8 + i * 0.083) % 1.0).toFloat()
            val d = phase * depth * 1.15f
            val along = f.touch - span * 0.8f + (i / 11f) * span * 1.6f

            val p = when (f.edge) {
                Edge.LEFT -> d to along
                Edge.RIGHT -> (f.width - d) to along
                Edge.BOTTOM -> along to (f.height - d)
            }

            val pAlpha = ((1f - phase) * 230 * f.opacity).toInt().coerceIn(0, 255)
            particlePaint.color = withAlpha(lighten(f.color, 0.85f), pAlpha)
            f.canvas.drawCircle(p.first, p.second, (3.0f * (1f - phase * 0.5f)) * f.size, particlePaint)
        }
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
    private fun lighten(c: Int, t: Float) = Color.rgb((Color.red(c) + (255 - Color.red(c)) * t).toInt().coerceIn(0, 255), (Color.green(c) + (255 - Color.green(c)) * t).toInt().coerceIn(0, 255), (Color.blue(c) + (255 - Color.blue(c)) * t).toInt().coerceIn(0, 255))
}
