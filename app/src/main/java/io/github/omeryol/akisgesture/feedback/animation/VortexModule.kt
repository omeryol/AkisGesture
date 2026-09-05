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
 * Oceanic Maelstrom Vortex & Logarithmic Fluid Streamlines.
 * Renders an organic aquatic whirlpool expanding from the bezel with continuous
 * logarithmic spiral current arms, deep suction eye gradients, and luminous foam trails.
 */
class VortexModule : NaturalAnimationModule {
    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val spiralArmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val foamPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val armPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.3f)
        val depth = (18f + f.stretch * 1.15f).coerceAtMost(360f * f.size)
        val eyeRadius = (32f + growth * 75f) * f.size
        val center = point(f, f.touch, depth * 0.75f)

        // ── 1. Ambient Whirlpool Suction Halo ──
        val auraRadius = eyeRadius * 1.6f
        auraPaint.shader = RadialGradient(
            center.first, center.second, auraRadius,
            intArrayOf(alpha(f.color, (130 * f.opacity).toInt()), alpha(darken(f.color, 0.4f), (60 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 0.65f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawCircle(center.first, center.second, auraRadius, auraPaint)

        // ── 2. Logarithmic Spiral Fluid Current Arms (4 Curving Streamlines) ──
        val armCount = 4
        val rotSpeed = timeSec * 3.8
        for (arm in 0 until armCount) {
            val armOffset = arm * (2 * PI / armCount)
            armPath.reset()
            val steps = 36
            for (i in 0..steps) {
                val t = i / steps.toFloat() // 0 (eye) to 1 (outer rim)
                // Logarithmic expansion curve
                val r = (6f + (eyeRadius - 6f) * t * t) * f.size
                val angle = t * PI * 2.8 + rotSpeed + armOffset

                val x = center.first + (r * cos(angle)).toFloat()
                val y = center.second + (r * sin(angle)).toFloat()

                if (i == 0) armPath.moveTo(x, y) else armPath.lineTo(x, y)
            }

            spiralArmPaint.strokeWidth = (2.6f * (1f - arm * 0.15f) + growth * 1.2f) * f.size
            val armColor = blend(f.color, 0xFF80D8FF.toInt(), 0.5f)
            spiralArmPaint.color = alpha(armColor, (185 * f.opacity).toInt())
            f.canvas.drawPath(armPath, spiralArmPaint)
        }

        // ── 3. Soft Depth Suction Eye Core ──
        eyePaint.shader = RadialGradient(
            center.first, center.second, eyeRadius * 0.35f,
            intArrayOf(alpha(darken(f.color, 0.6f), (220 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawCircle(center.first, center.second, eyeRadius * 0.35f, eyePaint)

        // ── 4. Swirling Water Foam Specks ──
        val foamCount = 10
        for (i in 0 until foamCount) {
            val seed = i * 39.7f
            val phase = ((timeSec * 0.9 + seed) % 1.0).toFloat()
            val r = eyeRadius * (1f - phase * 0.82f)
            val angle = phase * PI * 4.5 + rotSpeed + i * (2 * PI / foamCount)

            val px = center.first + (r * cos(angle)).toFloat()
            val py = center.second + (r * sin(angle)).toFloat()
            val pAlpha = ((sin(phase * PI) * 220) * f.opacity).toInt().coerceIn(0, 255)

            foamPaint.color = alpha(Color.WHITE, pAlpha)
            f.canvas.drawCircle(px, py, (1.8f + (1f - phase) * 1.8f) * f.size, foamPaint)
        }

        auraPaint.shader = null
        eyePaint.shader = null
    }

    private fun point(f: AnimationFrame, along: Float, depth: Float) = when (f.edge) {
        Edge.LEFT -> depth to along
        Edge.RIGHT -> (f.width - depth) to along
        Edge.BOTTOM -> along to (f.height - depth)
    }
}
