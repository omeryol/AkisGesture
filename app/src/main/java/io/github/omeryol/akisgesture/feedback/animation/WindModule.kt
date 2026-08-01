package io.github.omeryol.akisgesture.feedback.animation
import android.graphics.*
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.sin
class WindModule:NaturalAnimationModule{private val p=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE;strokeCap=Paint.Cap.ROUND};private val path=Path();override fun draw(f:AnimationFrame){val len=(8f+f.progress*f.progress*275f)*f.size;for(i in 0..5){path.reset();for(s in 0..24){val u=s/24f;val along=f.touch+(i-2.5f)*18f*f.size+sin(u*6+f.time*2+i).toFloat()*22f*f.progress;val depth=u*len;val q=when(f.edge){Edge.LEFT->depth to along;Edge.RIGHT->f.width-depth to along;Edge.BOTTOM->along to f.height-depth};if(s==0)path.moveTo(q.first,q.second)else path.lineTo(q.first,q.second)};p.maskFilter=BlurMaskFilter(10f*f.size,BlurMaskFilter.Blur.NORMAL);p.color=alpha(if(i%2==0) f.color else lighten(f.color,.28f),((95+i*15)*f.opacity).toInt());p.strokeWidth=(18f+i%2*8f)*f.size;f.canvas.drawPath(path,p);p.maskFilter=null}}}
