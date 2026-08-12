package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.sin

class GlassRefractionModule : NaturalAnimationModule {
    private val glassPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val mainPath = Path()
    private val rimPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val depth = (14f + f.stretch * 1.18f).coerceAtMost(380f * f.size)
        val span = (40f + f.progress * 200f) * f.size

        // ── 1. Frosted Glass Body Path ──
        mainPath.reset()
        rimPath.reset()
        val steps = 35
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat()
            val shard = (sin(u * PI * 6.0 + timeSec * 2.0) * 4.0).toFloat()
            val d = (depth + shard) * env

            val p = when (f.edge) {
                Edge.LEFT -> d to (f.touch - span + u * span * 2f)
                Edge.RIGHT -> (f.width - d) to (f.touch - span + u * span * 2f)
                Edge.BOTTOM -> (f.touch - span + u * span * 2f) to (f.height - d)
            }

            if (i == 0) {
                mainPath.moveTo(p.first, p.second)
                rimPath.moveTo(p.first, p.second)
            } else {
                mainPath.lineTo(p.first, p.second)
                rimPath.lineTo(p.first, p.second)
            }
        }

        when (f.edge) {
            Edge.LEFT -> { mainPath.lineTo(0f, f.touch + span); mainPath.lineTo(0f, f.touch - span) }
            Edge.RIGHT -> { mainPath.lineTo(f.width, f.touch + span); mainPath.lineTo(f.width, f.touch - span) }
            Edge.BOTTOM -> { mainPath.lineTo(f.touch + span, f.height); mainPath.lineTo(f.touch - span, f.height) }
        }
        mainPath.close()

        // ── 2. Frosted Glass Shading ──
        glassPaint.shader = glassGradient(
            f, depth,
            withAlpha(lighten(f.color, 0.70f), (230 * f.opacity).toInt()),
            withAlpha(f.color, (140 * f.opacity).toInt())
        )
        f.canvas.drawPath(mainPath, glassPaint)

        // ── 3. Dual Specular Prism Rim Highlights ──
        rimPaint.strokeWidth = 3.5f * f.size
        rimPaint.color = withAlpha(Color.WHITE, (245 * f.opacity).toInt())
        f.canvas.drawPath(rimPath, rimPaint)

        glassPaint.shader = null
    }

    private fun glassGradient(f: AnimationFrame, depth: Float, c1: Int, c2: Int) = when (f.edge) {
        Edge.LEFT -> LinearGradient(0f, f.touch, depth, f.touch, c1, c2, Shader.TileMode.CLAMP)
        Edge.RIGHT -> LinearGradient(f.width, f.touch, f.width - depth, f.touch, c1, c2, Shader.TileMode.CLAMP)
        Edge.BOTTOM -> LinearGradient(f.touch, f.height, f.touch, f.height - depth, c1, c2, Shader.TileMode.CLAMP)
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
    private fun lighten(c: Int, t: Float) = Color.rgb((Color.red(c) + (255 - Color.red(c)) * t).toInt().coerceIn(0, 255), (Color.green(c) + (255 - Color.green(c)) * t).toInt().coerceIn(0, 255), (Color.blue(c) + (255 - Color.blue(c)) * t).toInt().coerceIn(0, 255))
}
