package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos

class MistModule : NaturalAnimationModule {
    private val mistPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val puffPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val depth = (16f + f.stretch * 1.15f).coerceAtMost(360f * f.size)
        val span = (50f + f.progress * 180f) * f.size

        val center = when (f.edge) {
            Edge.LEFT -> depth to f.touch
            Edge.RIGHT -> (f.width - depth) to f.touch
            Edge.BOTTOM -> f.touch to (f.height - depth)
        }

        // ── 1. Ambient Fog Aura ──
        val mainRadius = span * 1.1f
        mistPaint.shader = RadialGradient(
            center.first, center.second, mainRadius,
            intArrayOf(withAlpha(lighten(f.color, 0.5f), (160 * f.opacity).toInt()), withAlpha(f.color, (90 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawCircle(center.first, center.second, mainRadius, mistPaint)

        // ── 2. Drifting Soft Smoke Puffs (8 Puffs) ──
        for (i in 0..7) {
            val phase = ((timeSec * 0.4 + i * 0.125) % 1.0).toFloat()
            val offsetX = sin(timeSec * 0.9 + i) * (span * 0.55)
            val offsetY = (phase - 0.5f) * (depth * 0.8f)

            val p = when (f.edge) {
                Edge.LEFT -> (depth * 0.5f + offsetY).toFloat() to (f.touch + offsetX).toFloat()
                Edge.RIGHT -> (f.width - depth * 0.5f - offsetY).toFloat() to (f.touch + offsetX).toFloat()
                Edge.BOTTOM -> (f.touch + offsetX).toFloat() to (f.height - depth * 0.5f - offsetY).toFloat()
            }

            val pRadius = (28f + phase * 45f) * f.size
            val pAlpha = ((1f - phase) * 130 * f.opacity).toInt().coerceIn(0, 255)

            puffPaint.shader = RadialGradient(
                p.first, p.second, pRadius,
                withAlpha(lighten(f.color, 0.7f), pAlpha),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
            f.canvas.drawCircle(p.first, p.second, pRadius, puffPaint)
        }

        mistPaint.shader = null
        puffPaint.shader = null
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
    private fun lighten(c: Int, t: Float) = Color.rgb((Color.red(c) + (255 - Color.red(c)) * t).toInt().coerceIn(0, 255), (Color.green(c) + (255 - Color.green(c)) * t).toInt().coerceIn(0, 255), (Color.blue(c) + (255 - Color.blue(c)) * t).toInt().coerceIn(0, 255))
}
