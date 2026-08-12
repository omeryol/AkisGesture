package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.feedback.Physics3DEngine
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/** Realistic 3D Water Whirlpool Vortex with spinning fluid currents and inward particle suction. */
class VortexModule : NaturalAnimationModule {
    private val abyssPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val spiralBandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val bandPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.3f)
        val depth = (25f + f.stretch * 1.25f).coerceAtMost(420f * f.size)
        val outerRadius = (50f + growth * 220f) * f.size
        val innerRadius = (12f + growth * 45f) * f.size

        val center = point(f, f.touch, depth)

        // ── 1. LAYER: Deep Water Abyss Funnel Glow ──
        abyssPaint.shader = RadialGradient(
            center.first, center.second, outerRadius * 1.4f,
            intArrayOf(withAlpha(darken(f.color, 0.85f), (245 * f.opacity).toInt()), withAlpha(f.color, (170 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 0.60f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawCircle(center.first, center.second, outerRadius * 1.4f, abyssPaint)

        // ── 2. LAYER: Dynamic Spinning Fluid Vortex Bands (6 Swirling Spiral Arms) ──
        val armCount = 6
        val rotSpeed = timeSec * 6.5
        for (arm in 0 until armCount) {
            val armOffset = arm * (2 * PI / armCount)
            bandPath.reset()
            val steps = 40
            for (i in 0..steps) {
                val t = i / steps.toFloat()
                val r = innerRadius + (outerRadius - innerRadius) * t
                val angle = t * PI * 4.5 + rotSpeed + armOffset

                // 3D Perspective Projection for Spiral Depth
                val zDepth = (1f - t) * 50f
                val proj = Physics3DEngine.project(r, 0f, zDepth)

                val x = center.first + (proj.x * cos(angle)).toFloat()
                val y = center.second + (proj.x * sin(angle)).toFloat()

                if (i == 0) bandPath.moveTo(x, y) else bandPath.lineTo(x, y)
            }

            // 3D Drop Shadow for Spiral Arm
            Physics3DEngine.drawDropShadow(f.canvas, bandPath, dx = 5f, dy = 7f, opacity = f.opacity * 0.45f)

            spiralBandPaint.strokeWidth = ((1f - arm % 2 * 0.4f) * (4.5f * f.size)).coerceAtLeast(1.5f)
            val bandColor = if (arm % 2 == 0) lighten(f.color, 0.7f) else f.color
            spiralBandPaint.color = withAlpha(bandColor, (225 * f.opacity).toInt())
            f.canvas.drawPath(bandPath, spiralBandPaint)
        }

        // ── 3. LAYER: Central Dark Vortex Eye (Sink Hole) ──
        corePaint.color = withAlpha(Color.BLACK, (250 * f.opacity).toInt())
        f.canvas.drawCircle(center.first, center.second, innerRadius, corePaint)
        corePaint.style = Paint.Style.STROKE
        corePaint.strokeWidth = 2.5f * f.size
        corePaint.color = withAlpha(lighten(f.color, 0.9f), (240 * f.opacity).toInt())
        f.canvas.drawCircle(center.first, center.second, innerRadius, corePaint)
        corePaint.style = Paint.Style.FILL

        // ── 4. LAYER: Inward Accelerating Suction Particles (20 Water Particles) ──
        val particleCount = 20
        for (i in 0 until particleCount) {
            val pSeed = i * 29.3f
            val pLife = ((timeSec * 1.6 + pSeed) % 1.0).toFloat()
            // Inward spiral motion equation: r decreases as life increases, speed increases
            val r = outerRadius * (1f - pLife.toDouble().pow(1.5).toFloat())
            val angle = pLife * PI * 6.0 + i

            val px = center.first + (r * cos(angle)).toFloat()
            val py = center.second + (r * sin(angle)).toFloat()
            val pAlpha = ((pLife * 240) * f.opacity).toInt().coerceIn(0, 255)
            val pRadius = (1.5f + (1f - pLife) * 3.5f) * f.size

            particlePaint.color = withAlpha(Color.WHITE, pAlpha)
            f.canvas.drawCircle(px, py, pRadius, particlePaint)
        }

        abyssPaint.shader = null
    }

    private fun point(f: AnimationFrame, along: Float, depth: Float) = when (f.edge) {
        Edge.LEFT -> depth to along
        Edge.RIGHT -> (f.width - depth) to along
        Edge.BOTTOM -> along to (f.height - depth)
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
    private fun lighten(c: Int, t: Float) = Color.rgb((Color.red(c) + (255 - Color.red(c)) * t).toInt().coerceIn(0, 255), (Color.green(c) + (255 - Color.green(c)) * t).toInt().coerceIn(0, 255), (Color.blue(c) + (255 - Color.blue(c)) * t).toInt().coerceIn(0, 255))
    private fun darken(c: Int, t: Float) = Color.rgb((Color.red(c) * (1f - t)).toInt().coerceIn(0, 255), (Color.green(c) * (1f - t)).toInt().coerceIn(0, 255), (Color.blue(c) * (1f - t)).toInt().coerceIn(0, 255))
}
