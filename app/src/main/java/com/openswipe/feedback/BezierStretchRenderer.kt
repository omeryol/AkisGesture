package com.omer.akisgesture.feedback

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.omer.akisgesture.overlay.Edge
import kotlin.math.cos
import kotlin.math.sin

/**
 * Evolved multi-layered visual renderer featuring real-time fluid metaball dynamics,
 * volumetric radial gradients, specular glass optics, and high-depth motion physics.
 */
class BezierStretchRenderer {

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        color = Color.WHITE
    }

    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.WHITE
    }

    private val path = Path()
    private val secondaryPath = Path()
    private val arrowPath = Path()
    private val rectF = RectF()

    var halfSpan: Float = 190f
    var armed: Boolean = false
    var holdArmed: Boolean = false
    var isLUp: Boolean = false
    var isLDown: Boolean = false
    var bendStartY: Float = 0f

    var primaryColor: Int = Color.rgb(61, 90, 254)
    var secondaryColor: Int = Color.rgb(255, 145, 0)
    var lSwipeColor: Int = Color.rgb(0, 230, 118)
    var baseColor: Int = Color.rgb(61, 90, 254)
    var opacity: Float = 0.65f
    var animation: FeedbackAnimation = FeedbackAnimation.FLUID
    var quickIcon: FeedbackIcon = FeedbackIcon.CHEVRON
    var holdIcon: FeedbackIcon = FeedbackIcon.STAR
    var actionSymbol: String = ""
    var animSpeed: Float = 1f
    var animSize: Float = 1f
    var showIndicatorBar: Boolean = false

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
        // Lock touch position in place at bendStartY when L-swipe is active
        val effectiveTouchPos = if ((isLUp || isLDown) && bendStartY > 0f) {
            bendStartY
        } else {
            touchPosition
        }

        if (showIndicatorBar) {
            drawIndicatorBar(canvas, edge, effectiveTouchPos, canvasWidth, canvasHeight)
        }

        if (stretch < 0.5f || animation == FeedbackAnimation.NONE) return

        // Color Space Selection: L-swipe > Secondary (Hold) > Primary (Quick Swipe)
        baseColor = when {
            isLUp || isLDown -> lSwipeColor
            holdArmed -> secondaryColor
            else -> primaryColor
        }

        val progress = (stretch / peak.coerceAtLeast(1f)).coerceIn(0f, 1.4f)
        val stateBoost = when {
            isLUp || isLDown -> 1.40f
            holdArmed -> 1.28f
            armed -> 1.15f
            else -> 1.0f
        }

        // Draw active animation shape with anchored position
        when (animation) {
            FeedbackAnimation.FLUID -> drawFluidAuroraMetaball(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.NEON_PULSE -> drawQuantumNeonAura(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.CYBER_HEX -> drawCyberPrism(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.ORB_GLOW -> drawStellarFlareOrb(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.TEARDROP -> drawHydrodynamicMercuryDrop(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.BUBBLE -> drawBubbleDisplacement(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.MINIMAL_PADDLE -> drawGlassmorphicPill(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.ICON_ONLY, FeedbackAnimation.NONE -> Unit
        }

        // Draw icon & action symbol with tight shape interaction
        drawGestureIcon(
            canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, arrowAlpha, progress
        )
    }

    private fun center(edge: Edge, stretch: Float, touchPos: Float, w: Float, h: Float): Pair<Float, Float> {
        val maxOffset = 58f * animSize
        val inset = (stretch * 0.48f).coerceAtMost(maxOffset).coerceAtLeast(12f)
        return when (edge) {
            Edge.LEFT -> inset to touchPos
            Edge.RIGHT -> (w - inset) to touchPos
            Edge.BOTTOM -> touchPos to (h - inset)
        }
    }

    // ── 1. FLUID AURORA METABALL (Aurora Sıvı Metaball) ──
    private fun drawFluidAuroraMetaball(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val span = halfSpan * (0.8f + (stretch / 300f).coerceIn(0f, 0.3f)) * animSize
        val peakVal = stretch * 0.95f

        path.reset()
        when (edge) {
            Edge.LEFT -> {
                path.moveTo(0f, touchPos - span)
                path.cubicTo(peakVal * 0.85f, touchPos - span * 0.38f, peakVal * 0.85f, touchPos + span * 0.38f, 0f, touchPos + span)
            }
            Edge.RIGHT -> {
                path.moveTo(w, touchPos - span)
                path.cubicTo(w - peakVal * 0.85f, touchPos - span * 0.38f, w - peakVal * 0.85f, touchPos + span * 0.38f, w, touchPos + span)
            }
            Edge.BOTTOM -> {
                path.moveTo(touchPos - span, h)
                path.cubicTo(touchPos - span * 0.38f, h - peakVal * 0.85f, touchPos + span * 0.38f, h - peakVal * 0.85f, touchPos + span, h)
            }
        }
        path.close()

        // Multi-stage Linear Gradient Shader for volumetric depth
        val alphaVal = ((70 + progress * 80) * opacity * stateBoost).toInt().coerceIn(0, 240)
        val endColor = Color.argb(alphaVal / 3, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
        val startColor = Color.argb(alphaVal, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))

        val shader = when (edge) {
            Edge.LEFT -> LinearGradient(0f, cy, peakVal, cy, startColor, endColor, Shader.TileMode.CLAMP)
            Edge.RIGHT -> LinearGradient(w, cy, w - peakVal, cy, startColor, endColor, Shader.TileMode.CLAMP)
            Edge.BOTTOM -> LinearGradient(cx, h, cx, h - peakVal, startColor, endColor, Shader.TileMode.CLAMP)
        }
        bodyPaint.shader = shader
        canvas.drawPath(path, bodyPaint)
        bodyPaint.shader = null

        // Specular glass contour rim
        rimPaint.color = Color.WHITE
        rimPaint.strokeWidth = 2.4f * animSize
        rimPaint.alpha = ((80 + progress * 110) * opacity * stateBoost).toInt().coerceIn(0, 235)
        canvas.drawPath(path, rimPaint)

        // Detaching liquid metaball droplet physics
        if (progress > 0.4f) {
            val dropR = (8f + progress * 10f) * animSize
            val dropDist = (cx * 1.15f)
            val dropX = when (edge) {
                Edge.LEFT -> cx + dropDist * 0.3f
                Edge.RIGHT -> cx - dropDist * 0.3f
                Edge.BOTTOM -> cx
            }
            val dropY = when (edge) {
                Edge.BOTTOM -> cy - dropDist * 0.3f
                else -> cy
            }
            bodyPaint.color = baseColor
            bodyPaint.alpha = (alphaVal * 0.9f).toInt()
            canvas.drawCircle(dropX, dropY, dropR, bodyPaint)
        }
    }

    // ── 2. HYDRODYNAMIC MERCURY DROPLET (Erimiş Cıva Damlası) ──
    private fun drawHydrodynamicMercuryDrop(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val bulbR = (22f + progress * 16f) * animSize
        val baseSpan = (42f + progress * 24f) * animSize

        path.reset()
        when (edge) {
            Edge.LEFT -> {
                val tipX = cx + bulbR * 0.85f
                path.moveTo(0f, cy - baseSpan)
                path.cubicTo(tipX * 0.5f, cy - baseSpan * 0.85f, tipX + bulbR, cy - bulbR, tipX + bulbR, cy)
                path.cubicTo(tipX + bulbR, cy + bulbR, tipX * 0.5f, cy + baseSpan * 0.85f, 0f, cy + baseSpan)
            }
            Edge.RIGHT -> {
                val tipX = cx - bulbR * 0.85f
                path.moveTo(w, cy - baseSpan)
                path.cubicTo(w - (w - tipX) * 0.5f, cy - baseSpan * 0.85f, tipX - bulbR, cy - bulbR, tipX - bulbR, cy)
                path.cubicTo(tipX - bulbR, cy + bulbR, w - (w - tipX) * 0.5f, cy + baseSpan * 0.85f, w, cy + baseSpan)
            }
            Edge.BOTTOM -> {
                val tipY = cy - bulbR * 0.85f
                path.moveTo(cx - baseSpan, h)
                path.cubicTo(cx - baseSpan * 0.85f, h - (h - tipY) * 0.5f, cx - bulbR, tipY - bulbR, cx, tipY - bulbR)
                path.cubicTo(cx + bulbR, tipY - bulbR, cx + baseSpan * 0.85f, h - (h - tipY) * 0.5f, cx + baseSpan, h)
            }
        }
        path.close()

        // Radial volumetric liquid shader
        val alphaVal = (210 * opacity * stateBoost).toInt().coerceIn(0, 255)
        val radialShader = RadialGradient(
            cx, cy, bulbR * 2.2f,
            intColorWithAlpha(baseColor, alphaVal),
            intColorWithAlpha(baseColor, (alphaVal * 0.4f).toInt()),
            Shader.TileMode.CLAMP
        )
        bodyPaint.shader = radialShader
        canvas.drawPath(path, bodyPaint)
        bodyPaint.shader = null

        // Specular sheen rim
        rimPaint.color = Color.WHITE
        rimPaint.strokeWidth = 2.4f * animSize
        rimPaint.alpha = (210 * opacity).toInt().coerceIn(0, 255)
        canvas.drawPath(path, rimPaint)

        // Internal specular light reflection lens node
        highlightPaint.color = Color.WHITE
        highlightPaint.alpha = (140 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx - bulbR * 0.28f, cy - bulbR * 0.28f, bulbR * 0.32f, highlightPaint)
    }

    // ── 3. GLASSMORPHIC FLOATING PILL (3D Buzlu Cam Kapsül) ──
    private fun drawGlassmorphicPill(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val rx = (17f + progress * 8f) * animSize
        val ry = (36f + progress * 22f) * animSize

        rectF.set(cx - rx, cy - ry, cx + rx, cy + ry)

        // Backdrop soft shadow aura
        auraPaint.color = Color.BLACK
        auraPaint.alpha = (45 * opacity).toInt().coerceIn(0, 255)
        rectF.offset(0f, 4f)
        canvas.drawRoundRect(rectF, rx, rx, auraPaint)
        rectF.offset(0f, -4f)

        // Frosted Glass body
        val alphaVal = (215 * opacity * stateBoost).toInt().coerceIn(0, 255)
        bodyPaint.color = baseColor
        bodyPaint.alpha = alphaVal
        canvas.drawRoundRect(rectF, rx, rx, bodyPaint)

        // Dual-layer specular perimeter rim
        rimPaint.color = Color.WHITE
        rimPaint.strokeWidth = 2.4f * animSize
        rimPaint.alpha = (195 * opacity).toInt().coerceIn(0, 255)
        canvas.drawRoundRect(rectF, rx, rx, rimPaint)
    }

    // ── 4. BUBBLE DISPLACEMENT (Dalgalanan Cam Baloncuk) ──
    private fun drawBubbleDisplacement(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val r = (26f + progress * 16f) * animSize

        // Expanding water ripple ring 1
        rimPaint.color = baseColor
        rimPaint.strokeWidth = 2f * animSize
        rimPaint.alpha = ((160 - progress * 90) * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, r * (1.2f + progress * 0.4f), rimPaint)

        // Central Glass Bubble
        val alphaVal = (205 * opacity * stateBoost).toInt().coerceIn(0, 255)
        bodyPaint.color = baseColor
        bodyPaint.alpha = alphaVal
        canvas.drawCircle(cx, cy, r, bodyPaint)

        // Curved specular arc reflection
        rimPaint.color = Color.WHITE
        rimPaint.strokeWidth = 2.6f * animSize
        rimPaint.alpha = (210 * opacity).toInt().coerceIn(0, 255)
        rectF.set(cx - r * 0.7f, cy - r * 0.7f, cx + r * 0.7f, cy + r * 0.7f)
        canvas.drawArc(rectF, 205f, 85f, false, rimPaint)
    }

    // ── 5. QUANTUM NEON AURA (Kuantum Neon Parıltısı) ──
    private fun drawQuantumNeonAura(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val r = (25f + progress * 18f) * animSize

        val radialShader = RadialGradient(
            cx, cy, r * 1.6f,
            intColorWithAlpha(baseColor, (140 * opacity * stateBoost).toInt().coerceIn(0, 255)),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        auraPaint.shader = radialShader
        canvas.drawCircle(cx, cy, r * 1.6f, auraPaint)
        auraPaint.shader = null

        rimPaint.color = baseColor
        rimPaint.strokeWidth = (4.5f + progress * 2f) * animSize
        rimPaint.alpha = (240 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, r, rimPaint)

        // Particle orbital node
        val angle = Math.toRadians((System.currentTimeMillis() / 4.0) % 360.0)
        val px = cx + (r * cos(angle)).toFloat()
        val py = cy + (r * sin(angle)).toFloat()
        highlightPaint.color = Color.WHITE
        highlightPaint.alpha = (230 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(px, py, 4f * animSize, highlightPaint)
    }

    // ── 6. CYBER PRISM (Siber Prizma Altıgen) ──
    private fun drawCyberPrism(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val radius = (27f + progress * 17f) * animSize

        path.reset()
        secondaryPath.reset()
        for (i in 0 until 6) {
            val angle = Math.toRadians((i * 60 - 30).toDouble())
            val x = cx + (radius * cos(angle)).toFloat()
            val y = cy + (radius * sin(angle)).toFloat()
            val xInner = cx + (radius * 0.65f * cos(angle)).toFloat()
            val yInner = cy + (radius * 0.65f * sin(angle)).toFloat()
            if (i == 0) {
                path.moveTo(x, y)
                secondaryPath.moveTo(xInner, yInner)
            } else {
                path.lineTo(x, y)
                secondaryPath.lineTo(xInner, yInner)
            }
        }
        path.close()
        secondaryPath.close()

        bodyPaint.color = baseColor
        bodyPaint.alpha = (165 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawPath(path, bodyPaint)

        rimPaint.color = Color.WHITE
        rimPaint.strokeWidth = 2.6f * animSize
        rimPaint.alpha = (220 * opacity).toInt().coerceIn(0, 255)
        canvas.drawPath(path, rimPaint)
        canvas.drawPath(secondaryPath, rimPaint)
    }

    // ── 7. STELLAR FLARE ORB (Yıldız Parlaması Küre) ──
    private fun drawStellarFlareOrb(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val rOuter = (35f + progress * 23f) * animSize
        val rInner = (21f + progress * 13f) * animSize

        val radialShader = RadialGradient(
            cx, cy, rOuter,
            intColorWithAlpha(baseColor, (160 * opacity * stateBoost).toInt().coerceIn(0, 255)),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        auraPaint.shader = radialShader
        canvas.drawCircle(cx, cy, rOuter, auraPaint)
        auraPaint.shader = null

        bodyPaint.color = baseColor
        bodyPaint.alpha = (230 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, rInner, bodyPaint)

        highlightPaint.color = Color.WHITE
        highlightPaint.alpha = (150 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx - rInner * 0.22f, cy - rInner * 0.22f, rInner * 0.38f, highlightPaint)
    }

    // ── 8. GLASS ICON INTERACTION (Cam Rozet ve Yaylanan Simge) ──
    private fun drawGestureIcon(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, alpha: Float, progress: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val timeMs = System.currentTimeMillis()

        val scale = when {
            isLUp || isLDown -> 1.34f + 0.04f * sin(timeMs / 80.0).toFloat()
            holdArmed -> 1.26f + 0.04f * sin(timeMs / 90.0).toFloat()
            armed -> 1.15f
            else -> 0.85f + (progress * 0.22f).coerceAtMost(0.22f)
        }

        canvas.save()
        canvas.scale(scale * animSize, scale * animSize, cx, cy)

        if (armed || holdArmed || isLUp || isLDown) {
            auraPaint.color = when {
                isLUp || isLDown -> lSwipeColor
                holdArmed -> secondaryColor
                else -> baseColor
            }
            auraPaint.alpha = ((if (isLUp || isLDown) 210 else if (holdArmed) 190 else 135) * opacity * alpha).toInt().coerceIn(0, 255)
            val auraRadius = if (isLUp || isLDown || holdArmed) 34f else 28f
            canvas.drawCircle(cx, cy, auraRadius, auraPaint)

            rimPaint.color = Color.WHITE
            rimPaint.strokeWidth = 3f
            rimPaint.alpha = (235 * opacity * alpha).toInt().coerceIn(0, 255)
            canvas.drawCircle(cx, cy, auraRadius, rimPaint)
        }

        iconPaint.alpha = (alpha * opacity * 255).toInt().coerceIn(0, 255)
        iconPaint.setShadowLayer(7f, 0f, 1.8f, Color.argb(150, 0, 0, 0))

        val symbolStr = when {
            actionSymbol.isNotEmpty() -> actionSymbol
            isLUp -> "▲"
            isLDown -> "▼"
            else -> {
                val selectedIcon = if (holdArmed) holdIcon else quickIcon
                if (selectedIcon != FeedbackIcon.NONE && selectedIcon != FeedbackIcon.CHEVRON) selectedIcon.symbol else ""
            }
        }

        if (symbolStr.isNotEmpty()) {
            val popSize = if (isLUp || isLDown || holdArmed) 44f else if (armed) 40f else 34f
            iconPaint.textSize = popSize
            val baseline = cy - (iconPaint.ascent() + iconPaint.descent()) / 2f
            canvas.drawText(symbolStr, cx, baseline, iconPaint)
        } else {
            arrowPaint.alpha = (alpha * opacity * 255).toInt().coerceIn(0, 255)
            arrowPaint.strokeWidth = if (armed) 6.5f else 5f
            drawChevron(canvas, cx, cy, 24f, edge)
        }

        canvas.restore()
    }

    private fun drawChevron(canvas: Canvas, cx: Float, cy: Float, size: Float, edge: Edge) {
        arrowPath.reset()
        val half = size / 2f
        when (edge) {
            Edge.LEFT -> {
                arrowPath.moveTo(cx - half * 0.35f, cy - half)
                arrowPath.lineTo(cx + half * 0.45f, cy)
                arrowPath.lineTo(cx - half * 0.35f, cy + half)
            }
            Edge.RIGHT -> {
                arrowPath.moveTo(cx + half * 0.35f, cy - half)
                arrowPath.lineTo(cx - half * 0.45f, cy)
                arrowPath.lineTo(cx + half * 0.35f, cy + half)
            }
            Edge.BOTTOM -> {
                arrowPath.moveTo(cx - half, cy + half * 0.35f)
                arrowPath.lineTo(cx, cy - half * 0.45f)
                arrowPath.lineTo(cx + half, cy + half * 0.35f)
            }
        }
        canvas.drawPath(arrowPath, arrowPaint)
    }

    private fun drawIndicatorBar(canvas: Canvas, edge: Edge, touchPos: Float, w: Float, h: Float) {
        auraPaint.color = baseColor
        auraPaint.alpha = (130 * opacity).toInt().coerceIn(0, 255)
        when (edge) {
            Edge.LEFT -> {
                val cy = if (touchPos > 0) touchPos else h / 2f
                rectF.set(4f, cy - 42f, 10f, cy + 42f)
                canvas.drawRoundRect(rectF, 3.5f, 3.5f, auraPaint)
            }
            Edge.RIGHT -> {
                val cy = if (touchPos > 0) touchPos else h / 2f
                rectF.set(w - 10f, cy - 42f, w - 4f, cy + 42f)
                canvas.drawRoundRect(rectF, 3.5f, 3.5f, auraPaint)
            }
            Edge.BOTTOM -> {
                val cx = if (touchPos > 0) touchPos else w / 2f
                rectF.set(cx - 50f, h - 10f, cx + 50f, h - 4f)
                canvas.drawRoundRect(rectF, 3.5f, 3.5f, auraPaint)
            }
        }
    }

    private fun intColorWithAlpha(color: Int, alpha: Int): Int {
        return Color.argb(alpha.coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))
    }
}
