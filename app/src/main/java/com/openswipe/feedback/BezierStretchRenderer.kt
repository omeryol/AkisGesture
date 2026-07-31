package com.omer.akisgesture.feedback

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import com.omer.akisgesture.overlay.Edge
import kotlin.math.cos
import kotlin.math.sin

/**
 * Modern, minimal visual feedback renderer with natural glassmorphism aesthetics,
 * fluid organic physics, and high-contrast icon interaction.
 */
class BezierStretchRenderer {

    private val glassBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val glassAuraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val glassRimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
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
            isLUp || isLDown -> 1.35f
            holdArmed -> 1.25f
            armed -> 1.12f
            else -> 1.0f
        }

        // Draw active animation shape with anchored position
        when (animation) {
            FeedbackAnimation.FLUID -> drawFluidWave(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.NEON_PULSE -> drawNeonPulse(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.CYBER_HEX -> drawCyberHex(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.ORB_GLOW -> drawOrbGlow(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.TEARDROP -> drawTeardrop(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.BUBBLE -> drawBubble(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.MINIMAL_PADDLE -> drawMinimalPaddle(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.ICON_ONLY, FeedbackAnimation.NONE -> Unit
        }

        // Draw icon & action symbol with tight shape interaction
        drawGestureIcon(
            canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, arrowAlpha, progress
        )
    }

    /**
     * Calculates the centroid (cx, cy) of the shape so the icon tracks the shape center smoothly.
     */
    private fun center(edge: Edge, stretch: Float, touchPos: Float, w: Float, h: Float): Pair<Float, Float> {
        val maxOffset = 58f * animSize
        val inset = (stretch * 0.48f).coerceAtMost(maxOffset).coerceAtLeast(12f)
        return when (edge) {
            Edge.LEFT -> inset to touchPos
            Edge.RIGHT -> (w - inset) to touchPos
            Edge.BOTTOM -> touchPos to (h - inset)
        }
    }

    // ── 1. FLUID WAVE (Sade Buzlu Cam Dalgası) ──
    private fun drawFluidWave(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val span = halfSpan * (0.8f + (stretch / 320f).coerceIn(0f, 0.25f)) * animSize
        path.reset()

        val peakVal = stretch * 0.92f

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

        // Translucent frosted glass body
        glassBodyPaint.color = baseColor
        glassBodyPaint.alpha = ((50 + progress * 70) * opacity * stateBoost).toInt().coerceIn(0, 240)
        canvas.drawPath(path, glassBodyPaint)

        // Glass highlight rim (1.8px specular stroke)
        glassRimPaint.color = Color.WHITE
        glassRimPaint.strokeWidth = 2.2f * animSize
        glassRimPaint.alpha = ((60 + progress * 100) * opacity * stateBoost).toInt().coerceIn(0, 230)
        canvas.drawPath(path, glassRimPaint)
    }

    // ── 2. NEON PULSE (Sade Neon Halka) ──
    private fun drawNeonPulse(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val r = (24f + progress * 18f) * animSize

        glassAuraPaint.color = baseColor
        glassAuraPaint.alpha = (75 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, r * 1.38f, glassAuraPaint)

        glassRimPaint.color = baseColor
        glassRimPaint.strokeWidth = (4f + progress * 2f) * animSize
        glassRimPaint.alpha = (230 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, r, glassRimPaint)

        glassBodyPaint.color = Color.WHITE
        glassBodyPaint.alpha = (40 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, r * 0.65f, glassBodyPaint)
    }

    // ── 3. CYBER HEX (Cam Altıgen Rozet) ──
    private fun drawCyberHex(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val radius = (26f + progress * 16f) * animSize

        path.reset()
        for (i in 0 until 6) {
            val angle = Math.toRadians((i * 60 - 30).toDouble())
            val x = cx + (radius * cos(angle)).toFloat()
            val y = cy + (radius * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        glassBodyPaint.color = baseColor
        glassBodyPaint.alpha = (160 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawPath(path, glassBodyPaint)

        glassRimPaint.color = Color.WHITE
        glassRimPaint.strokeWidth = 2.5f * animSize
        glassRimPaint.alpha = (210 * opacity).toInt().coerceIn(0, 255)
        canvas.drawPath(path, glassRimPaint)
    }

    // ── 4. ORB GLOW (Cam Küre) ──
    private fun drawOrbGlow(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val rOuter = (34f + progress * 22f) * animSize
        val rInner = (20f + progress * 12f) * animSize

        glassAuraPaint.color = baseColor
        glassAuraPaint.alpha = (95 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, rOuter, glassAuraPaint)

        glassBodyPaint.color = baseColor
        glassBodyPaint.alpha = (220 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, rInner, glassBodyPaint)

        glassBodyPaint.color = Color.WHITE
        glassBodyPaint.alpha = (120 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx - rInner * 0.22f, cy - rInner * 0.22f, rInner * 0.36f, glassBodyPaint)
    }

    // ── 5. TEARDROP (Doğal Cıva / Sıvı Damla) ──
    private fun drawTeardrop(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val bulbR = (20f + progress * 14f) * animSize
        val baseSpan = (38f + progress * 22f) * animSize

        path.reset()
        when (edge) {
            Edge.LEFT -> {
                val tipX = cx + bulbR * 0.8f
                path.moveTo(0f, cy - baseSpan)
                path.cubicTo(tipX * 0.5f, cy - baseSpan * 0.85f, tipX + bulbR, cy - bulbR, tipX + bulbR, cy)
                path.cubicTo(tipX + bulbR, cy + bulbR, tipX * 0.5f, cy + baseSpan * 0.85f, 0f, cy + baseSpan)
            }
            Edge.RIGHT -> {
                val tipX = cx - bulbR * 0.8f
                path.moveTo(w, cy - baseSpan)
                path.cubicTo(w - (w - tipX) * 0.5f, cy - baseSpan * 0.85f, tipX - bulbR, cy - bulbR, tipX - bulbR, cy)
                path.cubicTo(tipX - bulbR, cy + bulbR, w - (w - tipX) * 0.5f, cy + baseSpan * 0.85f, w, cy + baseSpan)
            }
            Edge.BOTTOM -> {
                val tipY = cy - bulbR * 0.8f
                path.moveTo(cx - baseSpan, h)
                path.cubicTo(cx - baseSpan * 0.85f, h - (h - tipY) * 0.5f, cx - bulbR, tipY - bulbR, cx, tipY - bulbR)
                path.cubicTo(cx + bulbR, tipY - bulbR, cx + baseSpan * 0.85f, h - (h - tipY) * 0.5f, cx + baseSpan, h)
            }
        }
        path.close()

        // Translucent liquid glass body
        glassBodyPaint.color = baseColor
        glassBodyPaint.alpha = (200 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawPath(path, glassBodyPaint)

        // Glass highlight specular contour
        glassRimPaint.color = Color.WHITE
        glassRimPaint.strokeWidth = 2.2f * animSize
        glassRimPaint.alpha = (185 * opacity).toInt().coerceIn(0, 255)
        canvas.drawPath(path, glassRimPaint)
    }

    // ── 6. BUBBLE (Buzlu Cam Baloncuk) ──
    private fun drawBubble(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val radius = (26f + progress * 16f) * animSize

        glassBodyPaint.color = baseColor
        glassBodyPaint.alpha = (195 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, radius, glassBodyPaint)

        // Glass arc highlight reflection
        glassRimPaint.color = Color.WHITE
        glassRimPaint.strokeWidth = 2.5f * animSize
        glassRimPaint.alpha = (195 * opacity).toInt().coerceIn(0, 255)
        rectF.set(cx - radius * 0.68f, cy - radius * 0.68f, cx + radius * 0.68f, cy + radius * 0.68f)
        canvas.drawArc(rectF, 200f, 85f, false, glassRimPaint)
    }

    // ── 7. MINIMAL PADDLE (Buzlu Cam Kapsül - iOS Pill) ──
    private fun drawMinimalPaddle(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val rx = (16f + progress * 7f) * animSize
        val ry = (34f + progress * 20f) * animSize

        rectF.set(cx - rx, cy - ry, cx + rx, cy + ry)
        glassBodyPaint.color = baseColor
        glassBodyPaint.alpha = (215 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawRoundRect(rectF, rx, rx, glassBodyPaint)

        glassRimPaint.color = Color.WHITE
        glassRimPaint.strokeWidth = 2.2f * animSize
        glassRimPaint.alpha = (175 * opacity).toInt().coerceIn(0, 255)
        canvas.drawRoundRect(rectF, rx, rx, glassRimPaint)
    }

    // ── 8. GLASS ICON INTERACTION (Buzlu Cam Rozet ve Simge) ──
    private fun drawGestureIcon(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, alpha: Float, progress: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val timeMs = System.currentTimeMillis()

        val scale = when {
            isLUp || isLDown -> 1.30f + 0.04f * sin(timeMs / 80.0).toFloat()
            holdArmed -> 1.24f + 0.04f * sin(timeMs / 90.0).toFloat()
            armed -> 1.14f
            else -> 0.85f + (progress * 0.22f).coerceAtMost(0.22f)
        }

        canvas.save()
        canvas.scale(scale * animSize, scale * animSize, cx, cy)

        // Frosted glass badge ring when gesture is armed or L-swipe locked
        if (armed || holdArmed || isLUp || isLDown) {
            glassAuraPaint.color = when {
                isLUp || isLDown -> lSwipeColor
                holdArmed -> secondaryColor
                else -> baseColor
            }
            glassAuraPaint.alpha = ((if (isLUp || isLDown) 200 else if (holdArmed) 180 else 125) * opacity * alpha).toInt().coerceIn(0, 255)
            val auraRadius = if (isLUp || isLDown || holdArmed) 34f else 28f
            canvas.drawCircle(cx, cy, auraRadius, glassAuraPaint)

            glassRimPaint.color = Color.WHITE
            glassRimPaint.strokeWidth = 3f
            glassRimPaint.alpha = (230 * opacity * alpha).toInt().coerceIn(0, 255)
            canvas.drawCircle(cx, cy, auraRadius, glassRimPaint)
        }

        iconPaint.alpha = (alpha * opacity * 255).toInt().coerceIn(0, 255)
        iconPaint.setShadowLayer(6f, 0f, 1.5f, Color.argb(140, 0, 0, 0))

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
        glassAuraPaint.color = baseColor
        glassAuraPaint.alpha = (130 * opacity).toInt().coerceIn(0, 255)
        when (edge) {
            Edge.LEFT -> {
                val cy = if (touchPos > 0) touchPos else h / 2f
                rectF.set(4f, cy - 42f, 10f, cy + 42f)
                canvas.drawRoundRect(rectF, 3.5f, 3.5f, glassAuraPaint)
            }
            Edge.RIGHT -> {
                val cy = if (touchPos > 0) touchPos else h / 2f
                rectF.set(w - 10f, cy - 42f, w - 4f, cy + 42f)
                canvas.drawRoundRect(rectF, 3.5f, 3.5f, glassAuraPaint)
            }
            Edge.BOTTOM -> {
                val cx = if (touchPos > 0) touchPos else w / 2f
                rectF.set(cx - 50f, h - 10f, cx + 50f, h - 4f)
                canvas.drawRoundRect(rectF, 3.5f, 3.5f, glassAuraPaint)
            }
        }
    }
}
