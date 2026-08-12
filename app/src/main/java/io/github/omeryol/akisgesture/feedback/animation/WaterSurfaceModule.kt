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
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.cos

class WaterSurfaceModule : NaturalAnimationModule {
    private val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val crestPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val dropPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mainPath = Path()
    private val crestPath = Path()
    private val auraPath = Path()

    override fun draw(f: AnimationFrame) {
        val growth = (f.progress / 1.25f).coerceIn(0f, 1f).pow(2.0f)
        val span = (6f + growth * (280f + f.surfaceTension * 90f)) * f.size
        val depth = (f.stretch * (1.25f + f.surfaceTension * .35f)).coerceAtMost(420f * f.size)
        val timeSec = f.time

        // ── 1. LAYER: 3D Ambient Drop Shadow ──
        mainPath.reset()
        crestPath.reset()
        val steps = 50
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat().pow(1.8f)
            
            val ripple1 = sin(u * PI * (3.2 + f.surfaceTension * 1.2) + timeSec * (2.2 + f.damping * 1.5)) * (4.0 + growth * (14.0 + f.surfaceTension * 6.0))
            val ripple2 = sin(u * PI * 8.0 - timeSec * (1.8 + f.viscosity * 2.0)) * growth * (5.0 + f.viscosity * 5.0)
            val totalDepth = (depth + (ripple1 + ripple2).toFloat()) * env

            // 3D Perspective Projection Calculation
            val zDepth = sin(u * PI).toFloat() * 35f * growth
            val proj = Physics3DEngine.project(totalDepth, 0f, zDepth)

            val p = point(f, f.touch - span + u * span * 2f, proj.x)
            if (i == 0) {
                mainPath.moveTo(p.first, p.second)
                crestPath.moveTo(p.first, p.second)
            } else {
                mainPath.lineTo(p.first, p.second)
                crestPath.lineTo(p.first, p.second)
            }
        }
        close(mainPath, f, span)

        // Draw 3D Drop Shadow on screen background
        Physics3DEngine.drawDropShadow(f.canvas, mainPath, dx = 8f, dy = 12f, opacity = f.opacity * 0.5f)

        // ── 2. LAYER: Background Ambient Glow Aura ──
        auraPath.reset()
        val auraDepth = depth * 1.40f
        val auraSpan = span * 1.30f
        for (i in 0..30) {
            val u = i / 30f
            val env = sin(PI * u).toFloat().pow(1.5f)
            val p = point(f, f.touch - auraSpan + u * auraSpan * 2f, auraDepth * env)
            if (i == 0) auraPath.moveTo(p.first, p.second) else auraPath.lineTo(p.first, p.second)
        }
        close(auraPath, f, auraSpan)
        auraPaint.shader = auraGradient(f, auraDepth, withAlpha(f.color, (100 * f.opacity).toInt()))
        f.canvas.drawPath(auraPath, auraPaint)

        // ── 3. LAYER: Main Liquid Water Body with 3D Lighting ──
        val specular = Physics3DEngine.computeSpecularLight(0.2f, -0.6f, 0.77f, shininess = 20f)
        val brightColor = lighten(f.color, 0.35f + specular * 0.40f)
        val deepColor = darken(f.color, 0.30f)

        mainPaint.shader = gradient(
            f, depth,
            withAlpha(brightColor, (245 * f.opacity).toInt()),
            withAlpha(deepColor, (195 * f.opacity).toInt())
        )
        f.canvas.drawPath(mainPath, mainPaint)

        // ── 4. LAYER: 3D Specular Highlight Crest ──
        val crestColor = lighten(f.color, 0.88f)
        crestPaint.strokeWidth = (3.0f + growth * 3.0f + specular * 2f) * f.size
        crestPaint.color = withAlpha(crestColor, (240 * f.opacity).toInt())
        f.canvas.drawPath(crestPath, crestPaint)

        // ── 5. LAYER: Gravity-bound Teardrop Budding ──
        if (growth > 0.45f) {
            val dropStretch = (depth * 1.15f)
            val dropRadius = (8.0f + growth * 12.0f) * f.size
            val dropCenter = point(f, f.touch, dropStretch)

            dropPaint.shader = RadialGradient(
                dropCenter.first - dropRadius * 0.35f, dropCenter.second - dropRadius * 0.35f, dropRadius * 1.6f,
                intArrayOf(withAlpha(lighten(f.color, 0.85f), (255 * f.opacity).toInt()), withAlpha(f.color, (190 * f.opacity).toInt()), Color.TRANSPARENT),
                floatArrayOf(0f, 0.60f, 1f),
                Shader.TileMode.CLAMP
            )
            f.canvas.drawCircle(dropCenter.first, dropCenter.second, dropRadius, dropPaint)
            dropPaint.shader = null
        }

        mainPaint.shader = null
        auraPaint.shader = null
    }

    private fun point(f: AnimationFrame, along: Float, depth: Float) = when (f.edge) {
        Edge.LEFT -> depth to along
        Edge.RIGHT -> f.width - depth to along
        Edge.BOTTOM -> along to f.height - depth
    }

    private fun close(p: Path, f: AnimationFrame, span: Float) {
        when (f.edge) {
            Edge.LEFT -> {
                p.lineTo(0f, f.touch + span)
                p.lineTo(0f, f.touch - span)
            }
            Edge.RIGHT -> {
                p.lineTo(f.width, f.touch + span)
                p.lineTo(f.width, f.touch - span)
            }
            Edge.BOTTOM -> {
                p.lineTo(f.touch + span, f.height)
                p.lineTo(f.touch - span, f.height)
            }
        }
        p.close()
    }

    private fun gradient(f: AnimationFrame, depth: Float, a: Int, b: Int) = when (f.edge) {
        Edge.LEFT -> LinearGradient(0f, f.touch, depth, f.touch, a, b, Shader.TileMode.CLAMP)
        Edge.RIGHT -> LinearGradient(f.width, f.touch, f.width - depth, f.touch, a, b, Shader.TileMode.CLAMP)
        Edge.BOTTOM -> LinearGradient(f.touch, f.height, f.touch, f.height - depth, a, b, Shader.TileMode.CLAMP)
    }

    private fun auraGradient(f: AnimationFrame, depth: Float, color: Int) = when (f.edge) {
        Edge.LEFT -> RadialGradient(0f, f.touch, depth, color, Color.TRANSPARENT, Shader.TileMode.CLAMP)
        Edge.RIGHT -> RadialGradient(f.width, f.touch, depth, color, Color.TRANSPARENT, Shader.TileMode.CLAMP)
        Edge.BOTTOM -> RadialGradient(f.touch, f.height, depth, color, Color.TRANSPARENT, Shader.TileMode.CLAMP)
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))

    private fun lighten(color: Int, fraction: Float): Int {
        val r = (Color.red(color) + (255 - Color.red(color)) * fraction).toInt().coerceIn(0, 255)
        val g = (Color.green(color) + (255 - Color.green(color)) * fraction).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) + (255 - Color.blue(color)) * fraction).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }

    private fun darken(color: Int, fraction: Float): Int {
        val r = (Color.red(color) * (1f - fraction)).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * (1f - fraction)).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * (1f - fraction)).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }
}
