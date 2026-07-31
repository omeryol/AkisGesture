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
 * Modern, fluid visual feedback renderer with mathematically smooth,
 * elegant geometries and tight shape-icon interaction.
 */
class BezierStretchRenderer {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
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
            isLUp || isLDown -> 1.4f
            holdArmed -> 1.3f
            armed -> 1.15f
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
        val maxOffset = 60f * animSize
        val inset = (stretch * 0.48f).coerceAtMost(maxOffset).coerceAtLeast(12f)
        return when (edge) {
            Edge.LEFT -> inset to touchPos
            Edge.RIGHT -> (w - inset) to touchPos
            Edge.BOTTOM -> touchPos to (h - inset)
        }
    }

    // ── 1. FLUID WAVE (Akıcı Kenar Dalgası) ──
    private fun drawFluidWave(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val span = halfSpan * (0.8f + (stretch / 320f).coerceIn(0f, 0.25f)) * animSize
        path.reset()

        val peakVal = stretch * 0.95f

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

        fillPaint.color = baseColor
        fillPaint.alpha = ((60 + progress * 75) * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawPath(path, fillPaint)

        // Accent crest line
        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 2.8f * animSize
        strokePaint.alpha = ((50 + progress * 90) * opacity * stateBoost).toInt().coerceIn(0, 220)
        canvas.drawPath(path, strokePaint)
    }

    // ── 2. NEON PULSE (Neon Halka) ──
    private fun drawNeonPulse(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val r = (25f + progress * 20f) * animSize

        auraPaint.color = baseColor
        auraPaint.alpha = (90 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, r * 1.45f, auraPaint)

        strokePaint.color = baseColor
        strokePaint.strokeWidth = (4.5f + progress * 2.5f) * animSize
        strokePaint.alpha = (235 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, r, strokePaint)

        fillPaint.color = Color.WHITE
        fillPaint.alpha = (45 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, r * 0.68f, fillPaint)
    }

    // ── 3. CYBER HEX (Siber Altıgen) ──
    private fun drawCyberHex(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val radius = (27f + progress * 17f) * animSize

        path.reset()
        for (i in 0 until 6) {
            val angle = Math.toRadians((i * 60 - 30).toDouble())
            val x = cx + (radius * cos(angle)).toFloat()
            val y = cy + (radius * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        fillPaint.color = baseColor
        fillPaint.alpha = (165 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawPath(path, fillPaint)

        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 3f * animSize
        strokePaint.alpha = (220 * opacity).toInt().coerceIn(0, 255)
        canvas.drawPath(path, strokePaint)
    }

    // ── 4. ORB GLOW (Işıklı Küre) ──
    private fun drawOrbGlow(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val rOuter = (36f + progress * 24f) * animSize
        val rInner = (22f + progress * 14f) * animSize

        auraPaint.color = baseColor
        auraPaint.alpha = (105 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, rOuter, auraPaint)

        fillPaint.color = baseColor
        fillPaint.alpha = (225 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, rInner, fillPaint)

        fillPaint.color = Color.WHITE
        fillPaint.alpha = (130 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx - rInner * 0.22f, cy - rInner * 0.22f, rInner * 0.38f, fillPaint)
    }

    // ── 5. TEARDROP (Doğal Akıcı Damla) ──
    private fun drawTeardrop(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val bulbR = (22f + progress * 16f) * animSize
        val baseSpan = (42f + progress * 24f) * animSize

        path.reset()
        when (edge) {
            Edge.LEFT -> {
                val tipX = cx + bulbR
                path.moveTo(0f, cy - baseSpan)
                path.cubicTo(tipX * 0.45f, cy - baseSpan * 0.8f, tipX + bulbR * 0.6f, cy - bulbR * 1.1f, tipX + bulbR, cy)
                path.cubicTo(tipX + bulbR * 0.6f, cy + bulbR * 1.1f, tipX * 0.45f, cy + baseSpan * 0.8f, 0f, cy + baseSpan)
            }
            Edge.RIGHT -> {
                val tipX = cx - bulbR
                path.moveTo(w, cy - baseSpan)
                path.cubicTo(w - (w - tipX) * 0.45f, cy - baseSpan * 0.8f, tipX - bulbR * 0.6f, cy - bulbR * 1.1f, tipX - bulbR, cy)
                path.cubicTo(tipX - bulbR * 0.6f, cy + bulbR * 1.1f, w - (w - tipX) * 0.45f, cy + baseSpan * 0.8f, w, cy + baseSpan)
            }
            Edge.BOTTOM -> {
                val tipY = cy - bulbR
                path.moveTo(cx - baseSpan, h)
                path.cubicTo(cx - baseSpan * 0.8f, h - (h - tipY) * 0.45f, cx - bulbR * 1.1f, tipY - bulbR * 0.6f, cx, tipY - bulbR)
                path.cubicTo(cx + bulbR * 1.1f, tipY - bulbR * 0.6f, cx + baseSpan * 0.8f, h - (h - tipY) * 0.45f, cx + baseSpan, h)
            }
        }
        path.close()

        fillPaint.color = baseColor
        fillPaint.alpha = (215 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawPath(path, fillPaint)

        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 2.5f * animSize
        strokePaint.alpha = (190 * opacity).toInt().coerceIn(0, 255)
        canvas.drawPath(path, strokePaint)
    }

    // ── 6. BUBBLE (Cam Baloncuk) ──
    private fun drawBubble(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val radius = (28f + progress * 18f) * animSize

        fillPaint.color = baseColor
        fillPaint.alpha = (205 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, radius, fillPaint)

        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 3f * animSize
        strokePaint.alpha = (200 * opacity).toInt().coerceIn(0, 255)
        rectF.set(cx - radius * 0.7f, cy - radius * 0.7f, cx + radius * 0.7f, cy + radius * 0.7f)
        canvas.drawArc(rectF, 200f, 85f, false, strokePaint)
    }

    // ── 7. MINIMAL PADDLE (Minimal Kapsül) ──
    private fun drawMinimalPaddle(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val rx = (18f + progress * 8f) * animSize
        val ry = (36f + progress * 22f) * animSize

        rectF.set(cx - rx, cy - ry, cx + rx, cy + ry)
        fillPaint.color = baseColor
        fillPaint.alpha = (225 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawRoundRect(rectF, rx, rx, fillPaint)

        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 2.5f * animSize
        strokePaint.alpha = (180 * opacity).toInt().coerceIn(0, 255)
        canvas.drawRoundRect(rectF, rx, rx, strokePaint)
    }

    // ── 8. ICON & ACTION SYMBOL INTERACTION ──
    private fun drawGestureIcon(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, alpha: Float, progress: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val timeMs = System.currentTimeMillis()

        val scale = when {
            isLUp || isLDown -> 1.38f + 0.05f * sin(timeMs / 80.0).toFloat()
            holdArmed -> 1.30f + 0.05f * sin(timeMs / 90.0).toFloat()
            armed -> 1.18f
            else -> 0.85f + (progress * 0.25f).coerceAtMost(0.25f)
        }

        canvas.save()
        canvas.scale(scale * animSize, scale * animSize, cx, cy)

        // Arming / L-swipe badge ring
        if (armed || holdArmed || isLUp || isLDown) {
            auraPaint.color = when {
                isLUp || isLDown -> lSwipeColor
                holdArmed -> secondaryColor
                else -> baseColor
            }
            auraPaint.alpha = ((if (isLUp || isLDown) 210 else if (holdArmed) 190 else 130) * opacity * alpha).toInt().coerceIn(0, 255)
            val auraRadius = if (isLUp || isLDown || holdArmed) 36f else 30f
            canvas.drawCircle(cx, cy, auraRadius, auraPaint)

            strokePaint.color = Color.WHITE
            strokePaint.strokeWidth = 3.5f
            strokePaint.alpha = (240 * opacity * alpha).toInt().coerceIn(0, 255)
            canvas.drawCircle(cx, cy, auraRadius, strokePaint)
        }

        iconPaint.alpha = (alpha * opacity * 255).toInt().coerceIn(0, 255)
        iconPaint.setShadowLayer(8f, 0f, 2f, Color.argb(160, 0, 0, 0))

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
            val popSize = if (isLUp || isLDown || holdArmed) 46f else if (armed) 42f else 36f
            iconPaint.textSize = popSize
            val baseline = cy - (iconPaint.ascent() + iconPaint.descent()) / 2f
            canvas.drawText(symbolStr, cx, baseline, iconPaint)
        } else {
            arrowPaint.alpha = (alpha * opacity * 255).toInt().coerceIn(0, 255)
            arrowPaint.strokeWidth = if (armed) 7f else 5.5f
            drawChevron(canvas, cx, cy, 26f, edge)
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
        auraPaint.alpha = (140 * opacity).toInt().coerceIn(0, 255)
        when (edge) {
            Edge.LEFT -> {
                val cy = if (touchPos > 0) touchPos else h / 2f
                rectF.set(4f, cy - 44f, 11f, cy + 44f)
                canvas.drawRoundRect(rectF, 4f, 4f, auraPaint)
            }
            Edge.RIGHT -> {
                val cy = if (touchPos > 0) touchPos else h / 2f
                rectF.set(w - 11f, cy - 44f, w - 4f, cy + 44f)
                canvas.drawRoundRect(rectF, 4f, 4f, auraPaint)
            }
            Edge.BOTTOM -> {
                val cx = if (touchPos > 0) touchPos else w / 2f
                rectF.set(cx - 54f, h - 11f, cx + 54f, h - 4f)
                canvas.drawRoundRect(rectF, 4f, 4f, auraPaint)
            }
        }
    }
}
