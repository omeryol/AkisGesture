package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.cos
import kotlin.random.Random

/** High-realism, multi-layer natural flame simulation with flickering lobes and rising embers. */
class FireModule : NaturalAnimationModule {
    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outerFlamePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val innerFlamePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val coreFlamePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sparkPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val outerPath = Path()
    private val innerPath = Path()
    private val corePath = Path()

    // 16 persistent ember sparks
    private val embers = Array(16) { SparkParticle(it) }

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1f).pow(1.5f)
        val flameLength = (16f + growth * (240f + f.surfaceTension * 60f)) * f.size
        val flameWidth = (20f + growth * (120f + f.surfaceTension * 30f)) * f.size

        val origin = when (f.edge) {
            Edge.LEFT -> flameLength to f.touch
            Edge.RIGHT -> (f.width - flameLength) to f.touch
            Edge.BOTTOM -> f.touch to (f.height - flameLength)
        }

        // ── 1. LAYER: Ambient Fire Aura Glow ──
        val auraRadius = flameWidth * 1.5f
        auraPaint.shader = RadialGradient(
            origin.first, origin.second, auraRadius,
            intArrayOf(withAlpha(0xFFFF3D00.toInt(), (140 * f.opacity).toInt()), withAlpha(0xFFDD2C00.toInt(), (60 * f.opacity).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        f.canvas.drawCircle(origin.first, origin.second, auraRadius, auraPaint)

        // ── 2. LAYER: Outer Fiery Lobe (Crimson / Deep Orange) ──
        buildFlamePath(outerPath, f, flameLength, flameWidth, timeSec, 1.0f, 1.0f)
        outerFlamePaint.shader = flameGradient(
            f, flameLength,
            withAlpha(0xFFFF6D00.toInt(), (240 * f.opacity).toInt()),
            withAlpha(0xFFDD2C00.toInt(), (200 * f.opacity).toInt())
        )
        f.canvas.drawPath(outerPath, outerFlamePaint)

        // ── 3. LAYER: Mid Flame Lobe (Golden Yellow & Bright Orange) ──
        buildFlamePath(innerPath, f, flameLength * 0.75f, flameWidth * 0.65f, timeSec + 0.35, 1.2f, 0.85f)
        innerFlamePaint.shader = flameGradient(
            f, flameLength * 0.75f,
            withAlpha(0xFFFFD600.toInt(), (245 * f.opacity).toInt()),
            withAlpha(0xFFFFAB00.toInt(), (210 * f.opacity).toInt())
        )
        f.canvas.drawPath(innerPath, innerFlamePaint)

        // ── 4. LAYER: Inner Incandescent Core (White / Light Yellow) ──
        buildFlamePath(corePath, f, flameLength * 0.45f, flameWidth * 0.35f, timeSec + 0.7, 1.5f, 0.70f)
        coreFlamePaint.shader = flameGradient(
            f, flameLength * 0.45f,
            withAlpha(0xFFFFFFFF.toInt(), (255 * f.opacity).toInt()),
            withAlpha(0xFFFFEA00.toInt(), (230 * f.opacity).toInt())
        )
        f.canvas.drawPath(corePath, coreFlamePaint)

        // ── 5. LAYER: Rising Embers & Sparks Physics ──
        if (growth > 0.08f) {
            embers.forEach { spark ->
                spark.updateAndDraw(f, origin, flameLength, flameWidth, timeSec)
            }
        }

        auraPaint.shader = null
        outerFlamePaint.shader = null
        innerFlamePaint.shader = null
        coreFlamePaint.shader = null
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
        val steps = 30
        val baseTouch = f.touch

        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat()
            
            // Dynamic flickering turbulence equation
            val flicker1 = sin(u * PI * 3.5 + timeSec * 6.5 * speedMult) * (6.0 + length * 0.08)
            val flicker2 = cos(u * PI * 7.0 - timeSec * 9.0 * speedMult) * (3.0 + length * 0.04)
            val sway = (flicker1 + flicker2).toFloat() * u * widthMult

            val depth = length * u
            val along = baseTouch - width * env + sway

            val pt = point(f, along, depth)
            if (i == 0) path.moveTo(pt.first, pt.second) else path.lineTo(pt.first, pt.second)
        }

        closeBase(path, f, width)
    }

    private fun point(f: AnimationFrame, along: Float, depth: Float) = when (f.edge) {
        Edge.LEFT -> depth to along
        Edge.RIGHT -> (f.width - depth) to along
        Edge.BOTTOM -> along to (f.height - depth)
    }

    private fun closeBase(path: Path, f: AnimationFrame, width: Float) {
        when (f.edge) {
            Edge.LEFT -> {
                path.lineTo(0f, f.touch + width)
                path.lineTo(0f, f.touch - width)
            }
            Edge.RIGHT -> {
                path.lineTo(f.width, f.touch + width)
                path.lineTo(f.width, f.touch - width)
            }
            Edge.BOTTOM -> {
                path.lineTo(f.touch + width, f.height)
                path.lineTo(f.touch - width, f.height)
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

            val driftAlong = sin(phase * PI * 2.5 + seed).toFloat() * (flameWidth * 0.45f)
            val driftDepth = flameLength * (0.2f + phase * 0.95f)

            val pt = when (f.edge) {
                Edge.LEFT -> driftDepth to (origin.second + driftAlong)
                Edge.RIGHT -> (f.width - driftDepth) to (origin.second + driftAlong)
                Edge.BOTTOM -> (origin.first + driftAlong) to (f.height - driftDepth)
            }

            val radius = (3.5f * (1f - phase * 0.7f) * f.size).coerceAtLeast(0.8f)
            val alpha = ((1f - phase) * 235 * f.opacity).toInt().coerceIn(0, 255)

            val sparkColor = if (index % 2 == 0) 0xFFFFFF00.toInt() else 0xFFFF5500.toInt()
            sparkPaint.color = withAlpha(sparkColor, alpha)
            f.canvas.drawCircle(pt.first, pt.second, radius, sparkPaint)
        }
    }
}
