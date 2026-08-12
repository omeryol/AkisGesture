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

class SunModule : NaturalAnimationModule {
    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val coronaPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val rayPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val depth = (18f + f.stretch * 1.22f).coerceAtMost(380f * f.size)
        val radius = (20f + f.progress * 85f) * f.size

        val center = when (f.edge) {
            Edge.LEFT -> depth to f.touch
            Edge.RIGHT -> (f.width - depth) to f.touch
            Edge.BOTTOM -> f.touch to (f.height - depth)
        }

        // ── 1. Outer Corona Glow ──
        val coronaRadius = radius * 2.3f
        coronaPaint.shader = RadialGradient(
            center.first, center.second, coronaRadius,
            intArrayOf(withAlpha(0xFFFFAB00.toInt(), (210 * f.opacity).toInt()), withAlpha(0xFFFF3D00.toInt(), (120 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawCircle(center.first, center.second, coronaRadius, coronaPaint)

        // ── 2. Rotating Corona Rays (12 Rays) ──
        rayPath.reset()
        val numRays = 12
        for (i in 0 until numRays) {
            val angle = (i.toFloat() / numRays) * (2 * PI) + timeSec * 0.75
            val rayLen = radius * (1.3f + sin(timeSec * 3.0 + i) * 0.35f)

            val x1 = center.first + (radius * 0.85f * cos(angle)).toFloat()
            val y1 = center.second + (radius * 0.85f * sin(angle)).toFloat()
            val x2 = center.first + (rayLen * cos(angle)).toFloat()
            val y2 = center.second + (rayLen * sin(angle)).toFloat()

            rayPath.moveTo(x1, y1)
            rayPath.lineTo(x2, y2)
        }
        rayPaint.strokeWidth = 3.5f * f.size
        rayPaint.color = withAlpha(0xFFFFD600.toInt(), (230 * f.opacity).toInt())
        f.canvas.drawPath(rayPath, rayPaint)

        // ── 3. Incandescent Solar Core ──
        corePaint.shader = RadialGradient(
            center.first, center.second, radius,
            intArrayOf(withAlpha(0xFFFFFFFF.toInt(), (255 * f.opacity).toInt()), withAlpha(0xFFFFEA00.toInt(), (230 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 0.65f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawCircle(center.first, center.second, radius, corePaint)

        corePaint.shader = null
        coronaPaint.shader = null
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
}
