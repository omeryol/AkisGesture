package io.github.omeryol.akisgesture.feedback.animation
import android.graphics.*
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.sin
class FireModule:NaturalAnimationModule{private val p=Paint(Paint.ANTI_ALIAS_FLAG);override fun draw(f:AnimationFrame){val d=(8f+f.stretch*.78f).coerceAtMost(230f);val c=when(f.edge){Edge.LEFT->d to f.touch;Edge.RIGHT->f.width-d to f.touch;Edge.BOTTOM->f.touch to f.height-d};val h=(22f+f.progress*160f)*f.size;for(i in 0..11){val phase=((f.time*.55+i*.071)%1).toFloat();val x=c.first+sin(f.time*2+i).toFloat()*h*.22f*phase;val y=c.second-phase*h;val r=(8f+phase*22f)*f.size;p.shader=RadialGradient(x,y,r*2f,intArrayOf(alpha(lighten(f.color,.65f),(210*f.opacity).toInt()),alpha(f.color,(180*f.opacity).toInt()),alpha(darken(f.color,.5f),(80*f.opacity).toInt()),Color.TRANSPARENT),floatArrayOf(0f,.42f,.72f,1f),Shader.TileMode.CLAMP);f.canvas.drawCircle(x,y,r*2f,p)};p.shader=null}}
