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
 * Living Thermal Plasma & Natural Convection Flame.
 * Renders an organic, multi-layered ethereal flame tongue stretching from the bezel with
 * turbulent thermal convection waves, incandescent white-gold core, and drafting embers.
 */
class FireModule : NaturalAnimationModule {
    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outerFlamePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val midFlamePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val coreFlamePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val emberPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val outerPath = Path()
    private val midPath = Path()
    private val corePath = Path()
    private val auraPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.3f)
        val depth = (20f + f.stretch * 1.20f).coerceAtMost(380f * f.size)
        val span = (45f + f.progress * 190f) * f.size

        // ── 1. Radiant Thermal Glow Aura ──
        auraPath.reset()
        val auraDepth = depth * 1.35f
        val auraSpan = span * 1.25f
        val auraSteps = 24
        for (i in 0..auraSteps) {
            val u = i / auraSteps.toFloat()
            val env = sin(PI * u).toFloat().pow(1.3f)
            val p = point(f, f.touch - auraSpan + u * auraSpan * 2f, auraDepth * env)
            if (i == 0) auraPath.moveTo(p.first, p.second) else auraPath.lineTo(p.first, p.second)
        }
        close(auraPath, f, auraSpan)
        auraPaint.shader = gradient(f, auraDepth, alpha(0xFFFF3D00.toInt(), (110 * f.opacity).toInt()), Color.TRANSPARENT)
        f.canvas.drawPath(auraPath, auraPaint)

        // ── 2. Outer Crimson / Amber Flame Envelope ──
        buildFlameContour(outerPath, f, depth, span, timeSec, speed = 1.0f, phase = 0.0f)
        outerFlamePaint.shader = gradient(
            f, depth,
            alpha(0xFFFF6D00.toInt(), (235 * f.opacity).toInt()),
            alpha(0xFFD50000.toInt(), (140 * f.opacity).toInt())
        )
        f.canvas.drawPath(outerPath, outerFlamePaint)

        // ── 3. Mid Golden Thermal Lobe ──
        buildFlameContour(midPath, f, depth * 0.78f, span * 0.80f, timeSec, speed = 1.35f, phase = 1.2f)
        midFlamePaint.shader = gradient(
            f, depth * 0.78f,
            alpha(0xFFFFD600.toInt(), (245 * f.opacity).toInt()),
            alpha(0xFFFF6D00.toInt(), (180 * f.opacity).toInt())
        )
        f.canvas.drawPath(midPath, midFlamePaint)

        // ── 4. Inner Incandescent White-Gold Core ──
        buildFlameContour(corePath, f, depth * 0.48f, span * 0.52f, timeSec, speed = 1.8f, phase = 2.4f)
        coreFlamePaint.shader = gradient(
            f, depth * 0.48f,
            alpha(Color.WHITE, (255 * f.opacity).toInt()),
            alpha(0xFFFFF176.toInt(), (200 * f.opacity).toInt())
        )
        f.canvas.drawPath(corePath, coreFlamePaint)

        // ── 5. Rising Thermal Draft Embers ──
        val emberCount = 12
        for (i in 0 until emberCount) {
            val seed = i * 37.1f
            val phase = ((timeSec * 0.9 + seed) % 1.0).toFloat()
            val drift = (phase - 0.5f) * span * 0.85f
            val eDepth = depth * (0.35f + phase * 0.90f)
            val eTouch = f.touch + drift + sin(timeSec * 4.0 + i).toFloat() * 10f * f.size

            val p = point(f, eTouch, eDepth)
            val emberSize = (1.8f + (1f - phase) * 2.5f) * f.size
            val emberAlpha = ((sin(phase * PI) * 240) * f.opacity).toInt().coerceIn(0, 255)

            emberPaint.color = alpha(if (i % 3 == 0) Color.WHITE else 0xFFFFAB00.toInt(), emberAlpha)
            f.canvas.drawCircle(p.first, p.second, emberSize, emberPaint)
        }

        auraPaint.shader = null
        outerFlamePaint.shader = null
        midFlamePaint.shader = null
        coreFlamePaint.shader = null
    }

    private fun buildFlameContour(path: Path, f: AnimationFrame, depth: Float, span: Float, timeSec: Double, speed: Float, phase: Float) {
        path.reset()
        val steps = 40
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat().pow(1.6f)
            // Multi-frequency harmonic convection turbulence
            val turb1 = sin(u * PI * 4.0 + timeSec * 5.0 * speed + phase) * (7.0 + depth * 0.10)
            val turb2 = sin(u * PI * 9.0 - timeSec * 6.5 * speed) * (3.5 + depth * 0.04)
            val totalDepth = (depth + (turb1 + turb2).toFloat()) * env

            val p = point(f, f.touch - span + u * span * 2f, totalDepth)
            if (i == 0) path.moveTo(p.first, p.second) else path.lineTo(p.first, p.second)
        }
        close(path, f, span)
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
