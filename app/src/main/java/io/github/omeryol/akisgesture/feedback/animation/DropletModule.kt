package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.pow

class DropletModule : NaturalAnimationModule {
    private val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val splashPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val path = Path()
    private val highlightPath = Path()

    override fun draw(f: AnimationFrame) {
        val growth = (f.progress / 1.2f).coerceIn(0f, 1f).pow(1.8f)
        val depth = (12f + f.stretch * (1.20f + f.surfaceTension * 0.25f)).coerceAtMost(380f * f.size)
        val r = (14f + growth * (64f + f.surfaceTension * 22f)) * f.size
        val neck = (r * (1.28f - growth * (0.65f + f.surfaceTension * 0.12f))).coerceAtLeast(6f * f.size)
        val wobble = sin(f.time * PI * (1.8 + f.damping * 1.5)).toFloat() * r * (0.045f + f.viscosity * 0.04f)

        val center = when (f.edge) {
            Edge.LEFT -> depth to f.touch
            Edge.RIGHT -> (f.width - depth) to f.touch
            Edge.BOTTOM -> f.touch to (f.height - depth)
        }

        // ── 1. LAYER: Ambient Drop Glow ──
        auraPaint.shader = RadialGradient(
            center.first, center.second, r * 2.2f,
            intArrayOf(withAlpha(lighten(f.color, 0.4f), (110 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawCircle(center.first, center.second, r * 2.2f, auraPaint)

        // ── 2. LAYER: Main Teardrop Body with Surface Tension Neck ──
        path.reset()
        val detached = f.progress >= 1.02f
        when (f.edge) {
            Edge.LEFT -> if (detached) path.addCircle(center.first, center.second, r, Path.Direction.CW) else {
                path.moveTo(0f, f.touch - neck)
                path.cubicTo(depth * 0.38f, f.touch - neck, center.first - r * 0.9f, center.second - r + wobble, center.first, center.second - r)
                path.cubicTo(center.first + r, center.second - r, center.first + r, center.second + r, center.first, center.second + r)
                path.cubicTo(center.first - r * 0.9f, center.second + r, depth * 0.38f, f.touch + neck, 0f, f.touch + neck)
            }
            Edge.RIGHT -> if (detached) path.addCircle(center.first, center.second, r, Path.Direction.CW) else {
                path.moveTo(f.width, f.touch - neck)
                path.cubicTo(f.width - depth * 0.38f, f.touch - neck, center.first + r * 0.9f, center.second - r + wobble, center.first, center.second - r)
                path.cubicTo(center.first - r, center.second - r, center.first - r, center.second + r, center.first, center.second + r)
                path.cubicTo(center.first + r * 0.9f, center.second + r, f.width - depth * 0.38f, f.touch + neck, f.width, f.touch + neck)
            }
            Edge.BOTTOM -> if (detached) path.addCircle(center.first, center.second, r, Path.Direction.CW) else {
                path.moveTo(f.touch - neck, f.height)
                path.cubicTo(f.touch - neck, f.height - depth * 0.38f, center.first - r, center.second + r * 0.9f, center.first - r, center.second)
                path.cubicTo(center.first - r, center.second - r, center.first + r, center.second - r, center.first + r, center.second)
                path.cubicTo(center.first + r, center.second + r * 0.9f, f.touch + neck, f.height - depth * 0.38f, f.touch + neck, f.height)
            }
        }
        path.close()

        mainPaint.shader = RadialGradient(
            center.first - r * 0.32f, center.second - r * 0.38f, r * 2.4f,
            intArrayOf(withAlpha(lighten(f.color, 0.75f), (250 * f.opacity).toInt()), withAlpha(f.color, (230 * f.opacity).toInt()), withAlpha(darken(f.color, 0.5f), (160 * f.opacity).toInt())),
            floatArrayOf(0f, 0.42f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawPath(path, mainPaint)

        // ── 3. LAYER: Specular Refraction Gloss Spot ──
        highlightPaint.color = withAlpha(Color.WHITE, (220 * f.opacity).toInt())
        val glintX = center.first - r * 0.35f
        val glintY = center.second - r * 0.38f
        f.canvas.drawCircle(glintX, glintY, (r * 0.28f).coerceAtLeast(3f), highlightPaint)

        // ── 4. LAYER: Secondary Micro-Splash Drops ──
        if (growth > 0.4f) {
            val sRadius = (r * 0.18f).coerceAtLeast(2.5f)
            val sDist = depth * 1.3f
            val sPt = when (f.edge) {
                Edge.LEFT -> sDist to (center.second + r * 1.2f)
                Edge.RIGHT -> (f.width - sDist) to (center.second + r * 1.2f)
                Edge.BOTTOM -> (center.first + r * 1.2f) to (f.height - sDist)
            }
            splashPaint.color = withAlpha(lighten(f.color, 0.6f), (200 * f.opacity).toInt())
            f.canvas.drawCircle(sPt.first, sPt.second, sRadius, splashPaint)
        }

        mainPaint.shader = null
        auraPaint.shader = null
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
    private fun lighten(c: Int, t: Float) = Color.rgb((Color.red(c) + (255 - Color.red(c)) * t).toInt().coerceIn(0, 255), (Color.green(c) + (255 - Color.green(c)) * t).toInt().coerceIn(0, 255), (Color.blue(c) + (255 - Color.blue(c)) * t).toInt().coerceIn(0, 255))
    private fun darken(c: Int, t: Float) = Color.rgb((Color.red(c) * (1f - t)).toInt().coerceIn(0, 255), (Color.green(c) * (1f - t)).toInt().coerceIn(0, 255), (Color.blue(c) * (1f - t)).toInt().coerceIn(0, 255))
}
