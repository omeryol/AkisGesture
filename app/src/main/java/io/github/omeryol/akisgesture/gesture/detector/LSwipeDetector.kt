package io.github.omeryol.akisgesture.gesture.detector

import android.view.MotionEvent
import io.github.omeryol.akisgesture.diagnostics.RuntimeDiagnostics
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
    var bendStartX: Float = 0f
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
        bendStartX = 0f
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
            bendStartX = event.rawX
            RuntimeDiagnostics.lTrace(edge.name, "armed", mapOf(
                "inward" to "%.1f".format(java.util.Locale.US, currentInwardPx),
                "max_inward" to "%.1f".format(java.util.Locale.US, maxInwardPx),
                "threshold" to "%.1f".format(java.util.Locale.US, inwardThreshold),
                "bend_x" to "%.1f".format(java.util.Locale.US, bendStartX),
            ))
        }

        if (inwardArmed && currentInwardPx < inwardThreshold * 0.75f) {
            RuntimeDiagnostics.lTrace(edge.name, "disarmed_inward", mapOf(
                "inward" to "%.1f".format(java.util.Locale.US, currentInwardPx),
                "max_inward" to "%.1f".format(java.util.Locale.US, maxInwardPx),
            ))
            inwardArmed = false
            detectedLGesture = null
            completedDirection = null
            previewDirection = null
        }

        if (inwardArmed) {
            val turnDyRaw = event.rawY - bendStartY
            val turnDy = abs(turnDyRaw)
            val turnDx = abs(event.rawX - bendStartX)
            val turnThreshold = lSwipeThresholdPx.coerceAtLeast(1f)
            val turnDistance: Float
            val perpendicularDistance: Float
            val candidateDirection: GestureType
            if (edge == Edge.BOTTOM) {
                val turnDxRaw = event.rawX - bendStartX
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
            // Bottom-edge L gestures are intentionally asymmetric: the first
            // leg may be much longer than the horizontal turn. Do not make
            // the second-leg threshold grow with the inward distance, and do
            // not require the horizontal leg to exceed the vertical leg.
            val minimumTurnDistance = if (edge == Edge.BOTTOM) {
                turnThreshold
            } else {
                maxOf(turnThreshold, maxInwardPx)
            }
            previewDirection = if (directionAllowed && turnDistance >= minimumTurnDistance) {
                candidateDirection
            } else {
                null
            }
            turnProgress = if (directionAllowed) {
                (turnDistance / minimumTurnDistance).coerceIn(0f, 1f)
            } else {
                0f
            }

            if (turnDistance >= turnThreshold * 0.5f) {
                RuntimeDiagnostics.lTrace(edge.name, "turn", mapOf(
                    "turn" to "%.1f".format(java.util.Locale.US, turnDistance),
                    "minimum" to "%.1f".format(java.util.Locale.US, minimumTurnDistance),
                    "perpendicular" to "%.1f".format(java.util.Locale.US, perpendicularDistance),
                    "direction" to candidateDirection.name,
                ))
            }

            val turnShapeValid = edge == Edge.BOTTOM ||
                turnDistance >= perpendicularDistance * 1.0f
            if (directionAllowed &&
                turnDistance >= minimumTurnDistance &&
                turnShapeValid
            ) {
                completedDirection = candidateDirection
                detectedLGesture = candidateDirection
                RuntimeDiagnostics.lTrace(edge.name, "detected", mapOf("direction" to candidateDirection.name))
            } else if (!directionAllowed || turnDistance < turnThreshold * 0.78f) {
                detectedLGesture = null
            }
        }
    }
}
