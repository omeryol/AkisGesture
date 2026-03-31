package com.openswipe.gesture

import android.view.MotionEvent
import android.view.VelocityTracker
import com.openswipe.gesture.model.GestureResult
import com.openswipe.gesture.model.SwipeDirection
import com.openswipe.gesture.model.TouchState
import com.openswipe.overlay.Edge

class EdgeGestureDetector(
    private val edge: Edge,
    private val config: GestureConfig,
    private val scaledTouchSlop: Int,
    private val onGestureResult: (GestureResult) -> Unit,
) {
    private var state = GestureState.IDLE
    private val touchState = TouchState()
    private var velocityTracker: VelocityTracker? = null

    fun onTouchEvent(event: MotionEvent): Boolean {
        velocityTracker?.addMovement(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> handleDown(event)
            MotionEvent.ACTION_MOVE -> handleMove(event)
            MotionEvent.ACTION_UP -> handleUp(event)
            MotionEvent.ACTION_CANCEL -> reset()
        }
        return true
    }

    private fun handleDown(event: MotionEvent) {
        state = GestureState.TRACKING
        touchState.apply {
            downX = event.rawX
            downY = event.rawY
            prevX = event.rawX
            prevY = event.rawY
            downTime = System.currentTimeMillis()
        }
        velocityTracker = VelocityTracker.obtain()
        velocityTracker?.addMovement(event)
    }

    private fun handleMove(event: MotionEvent) {
        val dx = event.rawX - touchState.downX
        val dy = event.rawY - touchState.downY

        when (state) {
            GestureState.TRACKING -> {
                if (dx * dx + dy * dy > scaledTouchSlop * scaledTouchSlop) {
                    state = GestureState.DETECTED
                }
            }
            GestureState.DETECTED -> {
                // 手势已检测到，继续跟踪（视觉反馈在 Phase 2 中通过 Canvas 层实现）
            }
            else -> {}
        }

        touchState.prevX = event.rawX
        touchState.prevY = event.rawY
    }

    private fun handleUp(event: MotionEvent) {
        velocityTracker?.computeCurrentVelocity(1000)

        val dx = event.rawX - touchState.downX
        val dy = event.rawY - touchState.downY
        val rawDisplacement = when (edge) {
            Edge.LEFT -> dx.coerceAtLeast(0f)
            Edge.RIGHT -> (-dx).coerceAtLeast(0f)
            Edge.BOTTOM -> (-dy).coerceAtLeast(0f)
        }
        val dampedDisplacement = rawDisplacement / config.dampingFactor
        val section = resolveSection(touchCoord(event))

        if (state == GestureState.DETECTED) {
            state = GestureState.EXECUTING
            val result = resolveGestureResult(dampedDisplacement, section, dx, dy)
            onGestureResult(result)
        }

        reset()
    }

    private fun resolveGestureResult(
        displacement: Float,
        section: Int,
        rawDx: Float,
        rawDy: Float,
    ): GestureResult {
        val minThreshold = config.minSwipeThresholdPx
        val halfPeak = config.peakThreshold / 2f

        return when {
            displacement > minThreshold && displacement <= halfPeak ->
                GestureResult.EdgeSwipe(edge, section, isPrimary = true)
            displacement > halfPeak ->
                GestureResult.EdgeSwipe(edge, section, isPrimary = false)
            displacement <= minThreshold && edge != Edge.BOTTOM -> {
                when {
                    rawDy < -config.minSwipeThresholdPx ->
                        GestureResult.VerticalSwipe(edge, section, SwipeDirection.UP)
                    rawDy > config.minSwipeThresholdPx ->
                        GestureResult.VerticalSwipe(edge, section, SwipeDirection.DOWN)
                    else -> GestureResult.Tap(edge, section)
                }
            }
            else -> GestureResult.Tap(edge, section)
        }
    }

    private fun resolveSection(touchCoord: Float): Int {
        val totalLength = config.sensorLength
        if (totalLength <= 0 || config.sectionCount <= 0) return 0
        val sectionWidth = totalLength / config.sectionCount
        return (touchCoord / sectionWidth).toInt().coerceIn(0, config.sectionCount - 1)
    }

    private fun touchCoord(event: MotionEvent): Float = when (edge) {
        Edge.LEFT, Edge.RIGHT -> event.rawY
        Edge.BOTTOM -> event.rawX
    }

    private fun reset() {
        state = GestureState.IDLE
        touchState.reset()
        velocityTracker?.recycle()
        velocityTracker = null
    }
}

enum class GestureState {
    IDLE,
    TRACKING,
    DETECTED,
    EXECUTING,
}
