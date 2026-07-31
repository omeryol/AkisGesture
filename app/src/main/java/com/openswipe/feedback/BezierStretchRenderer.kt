package com.omer.akisgesture.feedback

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.omer.akisgesture.overlay.Edge
import kotlin.math.cos
import kotlin.math.sin

/**
 * Modern, fluid visual feedback renderer with rich geometric aesthetics,
 * smooth organic curves, and tight shape-icon interaction.
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
        if (showIndicatorBar) {
            drawIndicatorBar(canvas, edge, touchPosition, canvasWidth, canvasHeight)
        }

        if (stretch < 0.5f || animation == FeedbackAnimation.NONE) return

        val progress = (stretch / peak.coerceAtLeast(1f)).coerceIn(0f, 1.4f)
        val stateBoost = when {
            holdArmed -> 1.3f
            armed -> 1.15f
            else -> 1.0f
        }

        // Draw active animation shape
        when (animation) {
            FeedbackAnimation.FLUID -> drawFluidWave(canvas, edge, stretch, touchPosition, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.NEON_PULSE -> drawNeonPulse(canvas, edge, stretch, touchPosition, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.CYBER_HEX -> drawCyberHex(canvas, edge, stretch, touchPosition, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.ORB_GLOW -> drawOrbGlow(canvas, edge, stretch, touchPosition, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.TEARDROP -> drawTeardrop(canvas, edge, stretch, touchPosition, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.BUBBLE -> drawBubble(canvas, edge, stretch, touchPosition, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.MINIMAL_PADDLE -> drawMinimalPaddle(canvas, edge, stretch, touchPosition, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.ICON_ONLY, FeedbackAnimation.NONE -> Unit
        }

        // Draw icon & action symbol with tight shape interaction
        drawGestureIcon(
            canvas, edge, stretch, touchPosition, canvasWidth, canvasHeight, arrowAlpha, progress
        )
    }

    /**
     * Calculates the centroid (cx, cy) of the shape so the icon tracks the wave peak smoothly.
     */
    private fun center(edge: Edge, stretch: Float, touchPos: Float, w: Float, h: Float): Pair<Float, Float> {
        val maxOffset = 64f * animSize
        val inset = (stretch * 0.52f).coerceAtMost(maxOffset).coerceAtLeast(10f)
        return when (edge) {
            Edge.LEFT -> inset to touchPos
            Edge.RIGHT -> (w - inset) to touchPos
            Edge.BOTTOM -> touchPos to (h - inset)
        }
    }

    // ── 1. FLUID WAVE ──
    private fun drawFluidWave(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val span = halfSpan * (0.8f + (stretch / 320f).coerceIn(0f, 0.25f)) * animSize
        path.reset()

        val peakVal = stretch * 1.05f

        when (edge) {
            Edge.LEFT -> {
                path.moveTo(0f, touchPos - span)
                path.cubicTo(peakVal, touchPos - span * 0.42f, peakVal, touchPos + span * 0.42f, 0f, touchPos + span)
            }
            Edge.RIGHT -> {
                path.moveTo(w, touchPos - span)
                path.cubicTo(w - peakVal, touchPos - span * 0.42f, w - peakVal, touchPos + span * 0.42f, w, touchPos + span)
            }
            Edge.BOTTOM -> {
                path.moveTo(touchPos - span, h)
                path.cubicTo(touchPos - span * 0.42f, h - peakVal, touchPos + span * 0.42f, h - peakVal, touchPos + span, h)
            }
        }
        path.close()

        fillPaint.color = baseColor
        fillPaint.alpha = ((55 + progress * 80) * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawPath(path, fillPaint)

        // Accent glow along the inner crest
        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 3f * animSize
        strokePaint.alpha = ((40 + progress * 90) * opacity * stateBoost).toInt().coerceIn(0, 220)
        canvas.drawPath(path, strokePaint)
    }

    // ── 2. NEON PULSE ──
    private fun drawNeonPulse(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val baseRadius = (24f + progress * 22f) * animSize

        // Outer glow aura
        auraPaint.color = baseColor
        auraPaint.alpha = (90 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, baseRadius * 1.5f, auraPaint)

        // Inner glowing ring
        strokePaint.color = baseColor
        strokePaint.strokeWidth = (5f + progress * 3f) * animSize
        strokePaint.alpha = (230 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, baseRadius, strokePaint)

        // Core fill
        fillPaint.color = Color.WHITE
        fillPaint.alpha = (50 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, baseRadius * 0.7f, fillPaint)
    }

    // ── 3. CYBER HEX ──
    private fun drawCyberHex(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val radius = (28f + progress * 18f) * animSize

        path.reset()
        for (i in 0 until 6) {
            val angle = Math.toRadians((i * 60 - 30).toDouble())
            val x = cx + (radius * cos(angle)).toFloat()
            val y = cy + (radius * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        fillPaint.color = baseColor
        fillPaint.alpha = (160 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawPath(path, fillPaint)

        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 3f * animSize
        strokePaint.alpha = (220 * opacity).toInt().coerceIn(0, 255)
        canvas.drawPath(path, strokePaint)
    }

    // ── 4. ORB GLOW ──
    private fun drawOrbGlow(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val rOuter = (38f + progress * 26f) * animSize
        val rInner = (20f + progress * 14f) * animSize

        auraPaint.color = baseColor
        auraPaint.alpha = (100 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, rOuter, auraPaint)

        fillPaint.color = baseColor
        fillPaint.alpha = (220 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, rInner, fillPaint)

        // Core highlight
        fillPaint.color = Color.WHITE
        fillPaint.alpha = (120 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx - rInner * 0.25f, cy - rInner * 0.25f, rInner * 0.4f, fillPaint)
    }

    // ── 5. TEARDROP ──
    private fun drawTeardrop(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val r = (24f + progress * 16f) * animSize

        path.reset()
        when (edge) {
            Edge.LEFT -> {
                path.moveTo(0f, cy - r * 1.3f)
                path.cubicTo(cx * 1.1f, cy - r * 1.2f, cx + r * 1.4f, cy - r * 0.4f, cx + r * 1.4f, cy)
                path.cubicTo(cx + r * 1.4f, cy + r * 0.4f, cx * 1.1f, cy + r * 1.2f, 0f, cy + r * 1.3f)
            }
            Edge.RIGHT -> {
                path.moveTo(w, cy - r * 1.3f)
                path.cubicTo(cx * 0.9f, cy - r * 1.2f, cx - r * 1.4f, cy - r * 0.4f, cx - r * 1.4f, cy)
                path.cubicTo(cx - r * 1.4f, cy + r * 0.4f, cx * 0.9f, cy + r * 1.2f, w, cy + r * 1.3f)
            }
            Edge.BOTTOM -> {
                path.moveTo(cx - r * 1.3f, h)
                path.cubicTo(cx - r * 1.2f, cy * 0.9f, cx - r * 0.4f, cy - r * 1.4f, cx, cy - r * 1.4f)
                path.cubicTo(cx + r * 0.4f, cy - r * 1.4f, cx + r * 1.2f, cy * 0.9f, cx + r * 1.3f, h)
            }
        }
        path.close()

        fillPaint.color = baseColor
        fillPaint.alpha = (210 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawPath(path, fillPaint)

        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 2.5f * animSize
        strokePaint.alpha = (180 * opacity).toInt().coerceIn(0, 255)
        canvas.drawPath(path, strokePaint)
    }

    // ── 6. BUBBLE ──
    private fun drawBubble(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val radius = (30f + progress * 20f) * animSize

        fillPaint.color = baseColor
        fillPaint.alpha = (200 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, radius, fillPaint)

        // Glossy bubble arc reflection
        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 3f * animSize
        strokePaint.alpha = (190 * opacity).toInt().coerceIn(0, 255)
        rectF.set(cx - radius * 0.7f, cy - radius * 0.7f, cx + radius * 0.7f, cy + radius * 0.7f)
        canvas.drawArc(rectF, 200f, 80f, false, strokePaint)
    }

    // ── 7. MINIMAL PADDLE ──
    private fun drawMinimalPaddle(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val rx = (20f + progress * 8f) * animSize
        val ry = (38f + progress * 24f) * animSize

        rectF.set(cx - rx, cy - ry, cx + rx, cy + ry)
        fillPaint.color = baseColor
        fillPaint.alpha = (220 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawRoundRect(rectF, rx, rx, fillPaint)

        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 2.5f * animSize
        strokePaint.alpha = (170 * opacity).toInt().coerceIn(0, 255)
        canvas.drawRoundRect(rectF, rx, rx, strokePaint)
    }

    // ── 8. ICON & ACTION SYMBOL INTERACTION ──
    private fun drawGestureIcon(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, alpha: Float, progress: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val timeMs = System.currentTimeMillis()

        // Elastic scale pop based on gesture arming state
        val scale = when {
            holdArmed -> 1.32f + 0.06f * sin(timeMs / 90.0).toFloat()
            armed -> 1.18f
            else -> 0.85f + (progress * 0.25f).coerceAtMost(0.25f)
        }

        canvas.save()
        canvas.scale(scale * animSize, scale * animSize, cx, cy)

        // Arming state glow ring/badge behind icon
        if (armed || holdArmed) {
            auraPaint.color = if (holdArmed) Color.rgb(255, 215, 0) else baseColor
            auraPaint.alpha = ((if (holdArmed) 190 else 130) * opacity * alpha).toInt().coerceIn(0, 255)
            val auraRadius = if (holdArmed) 36f else 30f
            canvas.drawCircle(cx, cy, auraRadius, auraPaint)

            strokePaint.color = Color.WHITE
            strokePaint.strokeWidth = 3.5f
            strokePaint.alpha = (240 * opacity * alpha).toInt().coerceIn(0, 255)
            canvas.drawCircle(cx, cy, auraRadius, strokePaint)
        }

        iconPaint.alpha = (alpha * opacity * 255).toInt().coerceIn(0, 255)
        iconPaint.setShadowLayer(8f, 0f, 2f, Color.argb(160, 0, 0, 0))

        val symbolStr = if (actionSymbol.isNotEmpty()) actionSymbol else {
            val selectedIcon = if (holdArmed) holdIcon else quickIcon
            if (selectedIcon != FeedbackIcon.NONE && selectedIcon != FeedbackIcon.CHEVRON) selectedIcon.symbol else ""
        }

        if (symbolStr.isNotEmpty()) {
            val popSize = if (holdArmed) 46f else if (armed) 42f else 36f
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
