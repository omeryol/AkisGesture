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
 * Suminagashi Ink in Water & Watercolor Dispersion.
 * Renders an organic Japanese marbling ink plume blooming from the bezel with
 * curling diffusion tendrils, watercolor feathering bleed gradients, and floating pigment wisps.
 */
class InkModule : NaturalAnimationModule {
    private val mainInkPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tendrilPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wispPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val mainInkPath = Path()
    private val tendrilPath = Path()
    private val wispPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.3f)
        val depth = (18f + f.stretch * 1.18f).coerceAtMost(380f * f.size)
        val span = (46f + f.progress * 210f) * f.size

        // ── 1. Main Watercolor Diffusion Plume ──
        mainInkPath.reset()
        val steps = 44
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat().pow(1.5f)
            // Multi-harmonic curling fluid dispersion
            val plume1 = sin(u * PI * 4.0 + timeSec * 2.2) * (7.0 + depth * 0.08)
            val plume2 = sin(u * PI * 9.0 - timeSec * 1.5) * (3.5 + depth * 0.04)
            val totalDepth = (depth + (plume1 + plume2).toFloat()) * env

            val p = point(f, f.touch - span + u * span * 2f, totalDepth)
            if (i == 0) mainInkPath.moveTo(p.first, p.second) else mainInkPath.lineTo(p.first, p.second)
        }
        close(mainInkPath, f, span)

        // Watercolor pigment: deep saturated ink core bleeding into translucent wash
        val deepPigment = darken(f.color, 0.40f)
        val washPigment = lighten(f.color, 0.20f)
        mainInkPaint.shader = gradient(
            f, depth,
            alpha(deepPigment, (230 * f.opacity).toInt()),
            alpha(washPigment, (100 * f.opacity).toInt())
        )
        f.canvas.drawPath(mainInkPath, mainInkPaint)

        // ── 2. Organic Curling Ink Tendrils ──
        val tendrilCount = 6
        for (t in 0 until tendrilCount) {
            tendrilPath.reset()
            val tOffset = (t - 2.5f) * (span * 0.32f)
            val tDepth = depth * (0.60f + sin(timeSec * 2.0 + t).toFloat() * 0.35f)
            val tSteps = 24

            for (i in 0..tSteps) {
                val u = i / tSteps.toFloat()
                val env = sin(PI * u).toFloat()
                val curl = sin(u * PI * 3.0 + timeSec * 2.8 + t) * (10.0 * u * f.size)
                val d = tDepth * u
                val along = f.touch + tOffset + curl.toFloat()

                val p = point(f, along, d)
                if (i == 0) tendrilPath.moveTo(p.first, p.second) else tendrilPath.lineTo(p.first, p.second)
            }
            close(tendrilPath, f, span * 0.25f)

            tendrilPaint.shader = gradient(
                f, tDepth,
                alpha(darken(f.color, 0.30f), (180 * f.opacity).toInt()),
                Color.TRANSPARENT
            )
            f.canvas.drawPath(tendrilPath, tendrilPaint)
        }

        // ── 3. Delicate Floating Ink Filament Wisps ──
        val wispCount = 5
        for (w in 0 until wispCount) {
            wispPath.reset()
            val wAlong = f.touch - span * 0.5f + (w / (wispCount - 1).toFloat()) * span
            val wDepthStart = depth * (0.45f + sin(timeSec * 1.5 + w).toFloat() * 0.15f)
            val wLen = (18f + w * 5f) * f.size

            val p1 = point(f, wAlong, wDepthStart)
            val p2 = point(f, wAlong + sin(timeSec * 3.0 + w).toFloat() * 8f * f.size, wDepthStart + wLen)

            wispPath.moveTo(p1.first, p1.second)
            wispPath.quadTo(
                (p1.first + p2.first) * 0.5f + 4f * f.size,
                (p1.second + p2.second) * 0.5f + 4f * f.size,
                p2.first, p2.second
            )

            wispPaint.strokeWidth = 1.8f * f.size
            wispPaint.color = alpha(deepPigment, (170 * f.opacity).toInt())
            f.canvas.drawPath(wispPath, wispPaint)
        }

        mainInkPaint.shader = null
        tendrilPaint.shader = null
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
