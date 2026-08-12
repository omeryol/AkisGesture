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

class NightModule : NaturalAnimationModule {
    private val abyssPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val suctionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val suctionPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val depth = (18f + f.stretch * 1.20f).coerceAtMost(380f * f.size)
        val radius = (25f + f.progress * 110f) * f.size

        val center = when (f.edge) {
            Edge.LEFT -> depth to f.touch
            Edge.RIGHT -> (f.width - depth) to f.touch
            Edge.BOTTOM -> f.touch to (f.height - depth)
        }

        // ── 1. Gravitational Abyss Core ──
        abyssPaint.shader = RadialGradient(
            center.first, center.second, radius * 1.6f,
            intArrayOf(0xFF000000.toInt(), withAlpha(0xFF1A237E.toInt(), (220 * f.opacity).toInt()), withAlpha(0xFF3D5AFE.toInt(), (120 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 0.45f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawCircle(center.first, center.second, radius * 1.6f, abyssPaint)

        // ── 2. Accreting Event Horizon Light Ring ──
        ringPaint.strokeWidth = 3.5f * f.size
        ringPaint.color = withAlpha(0xFF80D8FF.toInt(), (235 * f.opacity).toInt())
        f.canvas.drawCircle(center.first, center.second, radius * 0.75f, ringPaint)

        // ── 3. Inward Gravitational Suction Lines (8 Rays) ──
        suctionPath.reset()
        for (i in 0..7) {
            val angle = (i.toFloat() / 8f) * (2 * PI) + timeSec * 1.2
            val phase = ((timeSec * 1.5 + i * 0.125) % 1.0).toFloat()

            val outerR = radius * (1.1f + (1f - phase) * 0.8f)
            val innerR = radius * 0.75f

            val p1 = (center.first + outerR * cos(angle)).toFloat() to (center.second + outerR * sin(angle)).toFloat()
            val p2 = (center.first + innerR * cos(angle)).toFloat() to (center.second + innerR * sin(angle)).toFloat()

            suctionPath.moveTo(p1.first, p1.second)
            suctionPath.lineTo(p2.first, p2.second)
        }
        suctionPaint.strokeWidth = 2.2f * f.size
        suctionPaint.color = withAlpha(0xFFFFFFFF.toInt(), (200 * f.opacity).toInt())
        f.canvas.drawPath(suctionPath, suctionPaint)

        abyssPaint.shader = null
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
}
