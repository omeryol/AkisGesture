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
import kotlin.math.sin

/** Subtle, elegant, compact 3D Water Whirlpool Vortex with soft translucent fluid currents. */
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
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.2f)
        val depth = (18f + f.stretch * 1.10f).coerceAtMost(280f * f.size)
        // Compact, elegant outer and inner radii (non-distracting, refined scale)
        val outerRadius = (30f + growth * 85f) * f.size
        val innerRadius = (6f + growth * 18f) * f.size

        val center = point(f, f.touch, depth)

        // ── 1. LAYER: Soft Translucent Ambient Fluid Aura ──
        abyssPaint.shader = RadialGradient(
            center.first, center.second, outerRadius * 1.3f,
            intArrayOf(withAlpha(f.color, (140 * f.opacity).toInt()), withAlpha(darken(f.color, 0.4f), (60 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 0.65f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawCircle(center.first, center.second, outerRadius * 1.3f, abyssPaint)

        // ── 2. LAYER: Subtle Translucent Fluid Spiral Arms (4 Soft Arms) ──
        val armCount = 4
        val rotSpeed = timeSec * 4.5
        for (arm in 0 until armCount) {
            val armOffset = arm * (2 * PI / armCount)
            bandPath.reset()
            val steps = 30
            for (i in 0..steps) {
                val t = i / steps.toFloat()
                val r = innerRadius + (outerRadius - innerRadius) * t
                val angle = t * PI * 3.5 + rotSpeed + armOffset

                val zDepth = (1f - t) * 35f
                val proj = Physics3DEngine.project(r, 0f, zDepth)

                val x = center.first + (proj.x * cos(angle)).toFloat()
                val y = center.second + (proj.x * sin(angle)).toFloat()

                if (i == 0) bandPath.moveTo(x, y) else bandPath.lineTo(x, y)
            }

            // Soft Drop Shadow
            Physics3DEngine.drawDropShadow(f.canvas, bandPath, dx = 3f, dy = 5f, opacity = f.opacity * 0.30f)

            spiralBandPaint.strokeWidth = 2.2f * f.size
            spiralBandPaint.color = withAlpha(lighten(f.color, 0.6f), (150 * f.opacity).toInt())
            f.canvas.drawPath(bandPath, spiralBandPaint)
        }

        // ── 3. LAYER: Subtle Soft Core Eye ──
        corePaint.shader = RadialGradient(
            center.first, center.second, innerRadius * 1.2f,
            intArrayOf(withAlpha(darken(f.color, 0.6f), (180 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawCircle(center.first, center.second, innerRadius * 1.2f, corePaint)

        // ── 4. LAYER: Soft Floating Accretion Particles (8 Particles) ──
        val particleCount = 8
        for (i in 0 until particleCount) {
            val pSeed = i * 41.3f
            val pLife = ((timeSec * 1.2 + pSeed) % 1.0).toFloat()
            val r = outerRadius * (1f - pLife * 0.85f)
            val angle = pLife * PI * 4.0 + i

            val px = center.first + (r * cos(angle)).toFloat()
            val py = center.second + (r * sin(angle)).toFloat()
            val pAlpha = ((pLife * 160) * f.opacity).toInt().coerceIn(0, 255)

            particlePaint.color = withAlpha(Color.WHITE, pAlpha)
            f.canvas.drawCircle(px, py, 2.0f * f.size, particlePaint)
        }

        abyssPaint.shader = null
        corePaint.shader = null
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
