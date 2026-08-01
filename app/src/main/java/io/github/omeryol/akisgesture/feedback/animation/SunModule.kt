package io.github.omeryol.akisgesture.feedback.animation
import android.graphics.*
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.sin
class SunModule:NaturalAnimationModule{private val p=Paint(Paint.ANTI_ALIAS_FLAG);override fun draw(f:AnimationFrame){val d=(10f+f.stretch*.65f).coerceAtMost(190f);val c=when(f.edge){Edge.LEFT->d to f.touch;Edge.RIGHT->f.width-d to f.touch;Edge.BOTTOM->f.touch to f.height-d};val r=(10f+f.progress*95f)*f.size*(1+sin(f.time*1.8).toFloat()*.05f);p.shader=RadialGradient(c.first,c.second,r*2.3f,intArrayOf(alpha(lighten(f.color,.72f),(245*f.opacity).toInt()),alpha(f.color,(220*f.opacity).toInt()),alpha(darken(f.color,.42f),(150*f.opacity).toInt()),Color.TRANSPARENT),floatArrayOf(0f,.24f,.58f,1f),Shader.TileMode.CLAMP);f.canvas.drawCircle(c.first,c.second,r*2.3f,p);p.shader=null}}
