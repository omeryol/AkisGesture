package com.omer.akisgesture.feedback

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import com.omer.akisgesture.overlay.Edge

/**
 * Parmağın konumunu izleyen yumuşak kenar dalgası ve yön işareti.
 */
class BezierStretchRenderer {

    private val curvePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.WHITE
    }
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 44f
        typeface = Typeface.DEFAULT_BOLD
        color = Color.WHITE
    }
    private val curvePath = Path()
    private val arrowPath = Path()

    var halfSpan: Float = 180f
    var armed: Boolean = false
    var holdArmed: Boolean = false
    var baseColor: Int = Color.rgb(61, 90, 254)
    var opacity: Float = 0.57f
    var animation: FeedbackAnimation = FeedbackAnimation.FLUID
    var quickIcon: FeedbackIcon = FeedbackIcon.CHEVRON
    var holdIcon: FeedbackIcon = FeedbackIcon.STAR

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
        if (stretch < 0.5f || animation == FeedbackAnimation.NONE) return

        val progress = (stretch / peak.coerceAtLeast(1f)).coerceIn(0f, 1.35f)
        curvePaint.color = baseColor
        val stateBoost = when {
            holdArmed -> 1.2f
            armed -> 1.1f
            else -> 1f
        }
        curvePaint.alpha =
            ((50 + progress * 75) * opacity * stateBoost).toInt().coerceIn(0, 255)
        when (animation) {
            FeedbackAnimation.FLUID -> {
                drawCurve(canvas, edge, stretch, touchPosition, canvasWidth, canvasHeight)
                drawGlow(canvas, edge, stretch, touchPosition, canvasWidth, canvasHeight, progress)
            }
            FeedbackAnimation.BUBBLE ->
                drawBubble(canvas, edge, stretch, touchPosition, canvasWidth, canvasHeight, progress)
            FeedbackAnimation.TEARDROP ->
                drawTeardrop(canvas, edge, stretch, touchPosition, canvasWidth, canvasHeight, progress)
            FeedbackAnimation.ICON_ONLY, FeedbackAnimation.NONE -> Unit
        }
        drawGestureIcon(
            canvas,
            edge,
            stretch,
            touchPosition,
            canvasWidth,
            canvasHeight,
            arrowAlpha,
        )
    }

    private fun center(
        edge: Edge,
        stretch: Float,
        touchPos: Float,
        w: Float,
        h: Float,
    ): Pair<Float, Float> {
        val inset = (stretch * 0.72f).coerceAtLeast(8f)
        return when (edge) {
            Edge.LEFT -> inset to touchPos
            Edge.RIGHT -> (w - inset) to touchPos
            Edge.BOTTOM -> touchPos to (h - inset)
        }
    }

    private fun drawBubble(
        canvas: Canvas,
        edge: Edge,
        stretch: Float,
        touchPos: Float,
        w: Float,
        h: Float,
        progress: Float,
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        glowPaint.color = baseColor
        glowPaint.alpha = (210 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, 28f + progress * 18f, glowPaint)
    }

    private fun drawTeardrop(
        canvas: Canvas,
        edge: Edge,
        stretch: Float,
        touchPos: Float,
        w: Float,
        h: Float,
        progress: Float,
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val radius = 25f + progress * 13f
        curvePath.reset()
        when (edge) {
            Edge.LEFT -> {
                curvePath.moveTo(0f, cy)
                curvePath.quadTo(cx, cy - radius * 1.5f, cx + radius, cy)
                curvePath.quadTo(cx, cy + radius * 1.5f, 0f, cy)
            }
            Edge.RIGHT -> {
                curvePath.moveTo(w, cy)
                curvePath.quadTo(cx, cy - radius * 1.5f, cx - radius, cy)
                curvePath.quadTo(cx, cy + radius * 1.5f, w, cy)
            }
            Edge.BOTTOM -> {
                curvePath.moveTo(cx, h)
                curvePath.quadTo(cx - radius * 1.5f, cy, cx, cy - radius)
                curvePath.quadTo(cx + radius * 1.5f, cy, cx, h)
            }
        }
        curvePath.close()
        curvePaint.color = baseColor
        curvePaint.alpha = (210 * opacity).toInt().coerceIn(0, 255)
        canvas.drawPath(curvePath, curvePaint)
    }

    private fun drawCurve(
        canvas: Canvas,
        edge: Edge,
        stretch: Float,
        touchPos: Float,
        w: Float,
        h: Float,
    ) {
        val span = halfSpan * (0.78f + (stretch / 300f).coerceIn(0f, 0.22f))
        curvePath.reset()
        when (edge) {
            Edge.LEFT -> {
                curvePath.moveTo(0f, touchPos - span)
                curvePath.cubicTo(stretch, touchPos, stretch, touchPos, 0f, touchPos + span)
            }
            Edge.RIGHT -> {
                curvePath.moveTo(w, touchPos - span)
                curvePath.cubicTo(w - stretch, touchPos, w - stretch, touchPos, w, touchPos + span)
            }
            Edge.BOTTOM -> {
                curvePath.moveTo(touchPos - span, h)
                curvePath.cubicTo(touchPos, h - stretch, touchPos, h - stretch, touchPos + span, h)
            }
        }
        curvePath.close()
        canvas.drawPath(curvePath, curvePaint)
    }

    private fun drawGlow(
        canvas: Canvas,
        edge: Edge,
        stretch: Float,
        touchPos: Float,
        w: Float,
        h: Float,
        progress: Float,
    ) {
        val inset = (stretch * 0.72f).coerceAtLeast(6f)
        val cx = when (edge) {
            Edge.LEFT -> inset
            Edge.RIGHT -> w - inset
            Edge.BOTTOM -> touchPos
        }
        val cy = when (edge) {
            Edge.LEFT, Edge.RIGHT -> touchPos
            Edge.BOTTOM -> h - inset
        }
        glowPaint.color = baseColor
        glowPaint.alpha = ((if (holdArmed) 130 else if (armed) 105 else 72) * opacity)
            .toInt()
            .coerceIn(0, 255)
        canvas.drawCircle(cx, cy, 16f + progress * 11f, glowPaint)
    }

    private fun drawGestureIcon(
        canvas: Canvas,
        edge: Edge,
        stretch: Float,
        touchPos: Float,
        w: Float,
        h: Float,
        alpha: Float,
    ) {
        val arrowSize = 25f
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val selectedIcon = if (holdArmed) holdIcon else quickIcon
        if (selectedIcon == FeedbackIcon.NONE) return
        arrowPaint.alpha = (alpha * 255).toInt().coerceIn(0, 255)
        arrowPaint.strokeWidth = if (armed) 7f else 6f
        if (selectedIcon == FeedbackIcon.CHEVRON) {
            drawChevron(canvas, cx, cy, arrowSize, edge)
        } else {
            iconPaint.alpha = (alpha * 255).toInt().coerceIn(0, 255)
            iconPaint.textSize = if (holdArmed) 48f else 42f
            val baseline = cy - (iconPaint.ascent() + iconPaint.descent()) / 2f
            canvas.drawText(selectedIcon.symbol, cx, baseline, iconPaint)
        }
    }

    private fun drawChevron(canvas: Canvas, cx: Float, cy: Float, size: Float, edge: Edge) {
        arrowPath.reset()
        val half = size / 2f
        when (edge) {
            Edge.LEFT -> {
                arrowPath.moveTo(cx - half * 0.3f, cy - half)
                arrowPath.lineTo(cx + half * 0.3f, cy)
                arrowPath.lineTo(cx - half * 0.3f, cy + half)
            }
            Edge.RIGHT -> {
                arrowPath.moveTo(cx + half * 0.3f, cy - half)
                arrowPath.lineTo(cx - half * 0.3f, cy)
                arrowPath.lineTo(cx + half * 0.3f, cy + half)
            }
            Edge.BOTTOM -> {
                arrowPath.moveTo(cx - half, cy + half * 0.3f)
                arrowPath.lineTo(cx, cy - half * 0.3f)
                arrowPath.lineTo(cx + half, cy + half * 0.3f)
            }
        }
        canvas.drawPath(arrowPath, arrowPaint)
    }
}
