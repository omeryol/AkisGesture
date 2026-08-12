package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Canvas
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

/** 3D Natural Flame Simulation with thermal buoyancy acceleration, 3D shadows, and glowing coal bed. */
class FireModule : NaturalAnimationModule {
    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outerFlamePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val innerFlamePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val coreFlamePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val coalPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sparkPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val outerPath = Path()
    private val innerPath = Path()
    private val corePath = Path()

    // 16 persistent ember sparks
    private val embers = Array(16) { SparkParticle(it) }

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1f).pow(1.5f)
        val flameLength = (20f + growth * (250f + f.surfaceTension * 60f)) * f.size
        val flameWidth = (24f + growth * (130f + f.surfaceTension * 30f)) * f.size

        val origin = when (f.edge) {
            Edge.LEFT -> flameLength to f.touch
            Edge.RIGHT -> (f.width - flameLength) to f.touch
            Edge.BOTTOM -> f.touch to (f.height - flameLength)
        }

        // ── 1. LAYER: Outer Fiery Lobe Path Construction ──
        buildFlamePath(outerPath, f, flameLength, flameWidth, timeSec, 1.0f, 1.0f)
        buildFlamePath(innerPath, f, flameLength * 0.78f, flameWidth * 0.68f, timeSec + 0.35, 1.2f, 0.85f)
        buildFlamePath(corePath, f, flameLength * 0.48f, flameWidth * 0.38f, timeSec + 0.7, 1.5f, 0.70f)

        // ── 2. LAYER: 3D Drop Shadow Cast by Flame Body ──
        Physics3DEngine.drawDropShadow(f.canvas, outerPath, dx = 10f, dy = 14f, opacity = f.opacity * 0.55f)

        // ── 3. LAYER: Ambient Fire Aura Glow ──
        val auraRadius = flameWidth * 1.6f
        auraPaint.shader = RadialGradient(
            origin.first, origin.second, auraRadius,
            intArrayOf(withAlpha(0xFFFF3D00.toInt(), (150 * f.opacity).toInt()), withAlpha(0xFFDD2C00.toInt(), (65 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawCircle(origin.first, origin.second, auraRadius, auraPaint)

        // ── 4. LAYER: Outer Fiery Lobe (Crimson / Deep Orange) ──
        outerFlamePaint.shader = flameGradient(
            f, flameLength,
            withAlpha(0xFFFF6D00.toInt(), (240 * f.opacity).toInt()),
            withAlpha(0xFFDD2C00.toInt(), (200 * f.opacity).toInt())
        )
        f.canvas.drawPath(outerPath, outerFlamePaint)

        // ── 5. LAYER: Mid Flame Lobe (Golden Yellow & Bright Orange) ──
        innerFlamePaint.shader = flameGradient(
            f, flameLength * 0.78f,
            withAlpha(0xFFFFD600.toInt(), (248 * f.opacity).toInt()),
            withAlpha(0xFFFFAB00.toInt(), (215 * f.opacity).toInt())
        )
        f.canvas.drawPath(innerPath, innerFlamePaint)

        // ── 6. LAYER: Inner Incandescent Core (White / Light Yellow) ──
        coreFlamePaint.shader = flameGradient(
            f, flameLength * 0.48f,
            withAlpha(0xFFFFFFFF.toInt(), (255 * f.opacity).toInt()),
            withAlpha(0xFFFFEA00.toInt(), (235 * f.opacity).toInt())
        )
        f.canvas.drawPath(corePath, coreFlamePaint)

        // ── 7. LAYER: Burning Coal Bed (Köz Yatağı at Flame Root) ──
        drawGlowingCoalBed(f, flameWidth, timeSec)

        // ── 8. LAYER: Thermal Buoyancy Rising Embers Physics ──
        if (growth > 0.06f) {
            embers.forEach { spark ->
                spark.updateAndDraw(f, origin, flameLength, flameWidth, timeSec)
            }
        }

        auraPaint.shader = null
        outerFlamePaint.shader = null
        innerFlamePaint.shader = null
        coreFlamePaint.shader = null
        coalPaint.shader = null
    }

    private fun buildFlamePath(
        path: Path,
        f: AnimationFrame,
        length: Float,
        width: Float,
        timeSec: Double,
        speedMult: Float,
        widthMult: Float
    ) {
        path.reset()
        val steps = 32
        val baseTouch = f.touch

        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat()
            
            val flicker1 = sin(u * PI * 3.5 + timeSec * 6.5 * speedMult) * (7.0 + length * 0.09)
            val flicker2 = cos(u * PI * 7.0 - timeSec * 9.0 * speedMult) * (3.5 + length * 0.05)
            val sway = (flicker1 + flicker2).toFloat() * u * widthMult

            // 3D Perspective Projection for Z-depth
            val zDepth = u * 40f
            val proj = Physics3DEngine.project(length * u, 0f, zDepth)

            val along = baseTouch - width * env + sway
            val pt = point(f, along, proj.x)

            if (i == 0) path.moveTo(pt.first, pt.second) else path.lineTo(pt.first, pt.second)
        }

        closeOrganicBase(path, f, width, timeSec)
    }

    private fun drawGlowingCoalBed(f: AnimationFrame, width: Float, timeSec: Double) {
        val count = 7
        val baseTouch = f.touch
        for (i in 0 until count) {
            val u = i / (count - 1).toFloat()
            val along = baseTouch - width * 0.75f + u * width * 1.5f
            val pulse = (sin(timeSec * 4.0 + i * 1.3) * 0.25 + 0.75).toFloat()
            val coalRadius = (8f + pulse * 6f) * f.size
            val pt = point(f, along, coalRadius * 0.6f)

            coalPaint.shader = RadialGradient(
                pt.first, pt.second, coalRadius * 1.8f,
                intArrayOf(withAlpha(0xFFFFFFFF.toInt(), (240 * f.opacity).toInt()), withAlpha(0xFFFFAB00.toInt(), (200 * f.opacity).toInt()), withAlpha(0xFFDD2C00.toInt(), (100 * f.opacity).toInt()), Color.TRANSPARENT),
                floatArrayOf(0f, 0.35f, 0.75f, 1f),
                Shader.TileMode.CLAMP
            )
            f.canvas.drawCircle(pt.first, pt.second, coalRadius * 1.8f, coalPaint)
        }
    }

    private fun point(f: AnimationFrame, along: Float, depth: Float) = when (f.edge) {
        Edge.LEFT -> depth to along
        Edge.RIGHT -> (f.width - depth) to along
        Edge.BOTTOM -> along to (f.height - depth)
    }

    private fun closeOrganicBase(path: Path, f: AnimationFrame, width: Float, timeSec: Double) {
        val baseBulge = (sin(timeSec * 3.5) * 8.0 + 12.0).toFloat()
        when (f.edge) {
            Edge.LEFT -> {
                path.quadTo(baseBulge, f.touch + width * 0.5f, 0f, f.touch + width)
                path.lineTo(0f, f.touch - width)
                path.quadTo(baseBulge, f.touch - width * 0.5f, point(f, f.touch, 0f).first, point(f, f.touch, 0f).second)
            }
            Edge.RIGHT -> {
                path.quadTo(f.width - baseBulge, f.touch + width * 0.5f, f.width, f.touch + width)
                path.lineTo(f.width, f.touch - width)
                path.quadTo(f.width - baseBulge, f.touch - width * 0.5f, point(f, f.touch, 0f).first, point(f, f.touch, 0f).second)
            }
            Edge.BOTTOM -> {
                path.quadTo(f.touch + width * 0.5f, f.height - baseBulge, f.touch + width, f.height)
                path.lineTo(f.touch - width, f.height)
                path.quadTo(f.touch - width * 0.5f, f.height - baseBulge, point(f, f.touch, 0f).first, point(f, f.touch, 0f).second)
            }
        }
        path.close()
    }

    private fun flameGradient(f: AnimationFrame, depth: Float, colorInner: Int, colorOuter: Int) = when (f.edge) {
        Edge.LEFT -> LinearGradient(depth, f.touch, 0f, f.touch, colorInner, colorOuter, Shader.TileMode.CLAMP)
        Edge.RIGHT -> LinearGradient(f.width - depth, f.touch, f.width, f.touch, colorInner, colorOuter, Shader.TileMode.CLAMP)
        Edge.BOTTOM -> LinearGradient(f.touch, f.height - depth, f.touch, f.height, colorInner, colorOuter, Shader.TileMode.CLAMP)
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))

    private inner class SparkParticle(val index: Int) {
        private val seed = index * 37.19f
        fun updateAndDraw(f: AnimationFrame, origin: Pair<Float, Float>, flameLength: Float, flameWidth: Float, timeSec: Double) {
            val cycleLength = 0.85 + (index % 5) * 0.15
            val life = ((timeSec * 1.4 + seed) % cycleLength) / cycleLength
            val phase = life.toFloat()

            // Thermal buoyancy physics acceleration (rising faster as phase increases: phase^1.3)
            val buoyancyPhase = phase.pow(1.3f)
            val driftAlong = sin(buoyancyPhase * PI * 2.5 + seed).toFloat() * (flameWidth * 0.45f)
            val driftDepth = flameLength * (0.2f + buoyancyPhase * 0.95f)

            // 3D Perspective Projection for Particle
            val zDepth = buoyancyPhase * 60f
            val proj = Physics3DEngine.project(driftDepth, driftAlong, zDepth)

            val pt = when (f.edge) {
                Edge.LEFT -> proj.x to (origin.second + proj.y)
                Edge.RIGHT -> (f.width - proj.x) to (origin.second + proj.y)
                Edge.BOTTOM -> (origin.first + proj.y) to (f.height - proj.x)
            }

            val radius = (4.0f * proj.scale * (1f - phase * 0.7f) * f.size).coerceAtLeast(0.9f)
            val alpha = ((1f - phase) * 240 * f.opacity).toInt().coerceIn(0, 255)

            // Draw particle shadow
            sparkPaint.color = Color.argb((alpha * 0.3f).toInt(), 0, 0, 0)
            f.canvas.drawCircle(pt.first + 3f, pt.second + 5f, radius, sparkPaint)

            val sparkColor = if (index % 2 == 0) 0xFFFFFF00.toInt() else 0xFFFF5500.toInt()
            sparkPaint.color = withAlpha(sparkColor, alpha)
            f.canvas.drawCircle(pt.first, pt.second, radius, sparkPaint)
        }
    }
}
