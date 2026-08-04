package io.github.omeryol.akisgesture.feedback

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import io.github.omeryol.akisgesture.feedback.animation.AnimationFrame
import io.github.omeryol.akisgesture.feedback.animation.AuroraModule
import io.github.omeryol.akisgesture.feedback.animation.BubbleModule
import io.github.omeryol.akisgesture.feedback.animation.DropletModule
import io.github.omeryol.akisgesture.feedback.animation.FireModule
import io.github.omeryol.akisgesture.feedback.animation.GlassRefractionModule
import io.github.omeryol.akisgesture.feedback.animation.InkModule
import io.github.omeryol.akisgesture.feedback.animation.MistModule
import io.github.omeryol.akisgesture.feedback.animation.NightModule
import io.github.omeryol.akisgesture.feedback.animation.NaturalAnimationModule
import io.github.omeryol.akisgesture.feedback.animation.PressureWaveModule
import io.github.omeryol.akisgesture.feedback.animation.RainModule
import io.github.omeryol.akisgesture.feedback.animation.StarsModule
import io.github.omeryol.akisgesture.feedback.animation.SunModule
import io.github.omeryol.akisgesture.feedback.animation.VortexModule
import io.github.omeryol.akisgesture.feedback.animation.WaterSurfaceModule
import io.github.omeryol.akisgesture.feedback.animation.WindModule
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.pow

/** Orchestrates independent animation modules and the near-edge action cue. */
class BezierStretchRenderer {
    private val water = WaterSurfaceModule()
    private val pressure = PressureWaveModule()
    private val droplet = DropletModule()
    private val vortex = VortexModule()
    private val ink = InkModule()
    private val mist = MistModule()
    private val glass = GlassRefractionModule()
    private val wind = WindModule()
    private val stars = StarsModule()
    private val fire = FireModule()
    private val sun = SunModule()
    private val night = NightModule()
    private val rain = RainModule()
    private val bubbles = BubbleModule()
    private val aurora = AuroraModule()
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val iconFill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val iconPath = Path()

    var halfSpan = 190f
    var armed = false
    var holdArmed = false
    var isLUp = false
    var isLDown = false
    var bendStartY = 0f
    var lColorProgress = 0f
    var primaryColor = Color.rgb(61, 90, 254)
    var secondaryColor = Color.rgb(255, 145, 0)
    var lSwipeColor = Color.rgb(0, 230, 118)
    var baseColor = primaryColor
    var opacity = .65f
    var animation = FeedbackAnimation.OCEAN_WAVE
    var quickIcon = FeedbackIcon.CHEVRON
    var holdIcon = FeedbackIcon.STAR
    var animSpeed = 1f
    var animSize = 1f
    var showIndicatorBar = false

    private var previousSymbol = ""
    private var displayedSymbol = ""
    private var symbolChangedAt = 0L
    var actionSymbol: String = ""
        set(value) {
            if (value != field) {
                previousSymbol = field
                displayedSymbol = value
                symbolChangedAt = System.nanoTime()
                field = value
            }
        }

    private val particleBurst = io.github.omeryol.akisgesture.feedback.animation.ParticleBurstModule()
    private var wasArmed = false

    fun draw(
        canvas: Canvas,
        edge: Edge,
        stretch: Float,
        touchPosition: Float,
        peak: Float,
        canvasWidth: Float,
        canvasHeight: Float,
        arrowAlpha: Float = 1f,
    ) {
        if (animation == FeedbackAnimation.NONE && !particleBurst.isActive) return
        
        // Fluid Non-Linear Spring Tension Curve
        val elasticStretch = if (peak > 0f) {
            (stretch / peak).coerceIn(0f, 1.4f).pow(0.82f) * peak
        } else stretch

        val progress = (elasticStretch / peak.coerceAtLeast(1f)).coerceIn(0f, 1.35f)
        val colorMix = smoothStep(.18f, 1.15f, progress)
        val preLColor = if (holdArmed) {
            blend(primaryColor, secondaryColor, colorMix)
        } else {
            primaryColor
        }
        baseColor = when {
            lColorProgress > 0f -> blend(
                preLColor,
                lSwipeColor,
                smoothStep(0f, 1f, lColorProgress),
            )
            else -> preLColor
        }

        // Trigger particle burst when gesture arms/fires
        val isNowArmed = armed || holdArmed
        if (isNowArmed && !wasArmed) {
            val burstX = when (edge) {
                Edge.LEFT -> elasticStretch
                Edge.RIGHT -> canvasWidth - elasticStretch
                Edge.BOTTOM -> touchPosition
            }
            val burstY = when (edge) {
                Edge.BOTTOM -> canvasHeight - elasticStretch
                else -> touchPosition
            }
            particleBurst.trigger(burstX, burstY, baseColor)
        }
        wasArmed = isNowArmed

        val size = animSize * when { holdArmed -> 1.12f; armed -> 1.06f; else -> 1f }
        val frame = AnimationFrame(
            canvas, edge, touchPosition, elasticStretch, progress,
            canvasWidth, canvasHeight, baseColor,
            (.46f + opacity * .54f).coerceIn(.55f, 1f), size,
            System.nanoTime() / 1_000_000_000.0 * animSpeed,
        )
        if (stretch >= 0.25f && animation != FeedbackAnimation.NONE) {
            moduleFor(animation).draw(frame)
            drawActionCue(canvas, edge, touchPosition, progress, arrowAlpha, canvasWidth, canvasHeight, size)
            if (showIndicatorBar) drawIndicator(canvas, edge, touchPosition, canvasWidth, canvasHeight)
        }

        // Draw particle burst on top
        particleBurst.draw(canvas, System.currentTimeMillis())
    }

    private fun moduleFor(style: FeedbackAnimation): NaturalAnimationModule = when (style) {
        FeedbackAnimation.OCEAN_WAVE -> water
        FeedbackAnimation.HYDRO_WIPE -> pressure
        FeedbackAnimation.MERCURY_TEARDROP, FeedbackAnimation.DEWDROP_GLASS -> droplet
        FeedbackAnimation.GLASS_RIPPLE, FeedbackAnimation.ICE_SHARDS, FeedbackAnimation.PRISM_SHATTER, FeedbackAnimation.ZIPPER_VOID, FeedbackAnimation.ICON_ONLY -> glass
        FeedbackAnimation.VORTEX -> vortex
        FeedbackAnimation.BLACK_HOLE_PULL -> night
        FeedbackAnimation.INK_FLOW, FeedbackAnimation.MATRIX_DISSOLVE -> ink
        FeedbackAnimation.ATMOSPHERIC_MIST -> mist
        FeedbackAnimation.AURORA_RIBBON -> aurora
        FeedbackAnimation.PLASMA_FIRE, FeedbackAnimation.SOLAR_FLARE, FeedbackAnimation.EMBER_BLOOM -> fire
        FeedbackAnimation.SOLAR_CORONA -> sun
        FeedbackAnimation.ELECTRIC_STORM, FeedbackAnimation.NEON_PULSE -> pressure
        FeedbackAnimation.QUANTUM_RING -> bubbles
        FeedbackAnimation.STARFIELD -> stars
        FeedbackAnimation.COMET_TAIL -> wind
        FeedbackAnimation.PRISM_FLOW -> rain
        FeedbackAnimation.NONE -> water
    }

    private fun drawActionCue(canvas: Canvas, edge: Edge, touch: Float, progress: Float, alphaValue: Float, width: Float, height: Float, size: Float) {
        // Deliberately stays near the edge while the liquid tip follows the finger.
        val cueGrowth = (progress / 1.35f).coerceIn(0f, 1f).pow(1.55f)
        val lEmphasis = smoothStep(0f, 1f, lColorProgress)
        val depth = (24f + cueGrowth * 30f).coerceAtMost(56f) * size
        val c = when (edge) { Edge.LEFT -> depth to touch; Edge.RIGHT -> width-depth to touch; Edge.BOTTOM -> touch to height-depth }
        val radius = (7f + cueGrowth * 38f + lEmphasis * 28f + if (holdArmed) 6f else 0f) * size
        val pulse = when {
            lColorProgress >= 1f -> 1.08f
            holdArmed -> 1.04f
            armed -> 1.02f
            else -> 1f
        }
        iconFill.maskFilter = BlurMaskFilter(9f*size, BlurMaskFilter.Blur.NORMAL)
        iconFill.color = withAlpha(baseColor,(115*opacity*alphaValue).toInt())
        canvas.drawCircle(c.first,c.second,radius*1.55f*pulse,iconFill)
        iconFill.maskFilter=null
        iconFill.shader=RadialGradient(c.first-radius*.3f,c.second-radius*.35f,radius*1.65f,intArrayOf(withAlpha(lighten(baseColor,.62f),(235*opacity).toInt()),withAlpha(baseColor,(220*opacity).toInt()),withAlpha(darken(baseColor,.42f),(175*opacity).toInt())),floatArrayOf(0f,.45f,1f),Shader.TileMode.CLAMP)
        canvas.drawCircle(c.first,c.second,radius*pulse,iconFill);iconFill.shader=null

        val elapsed=((System.nanoTime()-symbolChangedAt)/1_000_000_000f).coerceAtLeast(0f)
        val transition=smoothStep(0f,.20f,elapsed)
        val symbol=displayedSymbol.ifEmpty{actionSymbol}
        if(previousSymbol.isNotEmpty()&&transition<1f) drawSymbol(canvas,previousSymbol,c,radius,(1f-transition)*alphaValue)
        if(symbol.isNotEmpty()) drawSymbol(canvas,symbol,c,radius,transition*alphaValue)
        else drawFilledChevron(canvas,c,edge,radius,alphaValue)
    }

    private fun drawSymbol(canvas:Canvas,symbol:String,c:Pair<Float,Float>,radius:Float,alphaValue:Float){iconPaint.color=withAlpha(Color.WHITE,(235*opacity*alphaValue).toInt());iconPaint.textSize=radius*1.05f;canvas.drawText(symbol,c.first,c.second-(iconPaint.ascent()+iconPaint.descent())/2f,iconPaint)}
    private fun drawFilledChevron(canvas:Canvas,c:Pair<Float,Float>,edge:Edge,r:Float,a:Float){iconFill.color=withAlpha(Color.WHITE,(225*opacity*a).toInt());iconPath.reset();when(edge){Edge.LEFT->{iconPath.moveTo(c.first-r*.35f,c.second-r*.55f);iconPath.lineTo(c.first+r*.45f,c.second);iconPath.lineTo(c.first-r*.35f,c.second+r*.55f);iconPath.lineTo(c.first-r*.02f,c.second)};Edge.RIGHT->{iconPath.moveTo(c.first+r*.35f,c.second-r*.55f);iconPath.lineTo(c.first-r*.45f,c.second);iconPath.lineTo(c.first+r*.35f,c.second+r*.55f);iconPath.lineTo(c.first+r*.02f,c.second)};Edge.BOTTOM->{iconPath.moveTo(c.first-r*.55f,c.second+r*.35f);iconPath.lineTo(c.first,c.second-r*.45f);iconPath.lineTo(c.first+r*.55f,c.second+r*.35f);iconPath.lineTo(c.first,c.second+r*.02f)}};iconPath.close();canvas.drawPath(iconPath,iconFill)}
    private fun drawIndicator(canvas:Canvas,edge:Edge,touch:Float,width:Float,height:Float){iconFill.color=withAlpha(baseColor,(105*opacity).toInt());when(edge){Edge.LEFT->canvas.drawRoundRect(2f,touch-40f,7f,touch+40f,3f,3f,iconFill);Edge.RIGHT->canvas.drawRoundRect(width-7f,touch-40f,width-2f,touch+40f,3f,3f,iconFill);Edge.BOTTOM->canvas.drawRoundRect(touch-48f,height-7f,touch+48f,height-2f,3f,3f,iconFill)}}
    private fun smoothStep(a:Float,b:Float,v:Float):Float{val t=((v-a)/(b-a)).coerceIn(0f,1f);return t*t*(3f-2f*t)}
    private fun blend(a:Int,b:Int,t:Float)=Color.rgb((Color.red(a)+(Color.red(b)-Color.red(a))*t).toInt(),(Color.green(a)+(Color.green(b)-Color.green(a))*t).toInt(),(Color.blue(a)+(Color.blue(b)-Color.blue(a))*t).toInt())
    private fun lighten(c:Int,t:Float)=blend(c,Color.WHITE,t);private fun darken(c:Int,t:Float)=Color.rgb((Color.red(c)*(1-t)).toInt(),(Color.green(c)*(1-t)).toInt(),(Color.blue(c)*(1-t)).toInt());private fun withAlpha(c:Int,a:Int)=Color.argb(a.coerceIn(0,255),Color.red(c),Color.green(c),Color.blue(c))
}
