package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import io.github.omeryol.akisgesture.feedback.Physics3DEngine
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Crystalline Frosted Shards & Geometric Prism Refraction Facets. */
class GlassRefractionModule : NaturalAnimationModule {
    private val shardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val prismEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val glintPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val shardPath = Path()

    override fun draw(f: AnimationFrame) {
        val timeSec = f.time
        val growth = (f.progress / 1.15f).coerceIn(0f, 1.25f)
        val depth = (20f + growth * 210f) * f.size
        val span = (50f + growth * 190f) * f.size
        val origin = point(f, f.touch, 0f)

        // ── 7 Geometric Crystalline Glass Shard Facets ──
        val shardCount = 7
        for (i in 0 until shardCount) {
            val u1 = i / shardCount.toFloat()
            val u2 = (i + 1) / shardCount.toFloat()

            val angle1 = when (f.edge) {
                Edge.LEFT -> (-PI / 2.2 + u1 * PI / 1.1)
                Edge.RIGHT -> (PI / 2.2 + u1 * PI / 1.1)
                Edge.BOTTOM -> (-PI + u1 * PI)
            }
            val angle2 = when (f.edge) {
                Edge.LEFT -> (-PI / 2.2 + u2 * PI / 1.1)
                Edge.RIGHT -> (PI / 2.2 + u2 * PI / 1.1)
                Edge.BOTTOM -> (-PI + u2 * PI)
            }

            // Polygon Shard Vertices with sharp crystalline geometry
            val r1 = depth * (0.75f + sin(i * 1.8 + timeSec * 1.5).toFloat() * 0.25f)
            val r2 = depth * (0.75f + cos(i * 2.3 - timeSec * 1.5).toFloat() * 0.25f)

            val p1x = origin.first + cos(angle1).toFloat() * r1
            val p1y = origin.second + sin(angle1).toFloat() * r1
            val p2x = origin.first + cos(angle2).toFloat() * r2
            val p2y = origin.second + sin(angle2).toFloat() * r2

            shardPath.reset()
            shardPath.moveTo(origin.first, origin.second)
            shardPath.lineTo(p1x, p1y)
            shardPath.lineTo(p2x, p2y)
            shardPath.close()

            // 1. 3D Drop Shadow for Shard
            Physics3DEngine.drawDropShadow(f.canvas, shardPath, dx = 7f, dy = 10f, opacity = f.opacity * 0.45f)

            // 2. Frosted Prism Shard Gradient Shading
            val specular = Physics3DEngine.computeSpecularLight(cos(angle1).toFloat(), sin(angle1).toFloat(), 0.8f, shininess = 22f)
            val brightColor = lighten(f.color, 0.65f + specular * 0.35f)

            shardPaint.shader = LinearGradient(
                origin.first, origin.second, p1x, p1y,
                withAlpha(brightColor, (235 * f.opacity).toInt()),
                withAlpha(f.color, (135 * f.opacity).toInt()),
                Shader.TileMode.CLAMP
            )
            f.canvas.drawPath(shardPath, shardPaint)

            // 3. Bright White Crystalline Prism Edges
            prismEdgePaint.strokeWidth = (2.8f + (i % 2) * 1.5f) * f.size
            prismEdgePaint.color = withAlpha(Color.WHITE, (245 * f.opacity).toInt())
            f.canvas.drawPath(shardPath, prismEdgePaint)

            // 4. Refraction Glint Spot on Shard Tip
            glintPaint.color = withAlpha(Color.WHITE, (250 * f.opacity).toInt())
            f.canvas.drawCircle(p1x, p1y, 3.5f * f.size, glintPaint)
        }

        shardPaint.shader = null
    }

    private fun point(f: AnimationFrame, along: Float, depth: Float) = when (f.edge) {
        Edge.LEFT -> depth to along
        Edge.RIGHT -> (f.width - depth) to along
        Edge.BOTTOM -> along to (f.height - depth)
    }

    private fun withAlpha(c: Int, a: Int) = Color.argb(a.coerceIn(0, 255), Color.red(c), Color.green(c), Color.blue(c))
    private fun lighten(c: Int, t: Float) = Color.rgb((Color.red(c) + (255 - Color.red(c)) * t).toInt().coerceIn(0, 255), (Color.green(c) + (255 - Color.green(c)) * t).toInt().coerceIn(0, 255), (Color.blue(c) + (255 - Color.blue(c)) * t).toInt().coerceIn(0, 255))
}
