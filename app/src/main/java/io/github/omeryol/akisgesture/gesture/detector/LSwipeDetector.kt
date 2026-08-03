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
    var turnProgress: Float = 0f
        private set
    private var completedDirection: GestureType? = null

    fun reset() {
        maxInwardPx = 0f
        inwardArmed = false
        bendStartY = 0f
        detectedLGesture = null
        turnProgress = 0f
        completedDirection = null
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
        lSwipeThresholdPx: Float,
        hasLActionAtInitialTouch: Boolean,
    ) {
        maxInwardPx = maxOf(maxInwardPx, currentInwardPx)
        val inwardThreshold = swipeThresholdPx.coerceAtLeast(20f)

        if (!inwardArmed && maxInwardPx >= inwardThreshold && hasLActionAtInitialTouch) {
            inwardArmed = true
            bendStartY = event.rawY
        }

        if (inwardArmed && currentInwardPx < inwardThreshold * 0.75f) {
            inwardArmed = false
            detectedLGesture = null
            completedDirection = null
        }

        if (inwardArmed && (edge == Edge.LEFT || edge == Edge.RIGHT)) {
            val turnDyRaw = event.rawY - bendStartY
            val turnDy = abs(turnDyRaw)
            val turnDx = abs(event.rawX - downX).coerceAtLeast(1f)
            val turnThreshold = lSwipeThresholdPx.coerceAtLeast(1f)
            val candidateDirection = if (turnDyRaw <= 0f) {
                GestureType.SWIPE_UP_L
            } else {
                GestureType.SWIPE_DOWN_L
            }
            val directionAllowed = completedDirection == null || completedDirection == candidateDirection
            turnProgress = if (directionAllowed) {
                (turnDy / turnThreshold).coerceIn(0f, 1f)
            } else {
                0f
            }

            if (directionAllowed && turnDy >= turnThreshold && turnDy >= turnDx * 1.0f) {
                completedDirection = candidateDirection
                detectedLGesture = candidateDirection
            } else if (!directionAllowed || turnDy < turnThreshold * 0.78f) {
                detectedLGesture = null
            }
        }
    }
}
