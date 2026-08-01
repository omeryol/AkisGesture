package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class VortexModule:NaturalAnimationModule{
 private val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE;strokeCap=Paint.Cap.ROUND};private val core=Paint(Paint.ANTI_ALIAS_FLAG);private val path=Path()
 override fun draw(f:AnimationFrame){val d=(12f+f.stretch*.92f).coerceAtMost(270f*f.size);val c=when(f.edge){Edge.LEFT->d to f.touch;Edge.RIGHT->f.width-d to f.touch;Edge.BOTTOM->f.touch to f.height-d};val r=(28f+f.progress*125f)*f.size;core.shader=RadialGradient(c.first,c.second,r*1.15f,intArrayOf(withAlpha(Color.BLACK,120),withAlpha(f.color,175),Color.TRANSPARENT),floatArrayOf(0f,.52f,1f),Shader.TileMode.CLAMP);f.canvas.drawCircle(c.first,c.second,r*1.15f,core);core.shader=null;for(arm in 0..3){path.reset();for(i in 0..44){val u=i/44f;val a=f.time*1.15+arm*PI/2+u*PI*3.8;val rr=r*(.08f+u);val x=c.first+cos(a).toFloat()*rr;val y=c.second+sin(a).toFloat()*rr*.62f;if(i==0)path.moveTo(x,y)else path.lineTo(x,y)};paint.maskFilter=BlurMaskFilter(9f*f.size,BlurMaskFilter.Blur.NORMAL);paint.color=withAlpha(f.color,(125*f.opacity).toInt());paint.strokeWidth=(18f-arm*2f)*f.size;f.canvas.drawPath(path,paint);paint.maskFilter=null}}
 private fun withAlpha(c:Int,a:Int)=Color.argb(a.coerceIn(0,255),Color.red(c),Color.green(c),Color.blue(c))
}
