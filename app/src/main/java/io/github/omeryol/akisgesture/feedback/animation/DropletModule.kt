package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.feedback.Physics3DEngine
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.pow

/** Cohesive Surface Tension Liquid Membrane & Mercury Teardrop Pinching Physics. */
class DropletModule : NaturalAnimationModule {
    private val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val splashPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val path = Path()

    override fun draw(f: AnimationFrame) {
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.25f)
        val depth = (15f + f.stretch * (1.22f + f.surfaceTension * 0.28f)).coerceAtMost(400f * f.size)
        val r = (16f + growth * (70f + f.surfaceTension * 25f)) * f.size
        
        // Elastic Surface Tension Necking Equation: Neck narrows dramatically as stretch increases
        val neckWidth = (r * (1.35f - growth * (0.75f + f.surfaceTension * 0.15f))).coerceAtLeast(4f * f.size)
        val wobble = sin(f.time * PI * (2.2 + f.damping * 1.5)).toFloat() * r * (0.05f + f.viscosity * 0.04f)

        // 3D Perspective Projection for Center
        val proj = Physics3DEngine.project(depth, 0f, 30f * growth)
        val center = when (f.edge) {
            Edge.LEFT -> proj.x to f.touch
            Edge.RIGHT -> (f.width - proj.x) to f.touch
            Edge.BOTTOM -> f.touch to (f.height - proj.x)
        }

        // ── 1. LAYER: Elastic Liquid Surface Tension Membrane Path ──
        path.reset()
        val detached = f.progress >= 1.05f
        when (f.edge) {
            Edge.LEFT -> if (detached) path.addCircle(center.first, center.second, r, Path.Direction.CW) else {
                path.moveTo(0f, f.touch - neckWidth)
                path.cubicTo(proj.x * 0.35f, f.touch - neckWidth, center.first - r * 0.95f, center.second - r + wobble, center.first, center.second - r)
                path.cubicTo(center.first + r, center.second - r, center.first + r, center.second + r, center.first, center.second + r)
                path.cubicTo(center.first - r * 0.95f, center.second + r, proj.x * 0.35f, f.touch + neckWidth, 0f, f.touch + neckWidth)
            }
            Edge.RIGHT -> if (detached) path.addCircle(center.first, center.second, r, Path.Direction.CW) else {
                path.moveTo(f.width, f.touch - neckWidth)
                path.cubicTo(f.width - proj.x * 0.35f, f.touch - neckWidth, center.first + r * 0.95f, center.second - r + wobble, center.first, center.second - r)
                path.cubicTo(center.first - r, center.second - r, center.first - r, center.second + r, center.first, center.second + r)
                path.cubicTo(center.first + r * 0.95f, center.second + r, f.width - proj.x * 0.35f, f.touch + neckWidth, f.width, f.touch + neckWidth)
            }
            Edge.BOTTOM -> if (detached) path.addCircle(center.first, center.second, r, Path.Direction.CW) else {
                path.moveTo(f.touch - neckWidth, f.height)
                path.cubicTo(f.touch - neckWidth, f.height - proj.x * 0.35f, center.first - r, center.second + r * 0.95f, center.first - r, center.second)
                path.cubicTo(center.first - r, center.second - r, center.first + r, center.second - r, center.first + r, center.second)
                path.cubicTo(center.first + r, center.second + r * 0.95f, f.touch + neckWidth, f.height - proj.x * 0.35f, f.touch + neckWidth, f.height)
            }
        }
        path.close()

        // ── 2. LAYER: 3D Dynamic Drop Shadow ──
        Physics3DEngine.drawDropShadow(f.canvas, path, dx = 8f, dy = 12f, opacity = f.opacity * 0.60f)

        // ── 3. LAYER: Surface Tension Ambient Halo ──
        auraPaint.shader = RadialGradient(
            center.first, center.second, r * 2.4f,
            intArrayOf(withAlpha(lighten(f.color, 0.45f), (125 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawCircle(center.first, center.second, r * 2.4f, auraPaint)

        // ── 4. LAYER: Metallic Mercury Surface Body with Blinn-Phong Specular Lighting ──
        val specular = Physics3DEngine.computeSpecularLight(0.35f, -0.75f, 0.85f, shininess = 28f)
        val brightColor = lighten(f.color, 0.60f + specular * 0.40f)

        mainPaint.shader = RadialGradient(
            center.first - r * 0.35f, center.second - r * 0.38f, r * 2.5f,
            intArrayOf(withAlpha(brightColor, (255 * f.opacity).toInt()), withAlpha(f.color, (235 * f.opacity).toInt()), withAlpha(darken(f.color, 0.55f), (170 * f.opacity).toInt())),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawPath(path, mainPaint)

        // ── 5. LAYER: Cohesive Surface Tension Specular Glint Spot ──
        highlightPaint.color = withAlpha(Color.WHITE, (245 * f.opacity).toInt())
        val glintX = center.first - r * 0.36f
        val glintY = center.second - r * 0.38f
        f.canvas.drawCircle(glintX, glintY, (r * 0.32f).coerceAtLeast(3.5f), highlightPaint)

        // ── 6. LAYER: Gravity-bound Pinch-off Satellite Droplets ──
        if (growth > 0.45f) {
            val sRadius = (r * 0.20f).coerceAtLeast(3.0f)
            val gDrop = depth * 1.38f
            val sPt = when (f.edge) {
                Edge.LEFT -> gDrop to (center.second + r * 1.35f)
                Edge.RIGHT -> (f.width - gDrop) to (center.second + r * 1.35f)
                Edge.BOTTOM -> (center.first + r * 1.35f) to (f.height - gDrop)
            }
            splashPaint.color = withAlpha(lighten(f.color, 0.65f), (220 * f.opacity).toInt())
            f.canvas.drawCircle(sPt.first + 2f, sPt.second + 4f, sRadius, splashPaint) // Shadow
            f.canvas.drawCircle(sPt.first, sPt.second, sRadius, splashPaint)
        }

        mainPaint.shader = null
        auraPaint.shader = null
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
    private fun lighten(c: Int, t: Float) = Color.rgb((Color.red(c) + (255 - Color.red(c)) * t).toInt().coerceIn(0, 255), (Color.green(c) + (255 - Color.green(c)) * t).toInt().coerceIn(0, 255), (Color.blue(c) + (255 - Color.blue(c)) * t).toInt().coerceIn(0, 255))
    private fun darken(c: Int, t: Float) = Color.rgb((Color.red(c) * (1f - t)).toInt().coerceIn(0, 255), (Color.green(c) * (1f - t)).toInt().coerceIn(0, 255), (Color.blue(c) * (1f - t)).toInt().coerceIn(0, 255))
}
