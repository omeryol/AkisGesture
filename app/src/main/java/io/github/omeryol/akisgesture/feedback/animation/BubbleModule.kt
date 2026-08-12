package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.sin

class BubbleModule : NaturalAnimationModule {
    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glintPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val depth = (16f + f.stretch * 1.18f).coerceAtMost(360f * f.size)
        val span = (45f + f.progress * 180f) * f.size

        // ── 12 Glossy Water Bubbles ──
        for (i in 0..11) {
            val seed = i * 31.4f
            val cycle = 1.0 + (i % 4) * 0.2
            val life = ((timeSec * 0.95 + seed) % cycle) / cycle
            val phase = life.toFloat()

            val d = depth * (0.15f + phase * 0.9f)
            val sway = sin(phase * PI * 3.0 + seed).toFloat() * (span * 0.4f)
            val along = f.touch + sway

            val center = when (f.edge) {
                Edge.LEFT -> d to along
                Edge.RIGHT -> (f.width - d) to along
                Edge.BOTTOM -> along to (f.height - d)
            }

            val radius = (6f + phase * 16f) * f.size
            val alpha = ((1f - phase) * 220 * f.opacity).toInt().coerceIn(0, 255)

            // Glossy 3D Bubble Shell
            bubblePaint.shader = RadialGradient(
                center.first - radius * 0.35f, center.second - radius * 0.35f, radius * 1.5f,
                intArrayOf(withAlpha(lighten(f.color, 0.85f), alpha), withAlpha(f.color, (alpha * 0.6f).toInt()), withAlpha(darken(f.color, 0.4f), (alpha * 0.2f).toInt())),
                floatArrayOf(0f, 0.65f, 1f),
                Shader.TileMode.CLAMP
            )
            f.canvas.drawCircle(center.first, center.second, radius, bubblePaint)

            // Specular Highlight Spot
            glintPaint.color = withAlpha(Color.WHITE, (alpha * 0.9f).toInt())
            val gX = center.first - radius * 0.35f
            val gY = center.second - radius * 0.35f
            f.canvas.drawCircle(gX, gY, (radius * 0.30f).coerceAtLeast(1.5f), glintPaint)
        }

        bubblePaint.shader = null
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
    private fun lighten(c: Int, t: Float) = Color.rgb((Color.red(c) + (255 - Color.red(c)) * t).toInt().coerceIn(0, 255), (Color.green(c) + (255 - Color.green(c)) * t).toInt().coerceIn(0, 255), (Color.blue(c) + (255 - Color.blue(c)) * t).toInt().coerceIn(0, 255))
    private fun darken(c: Int, t: Float) = Color.rgb((Color.red(c) * (1f - t)).toInt().coerceIn(0, 255), (Color.green(c) * (1f - t)).toInt().coerceIn(0, 255), (Color.blue(c) * (1f - t)).toInt().coerceIn(0, 255))
}
