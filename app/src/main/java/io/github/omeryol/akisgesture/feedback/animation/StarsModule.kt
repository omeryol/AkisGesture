package io.github.omeryol.akisgesture.feedback.animation
import android.graphics.*
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.cos
import kotlin.math.sin
class StarsModule:NaturalAnimationModule{private val p=Paint(Paint.ANTI_ALIAS_FLAG);override fun draw(f:AnimationFrame){val d=(12f+f.stretch*.72f).coerceAtMost(210f);val c=when(f.edge){Edge.LEFT->d to f.touch;Edge.RIGHT->f.width-d to f.touch;Edge.BOTTOM->f.touch to f.height-d};val r=(20f+f.progress*170f)*f.size;for(i in 0 until 28){val a=i*2.399963+f.time*.12;val rr=r*(.18f+(i%11)/10f);val x=c.first+cos(a).toFloat()*rr;val y=c.second+sin(a).toFloat()*rr;val s=(2f+i%4*1.4f)*f.size*(.5f+f.progress);p.color=alpha(if(i%3==0) lighten(f.color,.62f) else f.color,((100+i%4*35)*f.opacity).toInt());f.canvas.drawCircle(x,y,s,p)}}}
