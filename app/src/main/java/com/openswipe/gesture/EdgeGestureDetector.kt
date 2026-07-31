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
import kotlin.math.atan2

class EdgeGestureDetector(
    private val edge: Edge,
    private val config: GestureConfig,
    private val scaledTouchSlop: Int,
    private val onGestureResult: (GestureResult) -> Unit,
    private val triggerMode: TriggerMode = TriggerMode.TOUCH,
    private val onReplayTap: ((Float, Float) -> Unit)? = null,
    private val onProgress: (GestureProgress) -> Unit = {},
    private val hasHoldActionAt: (Float) -> Boolean = { true },
    private val hasLActionAt: (Float) -> Boolean = { false },
) {
    private var state = GestureState.IDLE
    private val touchState = TouchState()
    private val handler = Handler(Looper.getMainLooper())
    private var holdScheduled = false
    private var holdArmed = false
    private var lastStretch = 0f
    private var lastTouchAlongEdge = 0f
    private var lastSwitchDirection: SwipeDirection? = null
    private var wasArmed = false
    private var holdFiredOnThreshold = false

    // Per-edge resolved values (no deprecated fields)
    private val edgeDamping: Float get() = config.dampingFor(edge)
    private val swipeThresholdPx: Float get() = config.minSwipeThresholdPx

    // ── 2-Phase L-Swipe State Tracking ──
    private var maxInwardPx = 0f
    private var inwardArmed = false
    private var bendStartY = 0f
    private var lGestureType: GestureType? = null

    private val holdRunnable = Runnable {
        holdScheduled = false
        if (state == GestureState.DETECTED &&
            lastStretch >= swipeThresholdPx &&
            hasHoldActionAt(lastTouchAlongEdge)
        ) {
            holdArmed = true
            Log.d(
                LOG_TAG,
                "hold_armed edge=$edge stretch=$lastStretch threshold=$swipeThresholdPx",
            )
            publishProgress(active = true)
            // Fire on threshold mode: immediately dispatch hold action
            if (config.holdFireMode == HoldFireMode.ON_THRESHOLD) {
                holdFiredOnThreshold = true
                val section = resolveSection(initialTouchCoord())
                val result = GestureResult.EdgeSwipe(
                    edge = edge,
                    section = section,
                    gestureType = GestureType.SWIPE_HOLD,
                    touchAlongEdgePx = initialTouchCoord(),
                )
                Log.d(LOG_TAG, "hold_fire_on_threshold edge=$edge")
                onGestureResult(result)
            }
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
        maxInwardPx = 0f
        inwardArmed = false
        bendStartY = 0f
        lGestureType = null

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
                appSwitchDirection = null,
            )
        )
    }

    private fun handleMove(event: MotionEvent) {
        val dx = event.rawX - touchState.downX
        val dy = event.rawY - touchState.downY
        val switchDirection = if (edge == Edge.BOTTOM) {
            BottomAppSwitchPolicy.direction(dx, dy, scaledTouchSlop.toFloat())
        } else {
            null
        }

        when (state) {
            GestureState.AWAITING_DIRECTION -> {
                if (dx * dx + dy * dy > scaledTouchSlop * scaledTouchSlop) {
                    val isValidSwipe = when (edge) {
                        Edge.BOTTOM -> (dy < 0 && abs(dy) > abs(dx)) ||
                            switchDirection != null
                        Edge.LEFT -> dx > 0 && abs(dx) > abs(dy)    // rightward
                        Edge.RIGHT -> dx < 0 && abs(dx) > abs(dy)   // leftward
                    }
                    // Direction accuracy check: reject swipes that deviate too much
                    if (isValidSwipe && edge != Edge.BOTTOM) {
                        val expectedAngle = when (edge) {
                            Edge.LEFT -> 0.0   // rightward = 0°
                            Edge.RIGHT -> 180.0 // leftward = 180°
                            else -> 0.0
                        }
                        val actualAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                            .let { if (it < 0) it + 360 else it }
                        var angleDiff = abs(actualAngle - expectedAngle)
                        if (angleDiff > 180) angleDiff = 360 - angleDiff
                        val effectiveTolerance = config.directionToleranceDegrees.coerceAtLeast(40f)
                        if (angleDiff > effectiveTolerance) {
                            state = GestureState.REJECTED
                            Log.d(LOG_TAG, "direction_rejected edge=$edge angleDiff=$angleDiff tolerance=${config.directionToleranceDegrees}")
                            return
                        }
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

        val currentInward = inwardDisplacement(dx, dy).coerceAtLeast(0f)
        maxInwardPx = maxOf(maxInwardPx, currentInward)

        val dampedDisplacement = GestureThresholds.dampedDisplacement(
            currentInward,
            edgeDamping,
        )
        lastSwitchDirection = switchDirection
        lastStretch = if (switchDirection != null) abs(dx) else dampedDisplacement
        lastTouchAlongEdge = touchCoord(event)

        val inwardThreshold = swipeThresholdPx.coerceAtLeast(20f)
        if (!inwardArmed && maxInwardPx >= inwardThreshold && hasLActionAt(initialTouchCoord())) {
            inwardArmed = true
            bendStartY = event.rawY
        }

        if (inwardArmed && (edge == Edge.LEFT || edge == Edge.RIGHT)) {
            val turnDyRaw = event.rawY - bendStartY
            val turnDy = abs(turnDyRaw)
            val turnDx = abs(event.rawX - touchState.downX).coerceAtLeast(1f)
            val turnThreshold = (swipeThresholdPx * 1.5f).coerceAtLeast(70f)
            if (turnDy >= turnThreshold && turnDy >= turnDx * 1.0f) {
                lGestureType = if (turnDyRaw <= 0f) GestureType.SWIPE_UP_L else GestureType.SWIPE_DOWN_L
                state = GestureState.DETECTED
                wasArmed = true
            } else if (turnDy < turnThreshold * 0.5f) {
                lGestureType = null
            }
        }

        val visuallyActive = state == GestureState.DETECTED ||
            state == GestureState.TRACKING ||
            state == GestureState.AWAITING_DIRECTION ||
            inwardArmed
        val quickArmed = state == GestureState.DETECTED && if (switchDirection != null) {
            BottomAppSwitchPolicy.isArmed(dx, swipeThresholdPx)
        } else {
            GestureThresholds.isQuickArmed(dampedDisplacement, swipeThresholdPx)
        }
        if (quickArmed) wasArmed = true

        // ── Multi-tier Hysteresis: Hold Reversion (65%) & Complete Cancel (35%) ──
        if (holdArmed || holdScheduled) {
            if (dampedDisplacement < swipeThresholdPx * 0.65f) {
                cancelHold() // Disarms hold and gracefully reverts to Quick Swipe mode
            }
        }

        if (switchDirection == null &&
            (state == GestureState.DETECTED || inwardArmed) &&
            (dampedDisplacement < swipeThresholdPx * 0.40f || currentInward < inwardThreshold * 0.5f)
        ) {
            state = GestureState.CANCELLED
            inwardArmed = false
            lGestureType = null
            cancelHold()
            publishProgress(active = false)
            return
        }
        if (switchDirection == null && quickArmed && !holdScheduled && !holdArmed) {
            holdScheduled = true
            handler.postDelayed(holdRunnable, config.holdTimeMs)
        }
        publishProgress(active = visuallyActive)
    }

    private fun handleUp(event: MotionEvent) {
        val initialTouchPx = initialTouchCoord()
        val section = resolveSection(initialTouchPx)

        // ── If gesture was cancelled, emit nothing ──
        if (state == GestureState.CANCELLED) {
            finishProgress(event)
            reset()
            return
        }

        // ── 1. Evaluate 2-Phase L-Swipe Result First ──
        val detectedLGesture = lGestureType
        if (detectedLGesture != null) {
            state = GestureState.EXECUTING
            val result = GestureResult.EdgeSwipe(
                edge = edge,
                section = section,
                gestureType = detectedLGesture,
                touchAlongEdgePx = initialTouchPx,
            )
            Log.d(
                LOG_TAG,
                "2_phase_L_swipe_executed edge=$edge type=$detectedLGesture section=$section initialTouchPx=$initialTouchPx",
            )
            onGestureResult(result)
            finishProgress(event)
            reset()
            return
        }

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
            edgeDamping,
        )

        if (state == GestureState.DETECTED) {
            state = GestureState.EXECUTING
            // If hold already fired on threshold, emit a no-op or quick result
            if (holdFiredOnThreshold) {
                val result = GestureResult.EdgeSwipe(
                    edge = edge,
                    section = section,
                    gestureType = GestureType.QUICK_SWIPE,
                    touchAlongEdgePx = initialTouchPx,
                )
                Log.d(LOG_TAG, "hold_already_fired edge=$edge — emitting quick")
                onGestureResult(result)
            } else {
                val result = resolveGestureResult(dampedDisplacement, section, dx, dy, initialTouchPx)
                Log.d(
                    LOG_TAG,
                    "gesture_result edge=$edge result=${result::class.simpleName} " +
                        "type=${(result as? GestureResult.EdgeSwipe)?.gestureType} " +
                        "stretch=$dampedDisplacement holdArmed=$holdArmed section=$section",
                )
                onGestureResult(result)
            }
        }

        finishProgress(event)
        reset()
    }

    private fun initialTouchCoord(): Float = when (edge) {
        Edge.LEFT, Edge.RIGHT -> touchState.downY
        Edge.BOTTOM -> touchState.downX
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
                appSwitchDirection = null,
            )
        )
    }

    private fun publishProgress(active: Boolean) {
        val dy = touchState.prevY - touchState.downY
        val armedNow = state == GestureState.DETECTED && lastStretch >= swipeThresholdPx
        val isLUp = armedNow && (edge == Edge.LEFT || edge == Edge.RIGHT) && dy < -45f
        val isLDown = armedNow && (edge == Edge.LEFT || edge == Edge.RIGHT) && dy > 45f
        onProgress(
            GestureProgress(
                edge = edge,
                stretch = lastStretch,
                touchAlongEdgePx = lastTouchAlongEdge,
                active = active,
                armed = armedNow,
                holdArmed = holdArmed,
                appSwitchDirection = lastSwitchDirection,
                isLUp = isLUp,
                isLDown = isLDown,
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
        val minThreshold = swipeThresholdPx

        return when {
            edge == Edge.BOTTOM &&
                abs(rawDx) > minThreshold &&
                abs(rawDx) > abs(rawDy) ->
                GestureResult.BottomHorizontalSwipe(
                    direction = if (rawDx < 0) SwipeDirection.LEFT else SwipeDirection.RIGHT,
                    touchAlongEdgePx = touchAlongEdgePx,
                )
            displacement > minThreshold -> {
                // L-swipe detection is handled by the 2-phase detector in handleUp;
                // here we only distinguish hold vs quick swipe.
                val gestureType = when {
                    holdArmed -> GestureType.SWIPE_HOLD
                    else -> GestureType.QUICK_SWIPE
                }
                GestureResult.EdgeSwipe(
                    edge = edge,
                    section = section,
                    gestureType = gestureType,
                    touchAlongEdgePx = touchAlongEdgePx,
                )
            }
            displacement <= minThreshold && edge != Edge.BOTTOM -> {
                when {
                    rawDy < -minThreshold ->
                        GestureResult.VerticalSwipe(edge, section, SwipeDirection.UP, touchAlongEdgePx = touchAlongEdgePx)
                    rawDy > minThreshold ->
                        GestureResult.VerticalSwipe(edge, section, SwipeDirection.DOWN, touchAlongEdgePx = touchAlongEdgePx)
                    else ->
                        GestureResult.Tap(edge, section, touchAlongEdgePx = touchAlongEdgePx)
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
        holdFiredOnThreshold = false
        lastStretch = 0f
        lastTouchAlongEdge = 0f
        lastSwitchDirection = null
        wasArmed = false
        maxInwardPx = 0f
        inwardArmed = false
        bendStartY = 0f
        lGestureType = null
        state = GestureState.IDLE
        touchState.reset()
    }

    companion object {
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
    val appSwitchDirection: SwipeDirection? = null,
    val isLUp: Boolean = false,
    val isLDown: Boolean = false,
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
