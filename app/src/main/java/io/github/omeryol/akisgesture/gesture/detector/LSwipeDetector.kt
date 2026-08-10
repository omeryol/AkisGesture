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
    var previewDirection: GestureType? = null
        private set
    private var completedDirection: GestureType? = null

    fun reset() {
        maxInwardPx = 0f
        inwardArmed = false
        bendStartY = 0f
        detectedLGesture = null
        turnProgress = 0f
        previewDirection = null
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
            previewDirection = null
        }

        if (inwardArmed) {
            val turnDyRaw = event.rawY - bendStartY
            val turnDy = abs(turnDyRaw)
            val turnDx = abs(event.rawX - downX).coerceAtLeast(1f)
            val turnThreshold = lSwipeThresholdPx.coerceAtLeast(1f)
            val turnDistance: Float
            val perpendicularDistance: Float
            val candidateDirection: GestureType
            if (edge == Edge.BOTTOM) {
                val turnDxRaw = event.rawX - downX
                turnDistance = abs(turnDxRaw)
                perpendicularDistance = turnDy
                candidateDirection = if (turnDxRaw >= 0f) {
                    GestureType.SWIPE_UP_L
                } else {
                    GestureType.SWIPE_DOWN_L
                }
            } else {
                turnDistance = turnDy
                perpendicularDistance = turnDx
                candidateDirection = if (turnDyRaw <= 0f) {
                    GestureType.SWIPE_UP_L
                } else {
                    GestureType.SWIPE_DOWN_L
                }
            }
            val directionAllowed = completedDirection == null || completedDirection == candidateDirection
            previewDirection = if (directionAllowed && turnDistance > 0f) candidateDirection else null
            turnProgress = if (directionAllowed) {
                (turnDistance / turnThreshold).coerceIn(0f, 1f)
            } else {
                0f
            }

            val minimumTurnDistance = maxOf(turnThreshold, maxInwardPx)
            if (directionAllowed &&
                turnDistance >= minimumTurnDistance &&
                turnDistance >= perpendicularDistance * 1.0f
            ) {
                completedDirection = candidateDirection
                detectedLGesture = candidateDirection
            } else if (!directionAllowed || turnDistance < turnThreshold * 0.78f) {
                detectedLGesture = null
            }
        }
    }
}
