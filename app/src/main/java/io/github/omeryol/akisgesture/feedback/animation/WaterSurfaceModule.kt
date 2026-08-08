package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

class WaterSurfaceModule : NaturalAnimationModule {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    override fun draw(f: AnimationFrame) {
        val growth = (f.progress / 1.35f).coerceIn(0f, 1f).pow(2.35f)
        val span = (2f + growth * (250f + f.surfaceTension * 75f)) * f.size
        val depth = (f.stretch * (1.12f + f.surfaceTension * .28f)).coerceAtMost(380f * f.size)
        path.reset()
        for (i in 0..40) {
            val u = i / 40f
            val envelope = sin(PI * u).toFloat()
            val ripple = (
                sin(u * PI * (3.4 + f.surfaceTension * 1.4) + f.time * (1.5 + f.damping * 1.4)) * (2.0 + growth * (8.0 + f.surfaceTension * 5.0)) +
                    sin(u * PI * 7.0 - f.time * (1.0 + f.viscosity)) * growth * (3.0 + f.viscosity * 4.0)
                ).toFloat()
            val p = point(f, f.touch - span + u * span * 2f, (depth + ripple) * envelope)
            if (i == 0) path.moveTo(p.first, p.second) else path.lineTo(p.first, p.second)
        }
        close(f, span)
        val bright = lighten(f.color, .38f)
        paint.shader = gradient(f, depth, withAlpha(f.color, (225 * f.opacity).toInt()), withAlpha(bright, (185 * f.opacity).toInt()))
        f.canvas.drawPath(path, paint)
        paint.shader = null
    }

    private fun point(f: AnimationFrame, along: Float, depth: Float) = when (f.edge) {
        Edge.LEFT -> depth to along; Edge.RIGHT -> f.width - depth to along; Edge.BOTTOM -> along to f.height - depth
    }
    private fun close(f: AnimationFrame, span: Float) { when (f.edge) { Edge.LEFT -> { path.lineTo(0f, f.touch + span); path.lineTo(0f, f.touch - span) }; Edge.RIGHT -> { path.lineTo(f.width, f.touch + span); path.lineTo(f.width, f.touch - span) }; Edge.BOTTOM -> { path.lineTo(f.touch + span, f.height); path.lineTo(f.touch - span, f.height) } }; path.close() }
    private fun gradient(f: AnimationFrame, depth: Float, a: Int, b: Int) = when (f.edge) { Edge.LEFT -> LinearGradient(0f, f.touch, depth, f.touch, a, b, Shader.TileMode.CLAMP); Edge.RIGHT -> LinearGradient(f.width, f.touch, f.width-depth, f.touch, a, b, Shader.TileMode.CLAMP); Edge.BOTTOM -> LinearGradient(f.touch, f.height, f.touch, f.height-depth, a, b, Shader.TileMode.CLAMP) }
    private fun withAlpha(c:Int,a:Int)=Color.argb(a.coerceIn(0,255),Color.red(c),Color.green(c),Color.blue(c))
    private fun blend(a:Int,b:Int,t:Float)=Color.rgb((Color.red(a)+(Color.red(b)-Color.red(a))*t).toInt(),(Color.green(a)+(Color.green(b)-Color.green(a))*t).toInt(),(Color.blue(a)+(Color.blue(b)-Color.blue(a))*t).toInt())
}
