package io.github.omeryol.akisgesture.feedback.animation
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.cos
import kotlin.math.sin
class InkModule:NaturalAnimationModule{private val paint=Paint(Paint.ANTI_ALIAS_FLAG);override fun draw(f:AnimationFrame){val d=(8f+f.stretch*.62f).coerceAtMost(190f*f.size);val c=when(f.edge){Edge.LEFT->d to f.touch;Edge.RIGHT->f.width-d to f.touch;Edge.BOTTOM->f.touch to f.height-d};val spread=(28f+f.progress*175f)*f.size;for(i in 0 until 24){val seed=i*2.399963;val age=((f.time*(.055+i%3*.008)+i*.043)%1).toFloat();val x=c.first+cos(seed+f.time*.07).toFloat()*spread*age;val y=c.second+sin(seed).toFloat()*spread*.58f-age*spread*.34f;val r=spread*(.16f+age*.21f);paint.shader=RadialGradient(x,y,r,withAlpha(f.color,((1-age)*190*f.opacity).toInt()),withAlpha(darken(f.color),(1-age).times(65).toInt()),Shader.TileMode.CLAMP);f.canvas.drawCircle(x,y,r,paint)};paint.shader=null}private fun withAlpha(c:Int,a:Int)=Color.argb(a.coerceIn(0,255),Color.red(c),Color.green(c),Color.blue(c));private fun darken(c:Int)=Color.rgb(Color.red(c)/3,Color.green(c)/3,Color.blue(c)/3)}
