package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

/**
 * Natural Fluid Meniscus & Water Surface Wave.
 * Renders an organic, crystal-clear liquid body stretching from the bezel with
 * multi-harmonic surface ripples, glowing caustic crests, and buoyant spray droplets.
 */
class WaterSurfaceModule : NaturalAnimationModule {
    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val waterBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val crestPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val sprayPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val waterPath = Path()
    private val crestPath = Path()
    private val auraPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.3f)
        val depth = (18f + f.stretch * 1.18f).coerceAtMost(380f * f.size)
        val span = (48f + f.progress * 210f) * f.size

        // ── 1. Ambient Aquatic Diffusion Glow ──
        auraPath.reset()
        val auraDepth = depth * 1.25f
        val auraSpan = span * 1.20f
        val auraSteps = 24
        for (i in 0..auraSteps) {
            val u = i / auraSteps.toFloat()
            val env = sin(PI * u).toFloat().pow(1.4f)
            val p = point(f, f.touch - auraSpan + u * auraSpan * 2f, auraDepth * env)
            if (i == 0) auraPath.moveTo(p.first, p.second) else auraPath.lineTo(p.first, p.second)
        }
        close(auraPath, f, auraSpan)
        val auraColor = blend(f.color, 0xFF00E5FF.toInt(), 0.35f)
        auraPaint.shader = gradient(f, auraDepth, alpha(auraColor, (90 * f.opacity).toInt()), Color.TRANSPARENT)
        f.canvas.drawPath(auraPath, auraPaint)

        // ── 2. Main Liquid Body with Multi-Harmonic Wave Motion ──
        waterPath.reset()
        crestPath.reset()
        val steps = 48
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat().pow(1.5f)
            val wave1 = sin(u * PI * 3.2 + timeSec * 2.6) * (6.0 + growth * 10.0)
            val wave2 = sin(u * PI * 7.0 - timeSec * 3.4) * (2.5 + growth * 4.5)
            val totalDepth = (depth + (wave1 + wave2).toFloat()) * env

            val p = point(f, f.touch - span + u * span * 2f, totalDepth)
            if (i == 0) {
                waterPath.moveTo(p.first, p.second)
                crestPath.moveTo(p.first, p.second)
            } else {
                waterPath.lineTo(p.first, p.second)
                crestPath.lineTo(p.first, p.second)
            }
        }
        close(waterPath, f, span)

        // Liquid Body Shader: Translucent Azure / Turquoise to Base
        val brightAqua = lighten(f.color, 0.45f)
        val deepAqua = darken(f.color, 0.25f)
        waterBodyPaint.shader = gradient(
            f, depth,
            alpha(brightAqua, (220 * f.opacity).toInt()),
            alpha(deepAqua, (140 * f.opacity).toInt())
        )
        f.canvas.drawPath(waterPath, waterBodyPaint)

        // ── 3. Specular Caustic Crest Highlight ──
        crestPaint.strokeWidth = (2.2f + growth * 2.0f) * f.size
        crestPaint.color = alpha(Color.WHITE, (215 * f.opacity).toInt())
        f.canvas.drawPath(crestPath, crestPaint)

        // ── 4. Natural Spray Droplets Floating Off the Wave Crest ──
        val dropCount = 7
        for (i in 0 until dropCount) {
            val seed = i * 29.3f
            val phase = ((timeSec * 0.75 + seed) % 1.0).toFloat()
            val dropDist = depth * (0.65f + phase * 0.55f)
            val along = f.touch - span * 0.6f + (i / (dropCount - 1).toFloat()) * span * 1.2f + sin(timeSec * 3.0 + i).toFloat() * 12f * f.size

            val p = point(f, along, dropDist)
            val dropRadius = (2.2f + (1f - phase) * 2.8f) * f.size
            val dropAlpha = ((sin(phase * PI) * 200) * f.opacity).toInt().coerceIn(0, 255)

            sprayPaint.color = alpha(lighten(brightAqua, 0.6f), dropAlpha)
            f.canvas.drawCircle(p.first, p.second, dropRadius, sprayPaint)
        }

        auraPaint.shader = null
        waterBodyPaint.shader = null
    }

    private fun point(f: AnimationFrame, along: Float, depth: Float) = when (f.edge) {
        Edge.LEFT -> depth to along
        Edge.RIGHT -> (f.width - depth) to along
        Edge.BOTTOM -> along to (f.height - depth)
    }

    private fun close(p: Path, f: AnimationFrame, span: Float) {
        when (f.edge) {
            Edge.LEFT -> { p.lineTo(0f, f.touch + span); p.lineTo(0f, f.touch - span) }
            Edge.RIGHT -> { p.lineTo(f.width, f.touch + span); p.lineTo(f.width, f.touch - span) }
            Edge.BOTTOM -> { p.lineTo(f.touch + span, f.height); p.lineTo(f.touch - span, f.height) }
        }
        p.close()
    }

    private fun gradient(f: AnimationFrame, depth: Float, startColor: Int, endColor: Int) = when (f.edge) {
        Edge.LEFT -> LinearGradient(0f, f.touch, depth, f.touch, startColor, endColor, Shader.TileMode.CLAMP)
        Edge.RIGHT -> LinearGradient(f.width, f.touch, f.width - depth, f.touch, startColor, endColor, Shader.TileMode.CLAMP)
        Edge.BOTTOM -> LinearGradient(f.touch, f.height, f.touch, f.height - depth, startColor, endColor, Shader.TileMode.CLAMP)
    }
}
