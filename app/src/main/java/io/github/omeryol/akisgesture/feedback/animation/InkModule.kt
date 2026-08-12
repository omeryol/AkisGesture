package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.sin

class InkModule : NaturalAnimationModule {
    private val inkPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val blobPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val inkPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val depth = (16f + f.stretch * 1.18f).coerceAtMost(360f * f.size)
        val span = (45f + f.progress * 190f) * f.size

        // ── 1. Main Ink Diffusion Body ──
        inkPath.reset()
        val steps = 36
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat()
            val bloom = (sin(u * PI * 5.0 + timeSec * 2.5) * 8.0 + sin(u * PI * 11.0 - timeSec * 1.5) * 4.0).toFloat()
            val totalDepth = (depth + bloom) * env

            val p = when (f.edge) {
                Edge.LEFT -> totalDepth to (f.touch - span + u * span * 2f)
                Edge.RIGHT -> (f.width - totalDepth) to (f.touch - span + u * span * 2f)
                Edge.BOTTOM -> (f.touch - span + u * span * 2f) to (f.height - totalDepth)
            }

            if (i == 0) inkPath.moveTo(p.first, p.second) else inkPath.lineTo(p.first, p.second)
        }

        when (f.edge) {
            Edge.LEFT -> { inkPath.lineTo(0f, f.touch + span); inkPath.lineTo(0f, f.touch - span) }
            Edge.RIGHT -> { inkPath.lineTo(f.width, f.touch + span); inkPath.lineTo(f.width, f.touch - span) }
            Edge.BOTTOM -> { inkPath.lineTo(f.touch + span, f.height); inkPath.lineTo(f.touch - span, f.height) }
        }
        inkPath.close()

        inkPaint.shader = RadialGradient(
            point(f, depth * 0.5f).first, point(f, depth * 0.5f).second, depth * 1.3f,
            intArrayOf(withAlpha(darken(f.color, 0.4f), (245 * f.opacity).toInt()), withAlpha(f.color, (180 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawPath(inkPath, inkPaint)

        // ── 2. Organic Ink Tendril Blobs (6 Tendrils) ──
        for (b in 0..5) {
            val offset = (b - 2.5f) * (span * 0.32f)
            val bDepth = depth * (0.6f + sin(timeSec * 2.0 + b).toFloat() * 0.35f)

            val center = when (f.edge) {
                Edge.LEFT -> bDepth to (f.touch + offset)
                Edge.RIGHT -> (f.width - bDepth) to (f.touch + offset)
                Edge.BOTTOM -> (f.touch + offset) to (f.height - bDepth)
            }

            val bRadius = (12f + sin(timeSec * 3.0 + b).toFloat() * 6f) * f.size
            blobPaint.shader = RadialGradient(
                center.first, center.second, bRadius,
                withAlpha(darken(f.color, 0.5f), (210 * f.opacity).toInt()),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
            f.canvas.drawCircle(center.first, center.second, bRadius, blobPaint)
        }

        inkPaint.shader = null
        blobPaint.shader = null
    }

    private fun point(f: AnimationFrame, depth: Float) = when (f.edge) {
        Edge.LEFT -> depth to f.touch
        Edge.RIGHT -> (f.width - depth) to f.touch
        Edge.BOTTOM -> f.touch to (f.height - depth)
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
    private fun darken(c: Int, t: Float) = Color.rgb((Color.red(c) * (1f - t)).toInt().coerceIn(0, 255), (Color.green(c) * (1f - t)).toInt().coerceIn(0, 255), (Color.blue(c) * (1f - t)).toInt().coerceIn(0, 255))
}
