package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.sin

class PressureWaveModule : NaturalAnimationModule {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun draw(f: AnimationFrame) {
        val depth = (8f + f.stretch * 1.05f).coerceAtMost(300f * f.size)
        val tip = when(f.edge){ Edge.LEFT->depth to f.touch; Edge.RIGHT->f.width-depth to f.touch; Edge.BOTTOM->f.touch to f.height-depth }
        for (layer in 3 downTo 0) {
            val phase = ((f.time * .72 + layer * .19) % 1.0).toFloat()
            val radius = (28f + f.progress * (60f + f.surfaceTension * 30f) + phase * (45f + f.damping * 22f)) * f.size
            val squeeze = .48f + layer * .06f + f.surfaceTension * .08f
            val alpha = ((1f-phase) * (105 + layer*22) * f.opacity).toInt()
            paint.shader = RadialGradient(tip.first,tip.second,radius*1.5f,intArrayOf(withAlpha(lighten(f.color,.38f),alpha),withAlpha(f.color,alpha/2),Color.TRANSPARENT),floatArrayOf(0f,.48f,1f),Shader.TileMode.CLAMP)
            val pulse = 1f + sin(f.time*(2.2 + f.damping * 1.6)+layer).toFloat()*(.035f + f.viscosity * .045f)
            f.canvas.drawOval(RectF(tip.first-radius*pulse,tip.second-radius*squeeze,tip.first+radius*pulse,tip.second+radius*squeeze),paint)
        }
        paint.shader=null
    }
    private fun withAlpha(c:Int,a:Int)=Color.argb(a.coerceIn(0,255),Color.red(c),Color.green(c),Color.blue(c))
    private fun lighten(c:Int,t:Float)=Color.rgb((Color.red(c)+(255-Color.red(c))*t).toInt(),(Color.green(c)+(255-Color.green(c))*t).toInt(),(Color.blue(c)+(255-Color.blue(c))*t).toInt())
}
