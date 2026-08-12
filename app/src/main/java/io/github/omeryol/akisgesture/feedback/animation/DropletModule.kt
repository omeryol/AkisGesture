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

/** Unified Single-Path Water Surface Wave with Elastic Surface Tension Necking Droplet. */
class DropletModule : NaturalAnimationModule {
    private val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val crestPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val glintPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val unifiedPath = Path()
    private val crestPath = Path()

    override fun draw(f: AnimationFrame) {
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.25f)
        val timeSec = f.time

        // ── Unified Water Surface & Elastic Surface Tension Droplet Parameters ──
        val baseSpan = (50f + growth * 160f) * f.size
        val baseDepth = (f.stretch * 0.70f).coerceAtMost(200f * f.size)

        // Refined droplet head radius (scaled down 1 notch for elegance: 9f to 26f)
        val dropRadius = (9f + growth * (24f + f.surfaceTension * 10f)) * f.size
        val dropDepth = (baseDepth + growth * (100f + f.surfaceTension * 30f)).coerceAtMost(340f * f.size)

        // Elastic Necking: Connection thins out dynamically as stretch increases, but NEVER breaks
        val neckHalfWidth = (dropRadius * (1.15f - growth * 0.65f)).coerceAtLeast(3.5f * f.size)
        val wobble = sin(timeSec * PI * 2.8).toFloat() * dropRadius * 0.05f

        val proj = Physics3DEngine.project(dropDepth, 0f, 25f * growth)
        val dropCenter = when (f.edge) {
            Edge.LEFT -> proj.x to f.touch
            Edge.RIGHT -> (f.width - proj.x) to f.touch
            Edge.BOTTOM -> f.touch to (f.height - proj.x)
        }

        // ── 1. Construct ONE Single Unified Fluid Path ──
        unifiedPath.reset()
        crestPath.reset()

        val steps = 40
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat().pow(1.8f)

            // Dynamic bulge at wave center where elastic surface tension neck connects droplet head
            val distFromCenter = Math.abs(u - 0.5f) * 2f // 0 at center, 1 at ends
            val dropInfluence = (1f - distFromCenter.pow(4f)).coerceIn(0f, 1f)

            val ripple = sin(u * PI * 4.0 + timeSec * 3.0) * (3.0 + growth * 6.0)
            val d = (baseDepth + ripple.toFloat()) * env + (dropDepth - baseDepth) * dropInfluence

            val pt = point(f, f.touch - baseSpan + u * baseSpan * 2f, d)
            if (i == 0) {
                unifiedPath.moveTo(pt.first, pt.second)
                crestPath.moveTo(pt.first, pt.second)
            } else {
                unifiedPath.lineTo(pt.first, pt.second)
                crestPath.lineTo(pt.first, pt.second)
            }
        }
        close(unifiedPath, f, baseSpan)

        // ── 2. 3D Drop Shadow for Unified Fluid Body ──
        Physics3DEngine.drawDropShadow(f.canvas, unifiedPath, dx = 6f, dy = 10f, opacity = f.opacity * 0.50f)

        // ── 3. Fill Unified Fluid Body with Gradient Shading ──
        val brightColor = lighten(f.color, 0.45f)
        val deepColor = darken(f.color, 0.35f)
        mainPaint.shader = gradient(f, dropDepth, withAlpha(brightColor, (245 * f.opacity).toInt()), withAlpha(deepColor, (185 * f.opacity).toInt()))
        f.canvas.drawPath(unifiedPath, mainPaint)

        // ── 4. Surface Tension Specular Crest Highlight Line ──
        crestPaint.strokeWidth = 3.0f * f.size
        crestPaint.color = withAlpha(lighten(f.color, 0.85f), (240 * f.opacity).toInt())
        f.canvas.drawPath(crestPath, crestPaint)

        // ── 5. Refraction Glint Spot on Droplet Head ──
        val specular = Physics3DEngine.computeSpecularLight(0.3f, -0.7f, 0.8f, shininess = 24f)
        glintPaint.color = withAlpha(Color.WHITE, ((210 + specular * 45f) * f.opacity).toInt().coerceIn(0, 255))
        f.canvas.drawCircle(dropCenter.first - dropRadius * 0.35f, dropCenter.second - dropRadius * 0.35f, (dropRadius * 0.30f).coerceAtLeast(2.5f), glintPaint)

        mainPaint.shader = null
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
