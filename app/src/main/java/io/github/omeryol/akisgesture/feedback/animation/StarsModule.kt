package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.sin

class StarsModule : NaturalAnimationModule {
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val nebulaPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val trailPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val depth = (20f + f.stretch * 1.25f).coerceAtMost(400f * f.size)
        val span = (50f + f.progress * 180f) * f.size

        val center = when (f.edge) {
            Edge.LEFT -> depth to f.touch
            Edge.RIGHT -> (f.width - depth) to f.touch
            Edge.BOTTOM -> f.touch to (f.height - depth)
        }

        // ── 1. Galactic Nebula Aura ──
        val nebulaRadius = span * 1.2f
        nebulaPaint.shader = RadialGradient(
            center.first, center.second, nebulaRadius,
            intArrayOf(withAlpha(0xFF7C4DFF.toInt(), (140 * f.opacity).toInt()), withAlpha(0xFF00E5FF.toInt(), (70 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawCircle(center.first, center.second, nebulaRadius, nebulaPaint)

        // ── 2. Twinkling Stars (18 Stars) ──
        for (i in 0..17) {
            val seed = i * 29.3f
            val phase = ((timeSec * 0.8 + seed) % 1.0).toFloat()
            val along = f.touch - span + (i / 17f) * span * 2f
            val d = depth * (0.2f + sin(seed + timeSec).toFloat() * 0.4f + 0.4f)

            val p = when (f.edge) {
                Edge.LEFT -> d to along
                Edge.RIGHT -> (f.width - d) to along
                Edge.BOTTOM -> along to (f.height - d)
            }

            val twinkle = (sin(phase * PI * 2) * 0.4 + 0.6).toFloat()
            val starRadius = (2.5f + twinkle * 3f) * f.size
            val alpha = ((twinkle * 240) * f.opacity).toInt().coerceIn(0, 255)

            starPaint.color = withAlpha(if (i % 3 == 0) 0xFFFFFFFF.toInt() else 0xFF80D8FF.toInt(), alpha)
            f.canvas.drawCircle(p.first, p.second, starRadius, starPaint)

            // Meteor streak for every 4th star
            if (i % 4 == 0 && phase > 0.4f) {
                trailPath.reset()
                trailPath.moveTo(p.first, p.second)
                val tailX = p.first - 15f * f.size
                val tailY = p.second - 15f * f.size
                trailPath.lineTo(tailX, tailY)

                trailPaint.strokeWidth = 2.0f * f.size
                trailPaint.color = withAlpha(0xFFFFFFFF.toInt(), (alpha * 0.6f).toInt())
                f.canvas.drawPath(trailPath, trailPaint)
            }
        }

        nebulaPaint.shader = null
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
}
