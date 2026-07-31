package com.omer.akisgesture.gesture

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import com.omer.akisgesture.gesture.detector.DirectionValidator
import com.omer.akisgesture.gesture.detector.LSwipeDetector
import com.omer.akisgesture.gesture.model.GestureResult
import com.omer.akisgesture.gesture.model.SwipeDirection
import com.omer.akisgesture.gesture.model.TouchState
import com.omer.akisgesture.model.GestureType
import com.omer.akisgesture.model.TriggerMode
import com.omer.akisgesture.overlay.Edge
import kotlin.math.abs

/**
 * Clean State Machine for Edge Touch Gesture Detection.
 * Coordinates touch events and delegates specialized mechanics to [LSwipeDetector]
 * and [DirectionValidator].
 */
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
    private val lSwipeDetector = LSwipeDetector()
    private val handler = Handler(Looper.getMainLooper())

    private var holdScheduled = false
    private var holdArmed = false
    private var holdFiredOnThreshold = false

    private var lastStretch = 0f
    private var lastTouchAlongEdge = 0f
    private var lastSwitchDirection: SwipeDirection? = null
    private var wasArmed = false

    private val edgeDamping: Float get() = config.dampingFor(edge)
    private val swipeThresholdPx: Float get() = config.minSwipeThresholdPx

    private val holdRunnable = Runnable {
        holdScheduled = false
        if (state == GestureState.DETECTED &&
            lastStretch >= swipeThresholdPx &&
            hasHoldActionAt(lastTouchAlongEdge)
        ) {
            holdArmed = true
            Log.d(LOG_TAG, "hold_armed edge=$edge stretch=$lastStretch threshold=$swipeThresholdPx")
            publishProgress(active = true)

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
        state = if (triggerMode == TriggerMode.SWIPE) GestureState.AWAITING_DIRECTION else GestureState.TRACKING

        touchState.apply {
            downX = event.rawX
            downY = event.rawY
            prevX = event.rawX
            prevY = event.rawY
            downTime = System.currentTimeMillis()
        }

        lSwipeDetector.onDown()
        lastTouchAlongEdge = touchCoord(event)
        lastStretch = 0f

        onProgress(
            GestureProgress(
                edge = edge,
                stretch = 0f,
                touchAlongEdgePx = lastTouchAlongEdge,
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
                    val isValidSwipe = DirectionValidator.isValidInwardSwipe(edge, dx, dy, switchDirection != null)

                    if (isValidSwipe && edge != Edge.BOTTOM) {
                        if (!DirectionValidator.isAngleWithinTolerance(edge, dx, dy, config.directionToleranceDegrees)) {
                            state = GestureState.REJECTED
                            Log.d(LOG_TAG, "direction_rejected edge=$edge tolerance=${config.directionToleranceDegrees}")
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
            GestureState.DETECTED -> {}
            else -> {}
        }

        touchState.prevX = event.rawX
        touchState.prevY = event.rawY

        val currentInward = inwardDisplacement(dx, dy).coerceAtLeast(0f)
        val dampedDisplacement = GestureThresholds.dampedDisplacement(currentInward, edgeDamping)

        lastSwitchDirection = switchDirection
        lastStretch = if (switchDirection != null) abs(dx) else dampedDisplacement
        lastTouchAlongEdge = touchCoord(event)

        // Process 2-Phase L-Swipe detection via modular detector
        lSwipeDetector.onMove(
            event = event,
            edge = edge,
            downX = touchState.downX,
            downY = touchState.downY,
            currentInwardPx = currentInward,
            swipeThresholdPx = swipeThresholdPx,
            hasLActionAtInitialTouch = hasLActionAt(initialTouchCoord()),
        )

        if (lSwipeDetector.detectedLGesture != null) {
            state = GestureState.DETECTED
            wasArmed = true
        }

        val visuallyActive = state == GestureState.DETECTED ||
            state == GestureState.TRACKING ||
            state == GestureState.AWAITING_DIRECTION ||
            lSwipeDetector.inwardArmed

        val quickArmed = state == GestureState.DETECTED && if (switchDirection != null) {
            BottomAppSwitchPolicy.isArmed(dx, swipeThresholdPx)
        } else {
            GestureThresholds.isQuickArmed(dampedDisplacement, swipeThresholdPx)
        }
        if (quickArmed) wasArmed = true

        // Multi-tier Hysteresis logic
        if (holdArmed || holdScheduled) {
            if (dampedDisplacement < swipeThresholdPx * 0.65f) {
                cancelHold()
            }
        }

        val inwardThreshold = swipeThresholdPx.coerceAtLeast(20f)
        if (switchDirection == null &&
            (state == GestureState.DETECTED || lSwipeDetector.inwardArmed) &&
            (dampedDisplacement < swipeThresholdPx * 0.40f || currentInward < inwardThreshold * 0.5f)
        ) {
            state = GestureState.CANCELLED
            lSwipeDetector.reset()
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

        if (state == GestureState.CANCELLED) {
            finishProgress(event)
            reset()
            return
        }

        // 1. Evaluate 2-Phase L-Swipe Result First
        val detectedLGesture = lSwipeDetector.detectedLGesture
        if (detectedLGesture != null) {
            state = GestureState.EXECUTING
            val result = GestureResult.EdgeSwipe(
                edge = edge,
                section = section,
                gestureType = detectedLGesture,
                touchAlongEdgePx = initialTouchPx,
            )
            Log.d(LOG_TAG, "2_phase_L_swipe_executed edge=$edge type=$detectedLGesture section=$section")
            onGestureResult(result)
            finishProgress(event)
            reset()
            return
        }

        // Replay tap in SWIPE mode if no swipe occurred
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
        val dampedDisplacement = GestureThresholds.dampedDisplacement(rawDisplacement, edgeDamping)

        if (state == GestureState.DETECTED) {
            state = GestureState.EXECUTING
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
                Log.d(LOG_TAG, "gesture_result edge=$edge result=${result::class.simpleName} section=$section")
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
                edge = edge,
                stretch = 0f,
                touchAlongEdgePx = touchCoord(event),
                active = false,
                armed = false,
                holdArmed = false,
                appSwitchDirection = null,
            )
        )
    }

    private fun publishProgress(active: Boolean) {
        val armedNow = state == GestureState.DETECTED && lastStretch >= swipeThresholdPx
        val detectedL = lSwipeDetector.detectedLGesture
        val isLUp = detectedL == GestureType.SWIPE_UP_L
        val isLDown = detectedL == GestureType.SWIPE_DOWN_L
        val bendStartY = if (isLUp || isLDown || lSwipeDetector.inwardArmed) lSwipeDetector.bendStartY else 0f

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
                bendStartY = bendStartY,
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
        lSwipeDetector.reset()
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
    val bendStartY: Float = 0f,
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
