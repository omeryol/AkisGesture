package com.omer.akisgesture.feedback

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.omer.akisgesture.overlay.Edge

/**
 * Dokunmayı engellemeyen erişilebilirlik katmanında akıcı hareket geri bildirimi.
 */
class FeedbackView(context: Context) : View(context) {

    private val renderer = BezierStretchRenderer()
    private var releaseAnimator: ValueAnimator? = null

    var edge: Edge = Edge.LEFT

    var stretchDistance: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    var touchPosition: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    var peakThreshold: Float = 30f
    var feedbackColor: Int
        get() = renderer.baseColor
        set(value) {
            renderer.baseColor = value
            invalidate()
        }
    var feedbackOpacity: Float
        get() = renderer.opacity
        set(value) {
            renderer.opacity = value.coerceIn(0.1f, 1f)
            invalidate()
        }
    var isActive: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var isArmed: Boolean = false
        set(value) {
            field = value
            renderer.armed = value
            invalidate()
        }

    var isHoldArmed: Boolean = false
        set(value) {
            field = value
            renderer.holdArmed = value
            invalidate()
        }

    private val arrowAlpha: Float
        get() = if (peakThreshold > 0f) {
            (stretchDistance / peakThreshold).coerceIn(0f, 1f)
        } else {
            0f
        }

    init {
        isClickable = false
        isFocusable = false
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

    fun updateGestureState(
        edge: Edge,
        stretch: Float,
        touchPos: Float,
        active: Boolean,
        armed: Boolean,
        holdArmed: Boolean,
    ) {
        this.edge = edge
        this.touchPosition = touchPos
        this.isArmed = armed
        this.isHoldArmed = holdArmed
        if (active) {
            releaseAnimator?.cancel()
            isActive = true
            stretchDistance = stretch
        } else {
            animateRelease()
        }
    }

    private fun animateRelease() {
        releaseAnimator?.cancel()
        if (stretchDistance < 0.5f) {
            isActive = false
            return
        }
        releaseAnimator = ValueAnimator.ofFloat(stretchDistance, 0f).apply {
            duration = 180L
            interpolator = DecelerateInterpolator(1.8f)
            addUpdateListener {
                stretchDistance = it.animatedValue as Float
                if (stretchDistance < 0.5f) isActive = false
            }
            start()
        }
    }
}
