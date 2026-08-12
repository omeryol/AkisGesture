package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.sin

class PressureWaveModule : NaturalAnimationModule {
    private val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val mainPath = Path()
    private val shockPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val depth = (18f + f.stretch * 1.25f).coerceAtMost(400f * f.size)
        val span = (50f + f.progress * 220f) * f.size

        // ── 1. Main Hydraulic Pressure Wave Body ──
        mainPath.reset()
        shockPath.reset()
        val steps = 40
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat()
            val shock = (sin(u * PI * 4.0 + timeSec * 4.0) * 6.0).toFloat()
            val d = (depth + shock) * env

            val p = when (f.edge) {
                Edge.LEFT -> d to (f.touch - span + u * span * 2f)
                Edge.RIGHT -> (f.width - d) to (f.touch - span + u * span * 2f)
                Edge.BOTTOM -> (f.touch - span + u * span * 2f) to (f.height - d)
            }

            if (i == 0) {
                mainPath.moveTo(p.first, p.second)
                shockPath.moveTo(p.first, p.second)
            } else {
                mainPath.lineTo(p.first, p.second)
                shockPath.lineTo(p.first, p.second)
            }
        }

        when (f.edge) {
            Edge.LEFT -> { mainPath.lineTo(0f, f.touch + span); mainPath.lineTo(0f, f.touch - span) }
            Edge.RIGHT -> { mainPath.lineTo(f.width, f.touch + span); mainPath.lineTo(f.width, f.touch - span) }
            Edge.BOTTOM -> { mainPath.lineTo(f.touch + span, f.height); mainPath.lineTo(f.touch - span, f.height) }
        }
        mainPath.close()

        mainPaint.shader = gradient(
            f, depth,
            withAlpha(lighten(f.color, 0.55f), (245 * f.opacity).toInt()),
            withAlpha(f.color, (180 * f.opacity).toInt())
        )
        f.canvas.drawPath(mainPath, mainPaint)

        // ── 2. High-Velocity Shock Front Crest ──
        shockPaint.strokeWidth = (4.0f + f.progress * 3.0f) * f.size
        shockPaint.color = withAlpha(lighten(f.color, 0.90f), (250 * f.opacity).toInt())
        f.canvas.drawPath(shockPath, shockPaint)

        mainPaint.shader = null
    }

    private fun gradient(f: AnimationFrame, depth: Float, c1: Int, c2: Int) = when (f.edge) {
        Edge.LEFT -> LinearGradient(0f, f.touch, depth, f.touch, c1, c2, Shader.TileMode.CLAMP)
        Edge.RIGHT -> LinearGradient(f.width, f.touch, f.width - depth, f.touch, c1, c2, Shader.TileMode.CLAMP)
        Edge.BOTTOM -> LinearGradient(f.touch, f.height, f.touch, f.height - depth, c1, c2, Shader.TileMode.CLAMP)
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
    private fun lighten(c: Int, t: Float) = Color.rgb((Color.red(c) + (255 - Color.red(c)) * t).toInt().coerceIn(0, 255), (Color.green(c) + (255 - Color.green(c)) * t).toInt().coerceIn(0, 255), (Color.blue(c) + (255 - Color.blue(c)) * t).toInt().coerceIn(0, 255))
}
