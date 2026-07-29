package com.omer.akisgesture.gesture

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import com.omer.akisgesture.gesture.model.GestureResult
import com.omer.akisgesture.gesture.model.SwipeDirection
import com.omer.akisgesture.gesture.model.TouchState
import com.omer.akisgesture.model.TriggerMode
import com.omer.akisgesture.model.GestureType
import com.omer.akisgesture.overlay.Edge
import kotlin.math.abs

class EdgeGestureDetector(
    private val edge: Edge,
    private val config: GestureConfig,
    private val scaledTouchSlop: Int,
    private val onGestureResult: (GestureResult) -> Unit,
    private val triggerMode: TriggerMode = TriggerMode.TOUCH,
    private val onReplayTap: ((Float, Float) -> Unit)? = null,
    private val onProgress: (GestureProgress) -> Unit = {},
    private val hasHoldActionAt: (Float) -> Boolean = { true },
) {
    private var state = GestureState.IDLE
    private val touchState = TouchState()
    private val handler = Handler(Looper.getMainLooper())
    private var holdScheduled = false
    private var holdArmed = false
    private var lastStretch = 0f
    private var lastTouchAlongEdge = 0f
    private var wasArmed = false
    private var holdExecuted = false
    private val holdRunnable = Runnable {
        holdScheduled = false
        if (state == GestureState.DETECTED &&
            lastStretch >= config.minSwipeThresholdPx &&
            hasHoldActionAt(lastTouchAlongEdge)
        ) {
            holdArmed = true
            Log.d(
                LOG_TAG,
                "hold_armed edge=$edge stretch=$lastStretch threshold=${config.minSwipeThresholdPx}",
            )
            publishProgress(active = true)
            holdExecuted = true
            onGestureResult(
                GestureResult.EdgeSwipe(
                    edge = edge,
                    section = resolveSection(lastTouchAlongEdge),
                    gestureType = GestureType.SWIPE_HOLD,
                    touchAlongEdgePx = lastTouchAlongEdge,
                ),
            )
        }
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> handleDown(event)
            MotionEvent.ACTION_MOVE -> handleMove(event)
            MotionEvent.ACTION_UP -> handleUp(event)
            MotionEvent.ACTION_CANCEL -> {
                finishProgress(event)
                reset()
            }
        }
        return true
    }

    private fun handleDown(event: MotionEvent) {
        if (triggerMode == TriggerMode.SWIPE) {
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
        lastTouchAlongEdge = touchCoord(event)
        lastStretch = 0f
        onProgress(
            GestureProgress(
                edge,
                0f,
                lastTouchAlongEdge,
                active = true,
                armed = false,
                holdArmed = false,
            )
        )
    }

    private fun handleMove(event: MotionEvent) {
        val dx = event.rawX - touchState.downX
        val dy = event.rawY - touchState.downY

        when (state) {
            GestureState.AWAITING_DIRECTION -> {
                if (dx * dx + dy * dy > scaledTouchSlop * scaledTouchSlop) {
                    val isValidSwipe = when (edge) {
                        Edge.BOTTOM -> dy < 0 && abs(dy) > abs(dx)  // upward
                        Edge.LEFT -> dx > 0 && abs(dx) > abs(dy)    // rightward
                        Edge.RIGHT -> dx < 0 && abs(dx) > abs(dy)   // leftward
                    }
                    state = if (isValidSwipe) GestureState.DETECTED else GestureState.REJECTED
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

        val dampedDisplacement = GestureThresholds.dampedDisplacement(
            inwardDisplacement(dx, dy),
            config.dampingFactor,
        )
        lastStretch = dampedDisplacement
        lastTouchAlongEdge = touchCoord(event)
        val visuallyActive = state == GestureState.DETECTED ||
            state == GestureState.TRACKING ||
            state == GestureState.AWAITING_DIRECTION
        val quickArmed = state == GestureState.DETECTED &&
            GestureThresholds.isQuickArmed(
                dampedDisplacement,
                config.minSwipeThresholdPx,
            )
        if (quickArmed) wasArmed = true
        if (state == GestureState.DETECTED &&
            GestureCancelPolicy.shouldCancel(
                wasArmed,
                dampedDisplacement,
                config.minSwipeThresholdPx,
            )
        ) {
            state = GestureState.CANCELLED
            cancelHold()
            publishProgress(active = false)
            return
        }
        if (quickArmed && !holdScheduled && !holdArmed) {
            holdScheduled = true
            handler.postDelayed(holdRunnable, config.holdTimeMs)
        } else if (!quickArmed &&
            dampedDisplacement < config.minSwipeThresholdPx * HOLD_HYSTERESIS
        ) {
            cancelHold()
        }
        publishProgress(active = visuallyActive)
    }

    private fun handleUp(event: MotionEvent) {
        // In SWIPE mode: if we never detected an upward swipe, replay the tap
        if (triggerMode == TriggerMode.SWIPE) {
            if (state == GestureState.AWAITING_DIRECTION || state == GestureState.REJECTED) {
                onReplayTap?.invoke(touchState.downX, touchState.downY)
                finishProgress(event)
                reset()
                return
            }
        }

        val dx = event.rawX - touchState.downX
        val dy = event.rawY - touchState.downY
        val rawDisplacement = inwardDisplacement(dx, dy)
        val dampedDisplacement = GestureThresholds.dampedDisplacement(
            rawDisplacement,
            config.dampingFactor,
        )
        val touchAlongEdge = touchCoord(event)
        val section = resolveSection(touchAlongEdge)

        if (state == GestureState.DETECTED) {
            state = GestureState.EXECUTING
            if (!holdExecuted) {
                val result = resolveGestureResult(dampedDisplacement, section, dx, dy, touchAlongEdge)
                Log.d(
                    LOG_TAG,
                    "gesture_result edge=$edge result=${result::class.simpleName} " +
                        "type=${(result as? GestureResult.EdgeSwipe)?.gestureType} " +
                        "stretch=$dampedDisplacement holdArmed=$holdArmed",
                )
                onGestureResult(result)
            }
        }

        finishProgress(event)
        reset()
    }

    private fun inwardDisplacement(dx: Float, dy: Float): Float = when (edge) {
        Edge.LEFT -> dx.coerceAtLeast(0f)
        Edge.RIGHT -> (-dx).coerceAtLeast(0f)
        Edge.BOTTOM -> (-dy).coerceAtLeast(0f)
    }

    private fun finishProgress(event: MotionEvent) {
        onProgress(
            GestureProgress(
                edge,
                0f,
                touchCoord(event),
                active = false,
                armed = false,
                holdArmed = false,
            )
        )
    }

    private fun publishProgress(active: Boolean) {
        onProgress(
            GestureProgress(
                edge = edge,
                stretch = lastStretch,
                touchAlongEdgePx = lastTouchAlongEdge,
                active = active,
                armed = state == GestureState.DETECTED &&
                    lastStretch >= config.minSwipeThresholdPx,
                holdArmed = holdArmed,
            )
        )
    }

    private fun cancelHold() {
        handler.removeCallbacks(holdRunnable)
        holdScheduled = false
        holdArmed = false
    }

    private fun resolveGestureResult(
        displacement: Float,
        section: Int,
        rawDx: Float,
        rawDy: Float,
        touchAlongEdgePx: Float,
    ): GestureResult {
        val minThreshold = config.minSwipeThresholdPx

        return when {
            displacement > minThreshold ->
                GestureResult.EdgeSwipe(
                    edge = edge,
                    section = section,
                    gestureType = if (holdArmed) {
                        GestureType.SWIPE_HOLD
                    } else {
                        GestureType.QUICK_SWIPE
                    },
                    touchAlongEdgePx = touchAlongEdgePx,
                )
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
        cancelHold()
        lastStretch = 0f
        lastTouchAlongEdge = 0f
        wasArmed = false
        holdExecuted = false
        state = GestureState.IDLE
        touchState.reset()
    }

    companion object {
        private const val HOLD_HYSTERESIS = 0.72f
        private const val LOG_TAG = "AkisGesture"
    }
}

data class GestureProgress(
    val edge: Edge,
    val stretch: Float,
    val touchAlongEdgePx: Float,
    val active: Boolean,
    val armed: Boolean,
    val holdArmed: Boolean,
)

enum class GestureState {
    IDLE,
    TRACKING,
    DETECTED,
    EXECUTING,
    AWAITING_DIRECTION,
    REJECTED,
    CANCELLED,
}
