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

/** High-Velocity Kinetic Hydro Shockwave & Supersonic Pressure Rings. */
class PressureWaveModule : NaturalAnimationModule {
    private val shockArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val rayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val arcPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.1f).coerceIn(0f, 1.3f)
        val maxRadius = (40f + growth * 240f) * f.size
        val origin = point(f, f.touch, 0f)

        // ── 1. LAYER: Supersonic Pressure Core Glow ──
        val coreRadius = maxRadius * 0.45f
        corePaint.shader = RadialGradient(
            origin.first, origin.second, coreRadius,
            intArrayOf(withAlpha(lighten(f.color, 0.9f), (240 * f.opacity).toInt()), withAlpha(f.color, (140 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawCircle(origin.first, origin.second, coreRadius, corePaint)

        // ── 2. LAYER: Concentric High-Velocity Kinetic Shock Arcs ──
        val arcCount = 4
        for (i in 0 until arcCount) {
            val arcProgress = ((growth + i * 0.22f) % 1.0f)
            val r = maxRadius * arcProgress
            val alpha = ((1f - arcProgress) * 240 * f.opacity).toInt().coerceIn(0, 255)

            arcPath.reset()
            val startAngle = when (f.edge) {
                Edge.LEFT -> -80f
                Edge.RIGHT -> 100f
                Edge.BOTTOM -> -170f
            }
            val sweepAngle = 160f

            val rectLeft = origin.first - r
            val rectTop = origin.second - r
            val rectRight = origin.first + r
            val rectBottom = origin.second + r

            arcPath.addArc(rectLeft, rectTop, rectRight, rectBottom, startAngle, sweepAngle)

            // 3D Drop Shadow for Shock Arc
            Physics3DEngine.drawDropShadow(f.canvas, arcPath, dx = 6f, dy = 8f, opacity = f.opacity * 0.4f)

            shockArcPaint.strokeWidth = (6.0f * (1f - arcProgress * 0.6f) + 2f) * f.size
            shockArcPaint.color = withAlpha(lighten(f.color, 0.75f), alpha)
            f.canvas.drawPath(arcPath, shockArcPaint)
        }

        // ── 3. LAYER: Radial High-Pressure Energy Rays ──
        val rayCount = 14
        rayPaint.strokeWidth = 2.5f * f.size
        for (i in 0 until rayCount) {
            val u = i / (rayCount - 1).toFloat()
            val angleRad = when (f.edge) {
                Edge.LEFT -> (-PI / 2.2 + u * PI / 1.1)
                Edge.RIGHT -> (PI / 2.2 + u * PI / 1.1)
                Edge.BOTTOM -> (-PI + u * PI)
            }
            val rayLen = maxRadius * (0.6f + sin(u * PI * 5.0 + timeSec * 6.0).toFloat() * 0.4f)
            val dx = cos(angleRad).toFloat() * rayLen
            val dy = sin(angleRad).toFloat() * rayLen

            rayPaint.color = withAlpha(lighten(f.color, 0.6f), (180 * f.opacity).toInt())
            f.canvas.drawLine(origin.first, origin.second, origin.first + dx, origin.second + dy, rayPaint)
        }

        // ── 4. LAYER: Kinetic Discharge Particles ──
        val particleCount = 10
        for (i in 0 until particleCount) {
            val pSeed = i * 43.7f
            val pLife = ((timeSec * 2.2 + pSeed) % 1.0).toFloat()
            val pDist = maxRadius * pLife
            val pAngle = (-PI / 2.2 + (i / particleCount.toFloat()) * PI / 1.1)
            val px = origin.first + cos(pAngle).toFloat() * pDist
            val py = origin.second + sin(pAngle).toFloat() * pDist
            val pAlpha = ((1f - pLife) * 230 * f.opacity).toInt().coerceIn(0, 255)

            particlePaint.color = withAlpha(Color.WHITE, pAlpha)
            f.canvas.drawCircle(px, py, (3.5f * (1f - pLife * 0.5f)) * f.size, particlePaint)
        }

        corePaint.shader = null
    }

    private fun point(f: AnimationFrame, along: Float, depth: Float) = when (f.edge) {
        Edge.LEFT -> depth to along
        Edge.RIGHT -> (f.width - depth) to along
        Edge.BOTTOM -> along to (f.height - depth)
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
    private fun lighten(c: Int, t: Float) = Color.rgb((Color.red(c) + (255 - Color.red(c)) * t).toInt().coerceIn(0, 255), (Color.green(c) + (255 - Color.green(c)) * t).toInt().coerceIn(0, 255), (Color.blue(c) + (255 - Color.blue(c)) * t).toInt().coerceIn(0, 255))
}
