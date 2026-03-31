package com.openswipe.gesture

import android.view.MotionEvent
import com.openswipe.gesture.model.GestureResult
import com.openswipe.gesture.model.SwipeDirection
import com.openswipe.gesture.model.TouchState
import com.openswipe.overlay.Edge
import kotlin.math.abs

class EdgeGestureDetector(
    private val edge: Edge,
    private val config: GestureConfig,
    private val scaledTouchSlop: Int,
    private val onGestureResult: (GestureResult) -> Unit,
    private val triggerMode: BottomTriggerMode = BottomTriggerMode.TOUCH,
    private val onReplayTap: ((Float, Float) -> Unit)? = null,
) {
    private var state = GestureState.IDLE
    private val touchState = TouchState()

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> handleDown(event)
            MotionEvent.ACTION_MOVE -> handleMove(event)
            MotionEvent.ACTION_UP -> handleUp(event)
            MotionEvent.ACTION_CANCEL -> reset()
        }
        return true
    }

    private fun handleDown(event: MotionEvent) {
        if (edge == Edge.BOTTOM && triggerMode == BottomTriggerMode.SWIPE) {
            state = GestureState.AWAITING_DIRECTION
        } else {
            state = GestureState.TRACKING
        }
        touchState.apply {
            downX = event.rawX
            downY = event.rawY
            prevX = event.rawX
            prevY = event.rawY
            downTime = System.currentTimeMillis()
        }
    }

    private fun handleMove(event: MotionEvent) {
        val dx = event.rawX - touchState.downX
        val dy = event.rawY - touchState.downY

        when (state) {
            GestureState.AWAITING_DIRECTION -> {
                if (dx * dx + dy * dy > scaledTouchSlop * scaledTouchSlop) {
                    if (dy < 0 && abs(dy) > abs(dx)) {
                        // Clear upward swipe — enter normal gesture detection
                        state = GestureState.DETECTED
                    } else {
                        state = GestureState.REJECTED
                    }
                }
            }
            GestureState.TRACKING -> {
                if (dx * dx + dy * dy > scaledTouchSlop * scaledTouchSlop) {
                    state = GestureState.DETECTED
                }
            }
            GestureState.DETECTED -> {
                // Gesture detected, continue tracking
            }
            else -> {}
        }

        touchState.prevX = event.rawX
        touchState.prevY = event.rawY
    }

    private fun handleUp(event: MotionEvent) {
        // In SWIPE mode: if we never detected an upward swipe, replay the tap
        if (edge == Edge.BOTTOM && triggerMode == BottomTriggerMode.SWIPE) {
            if (state == GestureState.AWAITING_DIRECTION || state == GestureState.REJECTED) {
                onReplayTap?.invoke(touchState.downX, touchState.downY)
                reset()
                return
            }
        }

        val dx = event.rawX - touchState.downX
        val dy = event.rawY - touchState.downY
        val rawDisplacement = when (edge) {
            Edge.LEFT -> dx.coerceAtLeast(0f)
            Edge.RIGHT -> (-dx).coerceAtLeast(0f)
            Edge.BOTTOM -> (-dy).coerceAtLeast(0f)
        }
        val dampedDisplacement = rawDisplacement / config.dampingFactor
        val touchAlongEdge = touchCoord(event)
        val section = resolveSection(touchAlongEdge)

        if (state == GestureState.DETECTED) {
            state = GestureState.EXECUTING
            val result = resolveGestureResult(dampedDisplacement, section, dx, dy, touchAlongEdge)
            onGestureResult(result)
        }

        reset()
    }

    private fun resolveGestureResult(
        displacement: Float,
        section: Int,
        rawDx: Float,
        rawDy: Float,
        touchAlongEdgePx: Float,
    ): GestureResult {
        val minThreshold = config.minSwipeThresholdPx
        val halfPeak = config.peakThreshold / 2f

        return when {
            displacement > minThreshold && displacement <= halfPeak ->
                GestureResult.EdgeSwipe(edge, section, isPrimary = true, touchAlongEdgePx = touchAlongEdgePx)
            displacement > halfPeak ->
                GestureResult.EdgeSwipe(edge, section, isPrimary = false, touchAlongEdgePx = touchAlongEdgePx)
            displacement <= minThreshold && edge != Edge.BOTTOM -> {
                when {
                    rawDy < -config.minSwipeThresholdPx ->
                        GestureResult.VerticalSwipe(edge, section, SwipeDirection.UP, touchAlongEdgePx = touchAlongEdgePx)
                    rawDy > config.minSwipeThresholdPx ->
                        GestureResult.VerticalSwipe(edge, section, SwipeDirection.DOWN, touchAlongEdgePx = touchAlongEdgePx)
                    else -> GestureResult.Tap(edge, section, touchAlongEdgePx = touchAlongEdgePx)
                }
            }
            else -> GestureResult.Tap(edge, section, touchAlongEdgePx = touchAlongEdgePx)
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
    }
}

enum class GestureState {
    IDLE,
    TRACKING,
    DETECTED,
    EXECUTING,
    AWAITING_DIRECTION,
    REJECTED,
}
