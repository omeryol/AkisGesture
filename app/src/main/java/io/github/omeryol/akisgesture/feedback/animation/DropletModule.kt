package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.PI
import kotlin.math.sin

class DropletModule : NaturalAnimationModule {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG); private val path=Path()
    override fun draw(f:AnimationFrame){
        val depth=(10f+f.stretch*(1.16f+f.surfaceTension*.22f)).coerceAtMost(340f*f.size); val c=when(f.edge){Edge.LEFT->depth to f.touch;Edge.RIGHT->f.width-depth to f.touch;Edge.BOTTOM->f.touch to f.height-depth}
        val r=(12f+f.progress*(58f+f.surfaceTension*18f))*f.size; val neck=(r*(1.22f-f.progress*(.62f+f.surfaceTension*.10f))).coerceAtLeast(8f*f.size); val wobble=sin(f.time*PI*(1.5+f.damping*1.4)).toFloat()*r*(.035f+f.viscosity*.035f)
        path.reset()
        val detached = f.progress >= 1.02f
        when(f.edge){
            Edge.LEFT->if(detached) path.addCircle(c.first,c.second,r,Path.Direction.CW) else {path.moveTo(0f,f.touch-neck);path.cubicTo(depth*.36f,f.touch-neck,c.first-r*.88f,c.second-r+wobble,c.first,c.second-r);path.cubicTo(c.first+r,c.second-r,c.first+r,c.second+r,c.first,c.second+r);path.cubicTo(c.first-r*.88f,c.second+r,depth*.36f,f.touch+neck,0f,f.touch+neck)}
            Edge.RIGHT->if(detached) path.addCircle(c.first,c.second,r,Path.Direction.CW) else {path.moveTo(f.width,f.touch-neck);path.cubicTo(f.width-depth*.36f,f.touch-neck,c.first+r*.88f,c.second-r+wobble,c.first,c.second-r);path.cubicTo(c.first-r,c.second-r,c.first-r,c.second+r,c.first,c.second+r);path.cubicTo(c.first+r*.88f,c.second+r,f.width-depth*.36f,f.touch+neck,f.width,f.touch+neck)}
            Edge.BOTTOM->if(detached) path.addCircle(c.first,c.second,r,Path.Direction.CW) else {path.moveTo(f.touch-neck,f.height);path.cubicTo(f.touch-neck,f.height-depth*.36f,c.first-r,c.second+r*.88f,c.first-r,c.second);path.cubicTo(c.first-r,c.second-r,c.first+r,c.second-r,c.first+r,c.second);path.cubicTo(c.first+r,c.second+r*.88f,f.touch+neck,f.height-depth*.36f,f.touch+neck,f.height)}
        };path.close()
        paint.shader=RadialGradient(c.first-r*.3f,c.second-r*.35f,r*2.2f,intArrayOf(withAlpha(lighten(f.color,.62f),(245*f.opacity).toInt()),withAlpha(f.color,(225*f.opacity).toInt()),withAlpha(darken(f.color,.45f),(145*f.opacity).toInt())),floatArrayOf(0f,.38f,1f),Shader.TileMode.CLAMP);f.canvas.drawPath(path,paint);paint.shader=null
    }
    private fun withAlpha(c:Int,a:Int)=Color.argb(a.coerceIn(0,255),Color.red(c),Color.green(c),Color.blue(c));private fun lighten(c:Int,t:Float)=Color.rgb((Color.red(c)+(255-Color.red(c))*t).toInt(),(Color.green(c)+(255-Color.green(c))*t).toInt(),(Color.blue(c)+(255-Color.blue(c))*t).toInt());private fun darken(c:Int,t:Float)=Color.rgb((Color.red(c)*(1-t)).toInt(),(Color.green(c)*(1-t)).toInt(),(Color.blue(c)*(1-t)).toInt())
}
