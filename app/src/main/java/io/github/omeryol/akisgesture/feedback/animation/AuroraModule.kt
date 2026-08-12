package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.sin

class AuroraModule : NaturalAnimationModule {
    private val ribbonPaint1 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ribbonPaint2 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val path1 = Path()
    private val path2 = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val depth = (16f + f.stretch * 1.20f).coerceAtMost(400f * f.size)
        val span = (45f + f.progress * 220f) * f.size

        // ── 1. Ribbon 1: Aurora Emerald Green to Cyan ──
        buildRibbonPath(path1, f, depth, span, timeSec, 1.0f, 0.0f)
        ribbonPaint1.shader = ribbonGradient(
            f, depth,
            withAlpha(0xFF00E676.toInt(), (230 * f.opacity).toInt()),
            withAlpha(0xFF00E5FF.toInt(), (150 * f.opacity).toInt())
        )
        f.canvas.drawPath(path1, ribbonPaint1)

        // ── 2. Ribbon 2: Cosmic Purple to Neon Magenta ──
        buildRibbonPath(path2, f, depth * 0.82f, span * 1.15f, timeSec + 0.5, 1.3f, 0.4f)
        ribbonPaint2.shader = ribbonGradient(
            f, depth * 0.82f,
            withAlpha(0xFFE040FB.toInt(), (200 * f.opacity).toInt()),
            withAlpha(0xFF651FFF.toInt(), (110 * f.opacity).toInt())
        )
        f.canvas.drawPath(path2, ribbonPaint2)

        // ── 3. Shimmering Aurora Dust Particles ──
        for (i in 0..10) {
            val phase = ((timeSec * 0.6 + i * 0.09) % 1.0).toFloat()
            val along = f.touch - span * 0.7f + i * (span * 1.4f / 10f)
            val d = depth * (0.3f + sin(timeSec * 2.0 + i).toFloat() * 0.4f)

            val p = when (f.edge) {
                Edge.LEFT -> d to along
                Edge.RIGHT -> (f.width - d) to along
                Edge.BOTTOM -> along to (f.height - d)
            }

            val alpha = ((sin(phase * PI) * 220) * f.opacity).toInt().coerceIn(0, 255)
            starPaint.color = withAlpha(0xFF80D8FF.toInt(), alpha)
            f.canvas.drawCircle(p.first, p.second, (2.5f + phase * 2f) * f.size, starPaint)
        }

        ribbonPaint1.shader = null
        ribbonPaint2.shader = null
    }

    private fun buildRibbonPath(path: Path, f: AnimationFrame, depth: Float, span: Float, timeSec: Double, speed: Float, phaseShift: Float) {
        path.reset()
        val steps = 40
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val env = sin(PI * u).toFloat()
            val wave = sin(u * PI * 4.0 + timeSec * 2.5 * speed + phaseShift) * (8.0 + depth * 0.12)
            val totalDepth = (depth + wave.toFloat()) * env

            val p = when (f.edge) {
                Edge.LEFT -> totalDepth to (f.touch - span + u * span * 2f)
                Edge.RIGHT -> (f.width - totalDepth) to (f.touch - span + u * span * 2f)
                Edge.BOTTOM -> (f.touch - span + u * span * 2f) to (f.height - totalDepth)
            }

            if (i == 0) path.moveTo(p.first, p.second) else path.lineTo(p.first, p.second)
        }

        when (f.edge) {
            Edge.LEFT -> { path.lineTo(0f, f.touch + span); path.lineTo(0f, f.touch - span) }
            Edge.RIGHT -> { path.lineTo(f.width, f.touch + span); path.lineTo(f.width, f.touch - span) }
            Edge.BOTTOM -> { path.lineTo(f.touch + span, f.height); path.lineTo(f.touch - span, f.height) }
        }
        path.close()
    }

    private fun ribbonGradient(f: AnimationFrame, depth: Float, c1: Int, c2: Int) = when (f.edge) {
        Edge.LEFT -> LinearGradient(0f, f.touch, depth, f.touch, c1, c2, Shader.TileMode.CLAMP)
        Edge.RIGHT -> LinearGradient(f.width, f.touch, f.width - depth, f.touch, c1, c2, Shader.TileMode.CLAMP)
        Edge.BOTTOM -> LinearGradient(f.touch, f.height, f.touch, f.height - depth, c1, c2, Shader.TileMode.CLAMP)
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
}
