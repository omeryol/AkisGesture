package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos

class VortexModule : NaturalAnimationModule {
    private val abyssPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val spiralPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val spiralPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val depth = (18f + f.stretch * 1.20f).coerceAtMost(380f * f.size)
        val maxRadius = (35f + f.progress * 130f) * f.size

        val center = when (f.edge) {
            Edge.LEFT -> depth to f.touch
            Edge.RIGHT -> (f.width - depth) to f.touch
            Edge.BOTTOM -> f.touch to (f.height - depth)
        }

        // ── 1. Vortex Central Abyss Core ──
        abyssPaint.shader = RadialGradient(
            center.first, center.second, maxRadius,
            intArrayOf(withAlpha(darken(f.color, 0.7f), (240 * f.opacity).toInt()), withAlpha(f.color, (160 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawCircle(center.first, center.second, maxRadius, abyssPaint)

        // ── 2. Logarithmic Spiral Arms (3 Arms) ──
        spiralPath.reset()
        for (arm in 0..2) {
            val armOffset = (arm * 2 * PI / 3.0)
            val steps = 30
            for (i in 0..steps) {
                val t = i / steps.toFloat()
                val r = t * maxRadius
                val angle = t * PI * 3.5 + timeSec * 3.5 + armOffset

                val x = center.first + (r * cos(angle)).toFloat()
                val y = center.second + (r * sin(angle)).toFloat()

                if (i == 0) spiralPath.moveTo(x, y) else spiralPath.lineTo(x, y)
            }
        }
        spiralPaint.strokeWidth = 3.0f * f.size
        spiralPaint.color = withAlpha(lighten(f.color, 0.65f), (220 * f.opacity).toInt())
        f.canvas.drawPath(spiralPath, spiralPaint)

        // ── 3. Rotating Suction Particles (12 Particles) ──
        for (i in 0..11) {
            val phase = ((timeSec * 0.9 + i * 0.083) % 1.0).toFloat()
            val r = (1f - phase) * maxRadius
            val angle = phase * PI * 4.0 + i

            val px = center.first + (r * cos(angle)).toFloat()
            val py = center.second + (r * sin(angle)).toFloat()
            val pAlpha = ((phase * 230) * f.opacity).toInt().coerceIn(0, 255)

            particlePaint.color = withAlpha(lighten(f.color, 0.8f), pAlpha)
            f.canvas.drawCircle(px, py, (2.5f + phase * 2f) * f.size, particlePaint)
        }

        abyssPaint.shader = null
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
    private fun lighten(c: Int, t: Float) = Color.rgb((Color.red(c) + (255 - Color.red(c)) * t).toInt().coerceIn(0, 255), (Color.green(c) + (255 - Color.green(c)) * t).toInt().coerceIn(0, 255), (Color.blue(c) + (255 - Color.blue(c)) * t).toInt().coerceIn(0, 255))
    private fun darken(c: Int, t: Float) = Color.rgb((Color.red(c) * (1f - t)).toInt().coerceIn(0, 255), (Color.green(c) * (1f - t)).toInt().coerceIn(0, 255), (Color.blue(c) * (1f - t)).toInt().coerceIn(0, 255))
}
