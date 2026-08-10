package io.github.omeryol.akisgesture.gesture

import android.os.Handler
import android.os.Looper
import io.github.omeryol.akisgesture.diagnostics.RuntimeDiagnostics
import android.view.MotionEvent
import io.github.omeryol.akisgesture.gesture.detector.DirectionValidator
import io.github.omeryol.akisgesture.gesture.detector.LSwipeDetector
import io.github.omeryol.akisgesture.gesture.model.GestureResult
import io.github.omeryol.akisgesture.gesture.model.SwipeDirection
import io.github.omeryol.akisgesture.gesture.model.TouchState
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.model.TriggerMode
import io.github.omeryol.akisgesture.overlay.Edge
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
    private val swipeThresholdPx: Float,
    private val lSwipeThresholdPx: Float,
    private val onGestureResult: (GestureResult) -> Unit,
    private val triggerMode: TriggerMode = TriggerMode.TOUCH,
    private val onReplayTap: ((Float, Float) -> Unit)? = null,
    private val onProgress: (GestureProgress) -> Unit = {},
    private val hasHoldActionAt: (Float) -> Boolean = { true },
    private val hasLActionAt: (Float) -> Boolean = { false },
    private val hasRingActions: () -> Boolean = { false },
    private val onRingActionSelected: (Int) -> Unit = {},
    private val ringHitTest: (Float, Float, Float) -> Int = { _, _, _ -> -1 },
    private val ringHoverTest: (Float, Float, Float) -> Int = { _, _, _ -> -1 },
) {
    private var state = GestureState.IDLE
    private val touchState = TouchState()
    private val lSwipeDetector = LSwipeDetector()
    private val handler = Handler(Looper.getMainLooper())

    private var holdScheduled = false
    private var holdArmed = false
    private var holdFiredOnThreshold = false
    private var ringActive = false
    private var ringSelectedIndex = -1
    private var ringHitIndex = -1
    private var ringOpenedStretch = 0f
    private var ringAnchorTouch = 0f
    private var lastLPreviewGesture: GestureType? = null

    private var lastStretch = 0f
    private var lastTouchAlongEdge = 0f
    private var lastSwitchDirection: SwipeDirection? = null
    private var wasArmed = false
    private var maxDampedDisplacement = 0f

    private val edgeDamping: Float get() = config.dampingFor(edge)

    private val holdRunnable = Runnable {
        holdScheduled = false
        if (state == GestureState.DETECTED &&
            lastStretch >= swipeThresholdPx &&
            (hasHoldActionAt(lastTouchAlongEdge) || hasRingActions())
        ) {
            holdArmed = true
            RuntimeDiagnostics.gestureSignal(edge.name, "hold_armed")
            if (hasRingActions()) {
                ringActive = true
                ringSelectedIndex = -1
                ringHitIndex = -1
                ringOpenedStretch = lastStretch
                ringAnchorTouch = lastTouchAlongEdge
                RuntimeDiagnostics.ringOpened(edge.name)
            }
            publishProgress(active = true)

            if (config.holdFireMode == HoldFireMode.ON_THRESHOLD &&
                !hasLActionAt(initialTouchCoord()) && !ringActive
            ) {
                holdFiredOnThreshold = true
                val section = resolveSection(initialTouchCoord())
                val result = GestureResult.EdgeSwipe(
                    edge = edge,
                    section = section,
                    gestureType = GestureType.SWIPE_HOLD,
                    touchAlongEdgePx = initialTouchCoord(),
                )
                RuntimeDiagnostics.gestureSignal(edge.name, "hold_fired_on_threshold")
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
                touchAlongEdgePx = if (ringActive) ringAnchorTouch else lastTouchAlongEdge,
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
                            RuntimeDiagnostics.gestureSignal(edge.name, "direction_rejected")
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
        maxDampedDisplacement = maxOf(maxDampedDisplacement, dampedDisplacement)

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
            lSwipeThresholdPx = lSwipeThresholdPx,
            hasLActionAtInitialTouch = hasLActionAt(initialTouchCoord()),
        )
        if (lSwipeDetector.previewDirection != lastLPreviewGesture) {
            val signal = when (lSwipeDetector.previewDirection) {
                GestureType.SWIPE_UP_L -> "l_guide_up"
                GestureType.SWIPE_DOWN_L -> "l_guide_down"
                null -> "l_guide_cleared"
                else -> "l_guide_cleared"
            }
            RuntimeDiagnostics.gestureSignal(edge.name, signal)
            lastLPreviewGesture = lSwipeDetector.previewDirection
        }

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
        if (quickArmed && !wasArmed) {
            wasArmed = true
            RuntimeDiagnostics.gestureSignal(edge.name, "armed")
        }

        // Multi-tier Hysteresis logic:
        // Hold cancels at 88% of threshold (pulling back ~12% of the distance)
        if (holdArmed || holdScheduled) {
            if (dampedDisplacement < swipeThresholdPx * 0.88f) {
                if (holdArmed) RuntimeDiagnostics.gestureSignal(edge.name, "hold_cancelled")
                cancelHold()
            }
        }

        if (!ringActive && switchDirection == null && GestureCancelPolicy.shouldCancel(
                wasArmed = wasArmed || lSwipeDetector.inwardArmed,
                inwardDisplacement = dampedDisplacement,
                activationThreshold = swipeThresholdPx,
                hysteresisRatio = config.hysteresisRatio,
                maxInwardDisplacement = maxDampedDisplacement,
            )
        ) {
            RuntimeDiagnostics.gestureSignal(edge.name, "cancelled_after_armed")
            state = GestureState.CANCELLED
            lSwipeDetector.reset()
            cancelHold()
            publishProgress(active = false)
            return
        }

        if (switchDirection == null && quickArmed && !holdScheduled && !holdArmed) {
            holdScheduled = true
            val delayMs = if (hasRingActions()) {
                maxOf(config.holdTimeMs, RING_REVEAL_DELAY_MS)
            } else {
                config.holdTimeMs
            }
            handler.postDelayed(holdRunnable, delayMs)
        }

        if (ringActive) {
            if (dampedDisplacement < swipeThresholdPx * 0.72f) {
                RuntimeDiagnostics.ringDismissed(edge.name)
                ringActive = false
                ringSelectedIndex = -1
                state = GestureState.CANCELLED
                cancelHold()
                publishProgress(active = false)
                return
            }
            ringSelectedIndex = ringHoverTest(event.rawX, event.rawY, ringAnchorTouch)
            val hit = ringHitTest(event.rawX, event.rawY, ringAnchorTouch)
            if (hit >= 0) ringHitIndex = hit
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
            RuntimeDiagnostics.gestureSignal(edge.name, "l_swipe_executed")
            onGestureResult(result)
            finishProgress(event)
            reset()
            return
        }

        if (ringActive) {
            lastTouchAlongEdge = touchCoord(event)
            val hitAtRelease = ringHitTest(event.rawX, event.rawY, ringAnchorTouch)
            if (hitAtRelease >= 0) ringHitIndex = hitAtRelease
            if (ringHitIndex >= 0) {
                RuntimeDiagnostics.ringSelected(edge.name, ringHitIndex)
                onRingActionSelected(ringHitIndex)
            } else {
                RuntimeDiagnostics.ringDismissed(edge.name)
            }
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

        if (dampedDisplacement < swipeThresholdPx * config.hysteresisRatio && !holdFiredOnThreshold) {
            RuntimeDiagnostics.gestureSignal(edge.name, "released_below_hysteresis")
            finishProgress(event)
            reset()
            return
        }

        if (state == GestureState.DETECTED) {
            state = GestureState.EXECUTING
            if (holdFiredOnThreshold) {
                val result = GestureResult.EdgeSwipe(
                    edge = edge,
                    section = section,
                    gestureType = GestureType.QUICK_SWIPE,
                    touchAlongEdgePx = initialTouchPx,
                )
                RuntimeDiagnostics.gestureSignal(edge.name, "hold_already_fired")
                onGestureResult(result)
            } else {
                val result = resolveGestureResult(dampedDisplacement, section, dx, dy, initialTouchPx)
                if (result != null) {
                    onGestureResult(result)
                } else {
                    RuntimeDiagnostics.gestureSignal(edge.name, "no_result_emitted")
                }
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
                ringActive = false,
                ringSelectedIndex = -1,
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
                touchAlongEdgePx = if (ringActive) ringAnchorTouch else lastTouchAlongEdge,
                active = active,
                armed = armedNow,
                holdArmed = holdArmed,
                appSwitchDirection = lastSwitchDirection,
                isLUp = isLUp,
                isLDown = isLDown,
                bendStartY = bendStartY,
                lColorProgress = lSwipeDetector.turnProgress,
                lPreviewGesture = lSwipeDetector.previewDirection,
                ringActive = ringActive,
                ringSelectedIndex = ringSelectedIndex,
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
    ): GestureResult? {
        val minThreshold = swipeThresholdPx

        return when {
            edge == Edge.BOTTOM &&
                abs(rawDx) > minThreshold &&
                abs(rawDx) > abs(rawDy) ->
                GestureResult.BottomHorizontalSwipe(
                    direction = if (rawDx < 0) SwipeDirection.LEFT else SwipeDirection.RIGHT,
                    touchAlongEdgePx = touchAlongEdgePx,
                )
            displacement >= minThreshold -> {
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
            wasArmed -> {
                // Was once armed (swipe initiated & pulled back): DO NOT convert to Tap!
                RuntimeDiagnostics.gestureSignal(edge.name, "resolved_as_cancelled")
                null
            }
            edge != Edge.BOTTOM -> {
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
        ringActive = false
        ringSelectedIndex = -1
        ringHitIndex = -1
        ringOpenedStretch = 0f
        ringAnchorTouch = 0f
        lastLPreviewGesture = null
        lastStretch = 0f
        lastTouchAlongEdge = 0f
        lastSwitchDirection = null
        wasArmed = false
        maxDampedDisplacement = 0f
        lSwipeDetector.reset()
        state = GestureState.IDLE
        touchState.reset()
    }

    companion object {
        private const val LOG_TAG = "AkisGesture"
        private const val RING_REVEAL_DELAY_MS = 1_000L
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
    val lColorProgress: Float = 0f,
    val lPreviewGesture: GestureType? = null,
    val ringActive: Boolean = false,
    val ringSelectedIndex: Int = -1,
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
