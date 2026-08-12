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

/** Water Surface Wave with a Liquid Droplet Breaking Away via Surface Tension Physics. */
class DropletModule : NaturalAnimationModule {
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dropPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val crestPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val glintPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val wavePath = Path()
    private val dropPath = Path()
    private val crestPath = Path()

    override fun draw(f: AnimationFrame) {
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.25f)
        val timeSec = f.time

        // ── 1. Base Organic Water Surface Wave Geometry ──
        val baseSpan = (50f + growth * 180f) * f.size
        val baseDepth = (f.stretch * 0.75f).coerceAtMost(220f * f.size)

        wavePath.reset()
        crestPath.reset()
        val steps = 36
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat().pow(1.8f)
            val ripple = sin(u * PI * 4.0 + timeSec * 3.0) * (4.0 + growth * 8.0)
            val d = (baseDepth + ripple.toFloat()) * env

            val pt = point(f, f.touch - baseSpan + u * baseSpan * 2f, d)
            if (i == 0) {
                wavePath.moveTo(pt.first, pt.second)
                crestPath.moveTo(pt.first, pt.second)
            } else {
                wavePath.lineTo(pt.first, pt.second)
                crestPath.lineTo(pt.first, pt.second)
            }
        }
        close(wavePath, f, baseSpan)

        // 3D Drop Shadow for Base Water Wave
        Physics3DEngine.drawDropShadow(f.canvas, wavePath, dx = 6f, dy = 10f, opacity = f.opacity * 0.45f)

        // Draw Base Water Surface Wave
        val brightColor = lighten(f.color, 0.40f)
        val deepColor = darken(f.color, 0.35f)
        wavePaint.shader = gradient(f, baseDepth, withAlpha(brightColor, (240 * f.opacity).toInt()), withAlpha(deepColor, (185 * f.opacity).toInt()))
        f.canvas.drawPath(wavePath, wavePaint)

        crestPaint.strokeWidth = 3.0f * f.size
        crestPaint.color = withAlpha(lighten(f.color, 0.85f), (235 * f.opacity).toInt())
        f.canvas.drawPath(crestPath, crestPaint)

        // ── 2. Liquid Droplet Breaking Away via Surface Tension ──
        val dropDepth = (baseDepth + growth * (120f + f.surfaceTension * 35f)).coerceAtMost(380f * f.size)
        val dropRadius = (12f + growth * (36f + f.surfaceTension * 15f)) * f.size
        val neckWidth = (dropRadius * (1.25f - growth * 0.70f)).coerceAtLeast(3.5f * f.size)
        val wobble = sin(timeSec * PI * 3.0).toFloat() * dropRadius * 0.06f

        val proj = Physics3DEngine.project(dropDepth, 0f, 25f * growth)
        val dropCenter = when (f.edge) {
            Edge.LEFT -> proj.x to f.touch
            Edge.RIGHT -> (f.width - proj.x) to f.touch
            Edge.BOTTOM -> f.touch to (f.height - proj.x)
        }

        dropPath.reset()
        val detached = f.progress >= 1.05f
        when (f.edge) {
            Edge.LEFT -> if (detached) dropPath.addCircle(dropCenter.first, dropCenter.second, dropRadius, Path.Direction.CW) else {
                dropPath.moveTo(baseDepth, f.touch - neckWidth)
                dropPath.cubicTo(baseDepth + proj.x * 0.3f, f.touch - neckWidth, dropCenter.first - dropRadius * 0.9f, dropCenter.second - dropRadius + wobble, dropCenter.first, dropCenter.second - dropRadius)
                dropPath.cubicTo(dropCenter.first + dropRadius, dropCenter.second - dropRadius, dropCenter.first + dropRadius, dropCenter.second + dropRadius, dropCenter.first, dropCenter.second + dropRadius)
                dropPath.cubicTo(dropCenter.first - dropRadius * 0.9f, dropCenter.second + dropRadius, baseDepth + proj.x * 0.3f, f.touch + neckWidth, baseDepth, f.touch + neckWidth)
            }
            Edge.RIGHT -> if (detached) dropPath.addCircle(dropCenter.first, dropCenter.second, dropRadius, Path.Direction.CW) else {
                dropPath.moveTo(f.width - baseDepth, f.touch - neckWidth)
                dropPath.cubicTo(f.width - baseDepth - proj.x * 0.3f, f.touch - neckWidth, dropCenter.first + dropRadius * 0.9f, dropCenter.second - dropRadius + wobble, dropCenter.first, dropCenter.second - dropRadius)
                dropPath.cubicTo(dropCenter.first - dropRadius, dropCenter.second - dropRadius, dropCenter.first - dropRadius, dropCenter.second + dropRadius, dropCenter.first, dropCenter.second + dropRadius)
                dropPath.cubicTo(dropCenter.first + dropRadius * 0.9f, dropCenter.second + dropRadius, f.width - baseDepth - proj.x * 0.3f, f.touch + neckWidth, f.width - baseDepth, f.touch + neckWidth)
            }
            Edge.BOTTOM -> if (detached) dropPath.addCircle(dropCenter.first, dropCenter.second, dropRadius, Path.Direction.CW) else {
                dropPath.moveTo(f.touch - neckWidth, f.height - baseDepth)
                dropPath.cubicTo(f.touch - neckWidth, f.height - baseDepth - proj.x * 0.3f, dropCenter.first - dropRadius, dropCenter.second + dropRadius * 0.9f, dropCenter.first - dropRadius, dropCenter.second)
                dropPath.cubicTo(dropCenter.first - dropRadius, dropCenter.second - dropRadius, dropCenter.first + dropRadius, dropCenter.second - dropRadius, dropCenter.first + dropRadius, dropCenter.second)
                dropPath.cubicTo(dropCenter.first + dropRadius, dropCenter.second + dropRadius * 0.9f, f.touch + neckWidth, f.height - baseDepth - proj.x * 0.3f, f.touch + neckWidth, f.height - baseDepth)
            }
        }
        dropPath.close()

        // 3D Drop Shadow for Liquid Droplet
        Physics3DEngine.drawDropShadow(f.canvas, dropPath, dx = 7f, dy = 10f, opacity = f.opacity * 0.55f)

        // Render Water Droplet Body
        val specular = Physics3DEngine.computeSpecularLight(0.3f, -0.7f, 0.8f, shininess = 24f)
        val dropBright = lighten(f.color, 0.55f + specular * 0.35f)

        dropPaint.shader = RadialGradient(
            dropCenter.first - dropRadius * 0.35f, dropCenter.second - dropRadius * 0.35f, dropRadius * 2.2f,
            intArrayOf(withAlpha(dropBright, (250 * f.opacity).toInt()), withAlpha(f.color, (225 * f.opacity).toInt()), withAlpha(darken(f.color, 0.45f), (160 * f.opacity).toInt())),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawPath(dropPath, dropPaint)

        // Specular Refraction Glint
        glintPaint.color = withAlpha(Color.WHITE, (245 * f.opacity).toInt())
        f.canvas.drawCircle(dropCenter.first - dropRadius * 0.35f, dropCenter.second - dropRadius * 0.35f, (dropRadius * 0.30f).coerceAtLeast(3f), glintPaint)

        wavePaint.shader = null
        dropPaint.shader = null
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
