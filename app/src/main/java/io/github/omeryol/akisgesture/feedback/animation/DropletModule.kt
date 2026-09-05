package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

/**
 * Natural Cohesive Droplet & Liquid Mercury Meniscus.
 * Renders an elastic fluid droplet stretching from the bezel with a natural
 * surface-tension neck, specular spine highlight, and delicate satellite beads.
 */
class DropletModule : NaturalAnimationModule {
    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dropPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val spinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val beadPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val dropPath = Path()
    private val spinePath = Path()
    private val auraPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.3f)
        val depth = (16f + f.stretch * 1.15f).coerceAtMost(360f * f.size)
        val span = (42f + growth * 160f) * f.size

        // ── 1. Ambient Surface-Tension Halo ──
        auraPath.reset()
        val auraDepth = depth * 1.2f
        val auraSpan = span * 1.15f
        val auraSteps = 24
        for (i in 0..auraSteps) {
            val u = i / auraSteps.toFloat()
            val env = sin(PI * u).toFloat().pow(1.6f)
            val p = point(f, f.touch - auraSpan + u * auraSpan * 2f, auraDepth * env)
            if (i == 0) auraPath.moveTo(p.first, p.second) else auraPath.lineTo(p.first, p.second)
        }
        close(auraPath, f, auraSpan)
        auraPaint.shader = gradient(f, auraDepth, alpha(lighten(f.color, 0.4f), (80 * f.opacity).toInt()), Color.TRANSPARENT)
        f.canvas.drawPath(auraPath, auraPaint)

        // ── 2. Cohesive Liquid Droplet Body (Harmonic Meniscus + Bulb Head) ──
        dropPath.reset()
        val steps = 48
        // As growth increases, the neck thins organically and the head rounds out
        val headSharpness = 1.2f + growth * 0.9f
        for (i in 0..steps) {
            val u = i / steps.toFloat() // 0.0 to 1.0 along the span
            val env = sin(PI * u).toFloat().pow(headSharpness)
            // Subtle liquid shimmer along the contour
            val shimmer = sin(u * PI * 4.0 + timeSec * 3.0) * (1.5 + growth * 3.0)
            val d = (depth + shimmer.toFloat()) * env

            val p = point(f, f.touch - span + u * span * 2f, d)
            if (i == 0) dropPath.moveTo(p.first, p.second) else dropPath.lineTo(p.first, p.second)
        }
        close(dropPath, f, span)

        // Droplet Body Gradient: Viscous Luminous Core
        val coreColor = lighten(f.color, 0.35f)
        val edgeColor = darken(f.color, 0.20f)
        dropPaint.shader = gradient(
            f, depth,
            alpha(coreColor, (235 * f.opacity).toInt()),
            alpha(edgeColor, (170 * f.opacity).toInt())
        )
        f.canvas.drawPath(dropPath, dropPaint)

        // ── 3. Specular Liquid Light Spine ──
        // A radiant light streak running down the apex of the droplet
        spinePath.reset()
        val spineSteps = 24
        val spineSpan = span * 0.65f
        for (i in 0..spineSteps) {
            val u = i / spineSteps.toFloat()
            val env = sin(PI * u).toFloat().pow(1.8f)
            val d = (depth * 0.92f) * env
            val p = point(f, f.touch - spineSpan + u * spineSpan * 2f, d)
            if (i == 0) spinePath.moveTo(p.first, p.second) else spinePath.lineTo(p.first, p.second)
        }
        spinePaint.strokeWidth = (2.0f + growth * 1.8f) * f.size
        spinePaint.color = alpha(Color.WHITE, (220 * f.opacity).toInt())
        f.canvas.drawPath(spinePath, spinePaint)

        // ── 4. Floating Cohesive Satellite Beads ──
        if (growth > 0.35f) {
            val beadCount = 3
            for (i in 0 until beadCount) {
                val offsetPhase = i * 0.32f
                val beadDist = depth + (14f + i * 16f) * f.size
                val beadTouch = f.touch + sin(timeSec * 2.5 + offsetPhase).toFloat() * (6f * f.size)
                val beadRadius = (4.0f - i * 0.9f) * f.size * (growth / 1.3f)

                val center = point(f, beadTouch, beadDist)
                beadPaint.shader = RadialGradient(
                    center.first, center.second, beadRadius * 1.5f,
                    intArrayOf(alpha(Color.WHITE, (240 * f.opacity).toInt()), alpha(f.color, (170 * f.opacity).toInt()), Color.TRANSPARENT),
                    floatArrayOf(0f, 0.65f, 1f),
                    Shader.TileMode.CLAMP
                )
                f.canvas.drawCircle(center.first, center.second, beadRadius, beadPaint)
            }
        }

        auraPaint.shader = null
        dropPaint.shader = null
        beadPaint.shader = null
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
