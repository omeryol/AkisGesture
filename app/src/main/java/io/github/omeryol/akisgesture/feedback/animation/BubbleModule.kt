package io.github.omeryol.akisgesture.feedback.animation
import android.graphics.*
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.sin
class BubbleModule:NaturalAnimationModule{private val p=Paint(Paint.ANTI_ALIAS_FLAG);override fun draw(f:AnimationFrame){val d=(8f+f.stretch*.7f).coerceAtMost(210f);val c=when(f.edge){Edge.LEFT->d to f.touch;Edge.RIGHT->f.width-d to f.touch;Edge.BOTTOM->f.touch to f.height-d};val spread=(22f+f.progress*145f)*f.size;for(i in 0 until 14){val age=((f.time*(.18+i%4*.015)+i*.071)%1).toFloat();val x=c.first+sin(i*2.7+f.time).toFloat()*spread*.55f;val y=c.second+spread*.6f-age*spread*1.3f;val r=(5f+(i%5)*3f+age*8f)*f.size;p.shader=RadialGradient(x-r*.3f,y-r*.35f,r*1.4f,intArrayOf(alpha(lighten(f.color,.7f),(175*f.opacity).toInt()),alpha(f.color,(75*f.opacity).toInt()),Color.TRANSPARENT),floatArrayOf(0f,.62f,1f),Shader.TileMode.CLAMP);f.canvas.drawCircle(x,y,r*1.4f,p)};p.shader=null}}
