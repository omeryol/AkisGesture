package com.openswipe.feedback

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.openswipe.overlay.Edge

/**
 * 基于 STB 的贝塞尔曲线拉伸效果渲染器。
 *
 * 使用 Android Canvas（非 Compose Canvas），用于 WindowManager overlay 中的 FeedbackView。
 *
 * 核心公式来自 STB：两个控制点重合（P1==P2），形成对称鼓包。
 *   moveTo(0, touchY - halfSpan)
 *   cubicTo(stretch, touchY, stretch, touchY, 0, touchY + halfSpan)
 */
class BezierStretchRenderer {

    private val curvePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(80, 51, 51, 51)
    }

    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.WHITE
    }

    private val curvePath = Path()
    private val arrowPath = Path()

    /** 贝塞尔曲线高度半径（STB 固定值） */
    var halfSpan: Float = 180f

    /** 曲线填充颜色 */
    var curveColor: Int
        get() = curvePaint.color
        set(value) { curvePaint.color = value }

    /** 曲线透明度 (0-255) */
    var curveAlpha: Int
        get() = curvePaint.alpha
        set(value) { curvePaint.alpha = value }

    /** 箭头颜色 */
    var arrowColor: Int
        get() = arrowPaint.color
        set(value) { arrowPaint.color = value }

    /**
     * 绘制贝塞尔拉伸效果 + 箭头指示器。
     *
     * @param canvas 目标 Canvas
     * @param edge 拉伸方向：LEFT / RIGHT / BOTTOM
     * @param stretch 阻尼后的拉伸距离（px）
     * @param touchPosition 触摸位置（侧边为 Y 坐标，底部为 X 坐标）
     * @param peak 峰值阈值（px），超过 peak/2 显示双箭头
     * @param canvasWidth Canvas 宽度
     * @param canvasHeight Canvas 高度
     * @param arrowAlpha 箭头透明度 (0f-1f)
     */
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
        if (stretch < 0.5f) return

        drawBezierCurve(canvas, edge, stretch, touchPosition, canvasWidth, canvasHeight)

        val isPrimary = stretch <= peak / 2f
        drawArrow(canvas, edge, stretch, touchPosition, isPrimary, canvasWidth, canvasHeight, arrowAlpha)
    }

    private fun drawBezierCurve(
        canvas: Canvas,
        edge: Edge,
        stretch: Float,
        touchPos: Float,
        w: Float,
        h: Float,
    ) {
        curvePath.reset()
        when (edge) {
            Edge.LEFT -> {
                curvePath.moveTo(0f, touchPos - halfSpan)
                curvePath.cubicTo(stretch, touchPos, stretch, touchPos, 0f, touchPos + halfSpan)
            }
            Edge.RIGHT -> {
                curvePath.moveTo(w, touchPos - halfSpan)
                curvePath.cubicTo(w - stretch, touchPos, w - stretch, touchPos, w, touchPos + halfSpan)
            }
            Edge.BOTTOM -> {
                curvePath.moveTo(touchPos - halfSpan, h)
                curvePath.cubicTo(touchPos, h - stretch, touchPos, h - stretch, touchPos + halfSpan, h)
            }
        }
        curvePath.close()
        canvas.drawPath(curvePath, curvePaint)
    }

    /**
     * 箭头指示器（矢量 Path，分辨率无关）。
     *
     * STB 阈值逻辑：
     *   stretch > halfPeak → 双箭头（副动作）
     *   stretch <= halfPeak → 单箭头（主动作）
     */
    private fun drawArrow(
        canvas: Canvas,
        edge: Edge,
        stretch: Float,
        touchPos: Float,
        isPrimary: Boolean,
        w: Float,
        h: Float,
        alpha: Float,
    ) {
        val arrowSize = 24f
        // 箭头位置：stretch / 6（STB 的图标位置公式）
        val arrowPos = stretch / 6f

        val cx: Float
        val cy: Float
        when (edge) {
            Edge.LEFT -> { cx = arrowPos; cy = touchPos }
            Edge.RIGHT -> { cx = w - arrowPos; cy = touchPos }
            Edge.BOTTOM -> { cx = touchPos; cy = h - arrowPos }
        }

        arrowPaint.alpha = (alpha * 255).toInt().coerceIn(0, 255)

        drawChevron(canvas, cx, cy, arrowSize, edge)

        if (!isPrimary) {
            val offset = arrowSize * 0.6f
            val cx2: Float
            val cy2: Float
            when (edge) {
                Edge.LEFT -> { cx2 = cx + offset; cy2 = cy }
                Edge.RIGHT -> { cx2 = cx - offset; cy2 = cy }
                Edge.BOTTOM -> { cx2 = cx; cy2 = cy - offset }
            }
            drawChevron(canvas, cx2, cy2, arrowSize, edge)
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
