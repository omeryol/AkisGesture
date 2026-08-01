package io.github.omeryol.akisgesture.gesture.detector

import android.view.MotionEvent
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.abs

/**
 * 2-Phase L-Swipe Detector module.
 * Phase 1: Tracks max inward displacement to arm the L-gesture.
 * Phase 2: Detects turning motion (upward/downward) relative to the bend start position.
 */
class LSwipeDetector {

    var maxInwardPx: Float = 0f
        private set
    var inwardArmed: Boolean = false
        private set
    var bendStartY: Float = 0f
        private set
    var detectedLGesture: GestureType? = null
        private set

    fun reset() {
        maxInwardPx = 0f
        inwardArmed = false
        bendStartY = 0f
        detectedLGesture = null
    }

    fun onDown() {
        reset()
    }

    fun onMove(
        event: MotionEvent,
        edge: Edge,
        downX: Float,
        downY: Float,
        currentInwardPx: Float,
        swipeThresholdPx: Float,
        hasLActionAtInitialTouch: Boolean,
    ) {
        maxInwardPx = maxOf(maxInwardPx, currentInwardPx)
        val inwardThreshold = swipeThresholdPx.coerceAtLeast(20f)

        if (!inwardArmed && maxInwardPx >= inwardThreshold && hasLActionAtInitialTouch) {
            inwardArmed = true
            bendStartY = event.rawY
        }

        if (inwardArmed && (edge == Edge.LEFT || edge == Edge.RIGHT)) {
            val turnDyRaw = event.rawY - bendStartY
            val turnDy = abs(turnDyRaw)
            val turnDx = abs(event.rawX - downX).coerceAtLeast(1f)
            val turnThreshold = (swipeThresholdPx * 1.5f).coerceAtLeast(70f)

            if (turnDy >= turnThreshold && turnDy >= turnDx * 1.0f) {
                detectedLGesture = if (turnDyRaw <= 0f) GestureType.SWIPE_UP_L else GestureType.SWIPE_DOWN_L
            } else if (turnDy < turnThreshold * 0.5f) {
                detectedLGesture = null
            }
        }
    }
}
