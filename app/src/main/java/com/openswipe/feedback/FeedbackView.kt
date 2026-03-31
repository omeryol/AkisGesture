package com.openswipe.feedback

import android.content.Context
import android.graphics.Canvas
import android.view.View
import com.openswipe.overlay.Edge

/**
 * 视觉反馈 View，用在 WindowManager overlay 窗口中。
 *
 * 接收手势状态更新，使用 BezierStretchRenderer 绘制贝塞尔曲线。
 * 这是一个传统 View（非 Compose），因为需要放在 TYPE_ACCESSIBILITY_OVERLAY 窗口中。
 */
class FeedbackView(context: Context) : View(context) {

    private val renderer = BezierStretchRenderer()

    /** 当前拉伸的边缘方向 */
    var edge: Edge = Edge.LEFT

    /** 阻尼后的拉伸距离（px） */
    var stretchDistance: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    /** 触摸位置（侧边为 Y，底部为 X） */
    var touchPosition: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    /** 峰值阈值（px），超过 peak/2 显示双箭头 */
    var peakThreshold: Float = 300f

    /** 是否正在活跃绘制 */
    var isActive: Boolean = false
        set(value) {
            field = value
            if (!value) {
                stretchDistance = 0f
            }
            invalidate()
        }

    /** 箭头透明度 (0f-1f)，随拉伸距离渐变 */
    private val arrowAlpha: Float
        get() = if (peakThreshold > 0f) {
            (stretchDistance / peakThreshold).coerceIn(0f, 1f)
        } else {
            0f
        }

    /** 曲线填充颜色 */
    var curveColor: Int
        get() = renderer.curveColor
        set(value) { renderer.curveColor = value }

    /** 曲线透明度 (0-255) */
    var curveAlpha: Int
        get() = renderer.curveAlpha
        set(value) { renderer.curveAlpha = value }

    /** 贝塞尔曲线高度半径 */
    var halfSpan: Float
        get() = renderer.halfSpan
        set(value) { renderer.halfSpan = value }

    init {
        // 不拦截触摸，此 View 仅用于绘制
        isClickable = false
        isFocusable = false
        // 硬件加速确保流畅绘制
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isActive || stretchDistance < 0.5f) return

        renderer.draw(
            canvas = canvas,
            edge = edge,
            stretch = stretchDistance,
            touchPosition = touchPosition,
            peak = peakThreshold,
            canvasWidth = width.toFloat(),
            canvasHeight = height.toFloat(),
            arrowAlpha = arrowAlpha,
        )
    }

    /**
     * 接收手势状态更新，一次性设置所有参数后触发重绘。
     */
    fun updateGestureState(
        edge: Edge,
        stretch: Float,
        touchPos: Float,
        active: Boolean,
    ) {
        this.edge = edge
        this.touchPosition = touchPos
        this.isActive = active
        // stretchDistance 的 setter 会调用 invalidate()
        this.stretchDistance = stretch
    }
}
