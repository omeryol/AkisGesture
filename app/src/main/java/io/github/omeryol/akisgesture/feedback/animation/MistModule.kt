package io.github.omeryol.akisgesture.feedback.animation
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.cos
import kotlin.math.sin
class MistModule:NaturalAnimationModule{private val paint=Paint(Paint.ANTI_ALIAS_FLAG);override fun draw(f:AnimationFrame){val d=(6f+f.stretch*.45f).coerceAtMost(145f*f.size);val c=when(f.edge){Edge.LEFT->d to f.touch;Edge.RIGHT->f.width-d to f.touch;Edge.BOTTOM->f.touch to f.height-d};val spread=(30f+f.progress*210f)*f.size;for(i in 0 until 20){val a=i*2.399963+f.time*.085;val x=c.first+cos(a).toFloat()*spread*(.12f+i/22f);val y=c.second+sin(a*.73).toFloat()*spread*.68f;val r=spread*(.25f+i%4*.045f);paint.shader=RadialGradient(x,y,r,withAlpha(lighten(f.color),(82*f.opacity).toInt()),Color.TRANSPARENT,Shader.TileMode.CLAMP);f.canvas.drawCircle(x,y,r,paint)};paint.shader=null}private fun withAlpha(c:Int,a:Int)=Color.argb(a.coerceIn(0,255),Color.red(c),Color.green(c),Color.blue(c));private fun lighten(c:Int)=Color.rgb((Color.red(c)+255)/2,(Color.green(c)+255)/2,(Color.blue(c)+255)/2)}
