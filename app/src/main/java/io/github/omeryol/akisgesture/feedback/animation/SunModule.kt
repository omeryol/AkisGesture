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
 * Solar Corona & Magnetic Prominence Flare.
 * Renders an incandescent stellar corona expanding from the bezel with magnetic
 * plasma loop arches, golden-amber radiant auras, and streaming solar wind particles.
 */
class SunModule : NaturalAnimationModule {
    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val coronaPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val loopPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val flareParticlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val coronaPath = Path()
    private val loopPath = Path()
    private val auraPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.3f)
        val depth = (18f + f.stretch * 1.20f).coerceAtMost(380f * f.size)
        val span = (46f + f.progress * 200f) * f.size

        // ── 1. Radiant Heliographic Aura Glow ──
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
        auraPaint.shader = gradient(f, auraDepth, alpha(0xFFFF8F00.toInt(), (115 * f.opacity).toInt()), Color.TRANSPARENT)
        f.canvas.drawPath(auraPath, auraPaint)

        // ── 2. Incandescent Solar Corona Body ──
        coronaPath.reset()
        val steps = 40
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat().pow(1.5f)
            val wave1 = sin(u * PI * 3.5 + timeSec * 3.2) * (6.0 + depth * 0.08)
            val wave2 = sin(u * PI * 8.0 - timeSec * 4.5) * (3.0 + depth * 0.04)
            val totalDepth = (depth + (wave1 + wave2).toFloat()) * env

            val p = point(f, f.touch - span + u * span * 2f, totalDepth)
            if (i == 0) coronaPath.moveTo(p.first, p.second) else coronaPath.lineTo(p.first, p.second)
        }
        close(coronaPath, f, span)

        coronaPaint.shader = gradient(
            f, depth,
            alpha(0xFFFFD600.toInt(), (240 * f.opacity).toInt()),
            alpha(0xFFFF6D00.toInt(), (150 * f.opacity).toInt())
        )
        f.canvas.drawPath(coronaPath, coronaPaint)

        // ── 3. Dynamic Magnetic Prominence Loops ──
        val loopCount = 3
        for (l in 0 until loopCount) {
            loopPath.reset()
            val lSpan = span * (0.45f + l * 0.22f)
            val lDepth = depth * (0.85f + l * 0.20f)
            val lSpeed = 1.0f + l * 0.4f
            val lSteps = 24
            for (i in 0..lSteps) {
                val u = i / lSteps.toFloat()
                val env = sin(PI * u).toFloat().pow(1.8f)
                val pulse = sin(u * PI * 2.0 + timeSec * 4.0 * lSpeed + l) * (4.0 * f.size)
                val d = (lDepth + pulse.toFloat()) * env

                val p = point(f, f.touch - lSpan + u * lSpan * 2f, d)
                if (i == 0) loopPath.moveTo(p.first, p.second) else loopPath.lineTo(p.first, p.second)
            }
            loopPaint.strokeWidth = (2.2f + (2 - l) * 0.8f) * f.size
            loopPaint.color = alpha(if (l == 0) Color.WHITE else 0xFFFFF59D.toInt(), ((230 - l * 40) * f.opacity).toInt())
            f.canvas.drawPath(loopPath, loopPaint)
        }

        // ── 4. Streaming Solar Wind Ions ──
        val particleCount = 10
        for (i in 0 until particleCount) {
            val seed = i * 33.7f
            val phase = ((timeSec * 0.8 + seed) % 1.0).toFloat()
            val pDist = depth * (0.40f + phase * 0.85f)
            val along = f.touch - span * 0.6f + (i / (particleCount - 1).toFloat()) * span * 1.2f + sin(timeSec * 3.5 + i).toFloat() * 10f * f.size

            val p = point(f, along, pDist)
            val pSize = (2.0f + (1f - phase) * 2.5f) * f.size
            val pAlpha = ((sin(phase * PI) * 235) * f.opacity).toInt().coerceIn(0, 255)

            flareParticlePaint.color = alpha(if (i % 2 == 0) Color.WHITE else 0xFFFFEA00.toInt(), pAlpha)
            f.canvas.drawCircle(p.first, p.second, pSize, flareParticlePaint)
        }

        auraPaint.shader = null
        coronaPaint.shader = null
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
