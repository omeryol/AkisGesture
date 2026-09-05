package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.sin

/**
 * Aerodynamic Laminar Wind & Silk Ribbon Streamlines.
 * Renders organic air current ribbons billowing from the bezel with aerodynamic
 * von Kármán harmonic flutter, fluid velocity tapering, and suspended wind dust wisps.
 */
class WindModule : NaturalAnimationModule {
    private val ribbonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val wispPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val ribbonPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.3f)
        val depth = (18f + f.stretch * 1.20f).coerceAtMost(380f * f.size)
        val span = (46f + f.progress * 210f) * f.size

        // ── 1. Aerodynamic Flutter Ribbons (5 Flowing Streamlines) ──
        val ribbonCount = 5
        for (r in 0 until ribbonCount) {
            ribbonPath.reset()
            val rOffset = (r - 2) * (span * 0.28f)
            val rPhase = r * 0.45f
            val rSpeed = 1.0f + r * 0.2f
            val steps = 36

            for (i in 0..steps) {
                val u = i / steps.toFloat()
                val d = depth * u
                // Flutter wave increases amplitude toward ribbon tip
                val flutter = sin(u * PI * 3.5 + timeSec * 4.2 * rSpeed + rPhase) * (u * 14.0 * f.size)
                val along = f.touch + rOffset + flutter.toFloat()

                val p = point(f, along, d)
                if (i == 0) ribbonPath.moveTo(p.first, p.second) else ribbonPath.lineTo(p.first, p.second)
            }

            // Tapered stroke: center ribbons thicker, outer ribbons delicate
            val thickness = when (r) {
                2 -> 3.2f
                1, 3 -> 2.4f
                else -> 1.8f
            } * f.size

            val streamColor = blend(f.color, 0xFFE0F7FA.toInt(), 0.45f)
            val alphaVal = ((220 - kotlin.math.abs(r - 2) * 35) * f.opacity).toInt().coerceIn(0, 255)
            ribbonPaint.strokeWidth = thickness
            ribbonPaint.color = alpha(streamColor, alphaVal)
            f.canvas.drawPath(ribbonPath, ribbonPaint)
        }

        // ── 2. Suspended Micro Wind Dust Wisps Gliding Along Airflow ──
        val wispCount = 12
        for (i in 0 until wispCount) {
            val seed = i * 27.7f
            val phase = ((timeSec * 0.85 + seed) % 1.0).toFloat()
            val wDist = depth * phase * 1.05f
            val rIndex = i % ribbonCount
            val rOffset = (rIndex - 2) * (span * 0.28f)
            val flutter = sin(phase * PI * 3.5 + timeSec * 4.2 + rIndex) * (phase * 14.0 * f.size)
            val along = f.touch + rOffset + flutter.toFloat()

            val p = point(f, along, wDist)
            val wispAlpha = ((sin(phase * PI) * 220) * f.opacity).toInt().coerceIn(0, 255)
            val wispSize = (1.5f + (1f - phase) * 2.2f) * f.size

            wispPaint.color = alpha(Color.WHITE, wispAlpha)
            f.canvas.drawCircle(p.first, p.second, wispSize, wispPaint)
        }
    }

    private fun point(f: AnimationFrame, along: Float, depth: Float) = when (f.edge) {
        Edge.LEFT -> depth to along
        Edge.RIGHT -> (f.width - depth) to along
        Edge.BOTTOM -> along to (f.height - depth)
    }
}
