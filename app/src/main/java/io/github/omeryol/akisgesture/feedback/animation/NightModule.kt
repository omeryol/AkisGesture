package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Gravitational Photon Lensing & Midnight Cosmic Horizon.
 * Renders an ethereal cosmic event horizon well expanding from the bezel with
 * relativistic Einstein photon rings, gravitational light bending arcs, and deep indigo blooms.
 */
class NightModule : NaturalAnimationModule {
    private val wellPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val photonRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val lensingArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val arcPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.3f)
        val depth = (18f + f.stretch * 1.15f).coerceAtMost(360f * f.size)
        val wellRadius = (30f + growth * 80f) * f.size
        val center = point(f, f.touch, depth * 0.70f)

        // ── 1. Deep Midnight Cosmic Well Bloom ──
        val wellBloomRadius = wellRadius * 1.7f
        val deepIndigo = 0xFF1A237E.toInt()
        val cosmicPurple = 0xFF4A148C.toInt()
        wellPaint.shader = RadialGradient(
            center.first, center.second, wellBloomRadius,
            intArrayOf(alpha(deepIndigo, (210 * f.opacity).toInt()), alpha(cosmicPurple, (110 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawCircle(center.first, center.second, wellBloomRadius, wellPaint)

        // ── 2. Glowing Einstein Photon Ring ──
        val ringRadius = wellRadius * 0.75f
        photonRingPaint.strokeWidth = (2.4f + growth * 1.6f) * f.size
        photonRingPaint.color = alpha(0xFF80D8FF.toInt(), (235 * f.opacity).toInt())
        f.canvas.drawCircle(center.first, center.second, ringRadius, photonRingPaint)

        // ── 3. Relativistic Gravitational Lensing Arcs (3 Bending Arcs) ──
        val arcCount = 3
        for (a in 0 until arcCount) {
            arcPath.reset()
            val aRadius = wellRadius * (0.95f + a * 0.28f)
            val rotAngle = timeSec * 1.5 + a * (2 * PI / arcCount)
            val sweepRad = PI * 0.65

            val steps = 24
            for (i in 0..steps) {
                val t = i / steps.toFloat()
                val ang = rotAngle + t * sweepRad
                val r = aRadius + sin(t * PI * 2.0 + timeSec * 3.0).toFloat() * (3f * f.size)

                val x = center.first + (r * cos(ang)).toFloat()
                val y = center.second + (r * sin(ang)).toFloat()

                if (i == 0) arcPath.moveTo(x, y) else arcPath.lineTo(x, y)
            }

            lensingArcPaint.strokeWidth = (2.0f - a * 0.4f).coerceAtLeast(1f) * f.size
            lensingArcPaint.color = alpha(if (a == 0) Color.WHITE else 0xFFE040FB.toInt(), ((210 - a * 45) * f.opacity).toInt())
            f.canvas.drawPath(arcPath, lensingArcPaint)
        }

        wellPaint.shader = null
    }

    private fun point(f: AnimationFrame, along: Float, depth: Float) = when (f.edge) {
        Edge.LEFT -> depth to along
        Edge.RIGHT -> (f.width - depth) to along
        Edge.BOTTOM -> along to (f.height - depth)
    }
}
