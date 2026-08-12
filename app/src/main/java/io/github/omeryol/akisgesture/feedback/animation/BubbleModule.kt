package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.feedback.Physics3DEngine
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.sin

/** Ultra-Refined Effervescent Water Bubbles with Liquid Base & Specular Thin-Film Highlights. */
class BubbleModule : NaturalAnimationModule {
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bubbleFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bubbleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val glintPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val popRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val wavePath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.25f)
        val depth = (14f + f.stretch * 1.05f).coerceAtMost(280f * f.size)
        val span = (45f + growth * 150f) * f.size

        // ── 1. Delicate Liquid Base Surface Wave ──
        wavePath.reset()
        val steps = 30
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat()
            val ripple = sin(u * PI * 4.0 + timeSec * 3.0) * (3.0 + growth * 5.0)
            val d = (depth * 0.45f + ripple.toFloat()) * env

            val pt = point(f, f.touch - span + u * span * 2f, d)
            if (i == 0) wavePath.moveTo(pt.first, pt.second) else wavePath.lineTo(pt.first, pt.second)
        }
        close(wavePath, f, span)

        wavePaint.shader = gradient(f, depth * 0.45f, withAlpha(lighten(f.color, 0.5f), (180 * f.opacity).toInt()), withAlpha(f.color, (90 * f.opacity).toInt()))
        f.canvas.drawPath(wavePath, wavePaint)

        // ── 2. 10 Effervescent Water Bubbles ──
        val bubbleCount = 10
        for (i in 0 until bubbleCount) {
            val seed = i * 47.19f
            val cycle = 1.1 + (i % 3) * 0.3
            val life = ((timeSec * 1.1 + seed) % cycle) / cycle
            val phase = life.toFloat()

            // Archimedes buoyancy upward drift + lateral sine sway
            val bDepth = depth * (0.35f + phase * 0.95f)
            val sway = sin(phase * PI * 2.5 + seed).toFloat() * (span * 0.35f)
            val along = f.touch - span * 0.3f + (i / bubbleCount.toFloat()) * span * 0.6f + sway

            val center = point(f, along, bDepth)

            // Scaled, delicate bubble size (4.5dp to 16dp)
            val radius = (4.5f + phase * 11.5f) * f.size
            val alpha = ((1f - phase) * 230 * f.opacity).toInt().coerceIn(0, 255)

            if (phase < 0.88f) {
                // 3D Soft Drop Shadow for Bubble
                Physics3DEngine.drawDropShadow(f.canvas, Path().apply { addCircle(center.first, center.second, radius, Path.Direction.CW) }, dx = 3f, dy = 5f, opacity = f.opacity * 0.35f)

                // Translucent Liquid Bubble Shell Gradient
                val brightColor = lighten(f.color, 0.70f)
                bubbleFillPaint.shader = RadialGradient(
                    center.first - radius * 0.32f, center.second - radius * 0.32f, radius * 1.6f,
                    intArrayOf(withAlpha(brightColor, (alpha * 0.65f).toInt()), withAlpha(f.color, (alpha * 0.25f).toInt()), withAlpha(darken(f.color, 0.3f), (alpha * 0.05f).toInt())),
                    floatArrayOf(0f, 0.70f, 1f),
                    Shader.TileMode.CLAMP
                )
                f.canvas.drawCircle(center.first, center.second, radius, bubbleFillPaint)

                // Thin-Film Specular Highlight Edge Stroke
                bubbleStrokePaint.strokeWidth = (1.5f * f.size).coerceAtLeast(1.0f)
                bubbleStrokePaint.color = withAlpha(lighten(f.color, 0.85f), (alpha * 0.85f).toInt())
                f.canvas.drawCircle(center.first, center.second, radius, bubbleStrokePaint)

                // Crescent Specular Glint Spot
                glintPaint.color = withAlpha(Color.WHITE, (alpha * 0.95f).toInt())
                f.canvas.drawCircle(center.first - radius * 0.35f, center.second - radius * 0.35f, (radius * 0.30f).coerceAtLeast(1.5f), glintPaint)
            } else {
                // Pop Effect: Expanding Translucent Ripple Ring
                val popPhase = (phase - 0.88f) / 0.12f
                val popRadius = radius + popPhase * 8f * f.size
                val popAlpha = ((1f - popPhase) * 200 * f.opacity).toInt().coerceIn(0, 255)

                popRingPaint.strokeWidth = (1.8f * (1f - popPhase)).coerceAtLeast(0.8f)
                popRingPaint.color = withAlpha(Color.WHITE, popAlpha)
                f.canvas.drawCircle(center.first, center.second, popRadius, popRingPaint)
            }
        }

        wavePaint.shader = null
        bubbleFillPaint.shader = null
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

    private fun gradient(f: AnimationFrame, depth: Float, a: Int, b: Int) = when (f.edge) {
        Edge.LEFT -> LinearGradient(0f, f.touch, depth, f.touch, a, b, Shader.TileMode.CLAMP)
        Edge.RIGHT -> LinearGradient(f.width, f.touch, f.width - depth, f.touch, a, b, Shader.TileMode.CLAMP)
        Edge.BOTTOM -> LinearGradient(f.touch, f.height, f.touch, f.height - depth, a, b, Shader.TileMode.CLAMP)
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
    private fun lighten(c: Int, t: Float) = Color.rgb((Color.red(c) + (255 - Color.red(c)) * t).toInt().coerceIn(0, 255), (Color.green(c) + (255 - Color.green(c)) * t).toInt().coerceIn(0, 255), (Color.blue(c) + (255 - Color.blue(c)) * t).toInt().coerceIn(0, 255))
    private fun darken(c: Int, t: Float) = Color.rgb((Color.red(c) * (1f - t)).toInt().coerceIn(0, 255), (Color.green(c) * (1f - t)).toInt().coerceIn(0, 255), (Color.blue(c) * (1f - t)).toInt().coerceIn(0, 255))
}
