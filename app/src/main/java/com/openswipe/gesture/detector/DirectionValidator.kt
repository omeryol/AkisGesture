package com.omer.akisgesture.gesture.detector

import com.omer.akisgesture.gesture.GestureConfig
import com.omer.akisgesture.overlay.Edge
import kotlin.math.abs
import kotlin.math.atan2

/**
 * Validates swipe motion direction and angle tolerance against gesture configuration rules.
 */
object DirectionValidator {

    /**
     * Verifies if the (dx, dy) displacement matches the expected inward swipe direction for the given edge.
     */
    fun isValidInwardSwipe(edge: Edge, dx: Float, dy: Float, isBottomAppSwitch: Boolean = false): Boolean = when (edge) {
        Edge.BOTTOM -> (dy < 0 && abs(dy) > abs(dx)) || isBottomAppSwitch
        Edge.LEFT -> dx > 0 && abs(dx) > abs(dy)    // Rightward swipe
        Edge.RIGHT -> dx < 0 && abs(dx) > abs(dy)   // Leftward swipe
    }

    /**
     * Checks if the angle difference exceeds direction tolerance.
     * Returns true if the gesture direction is within valid bounds.
     */
    fun isAngleWithinTolerance(
        edge: Edge,
        dx: Float,
        dy: Float,
        configToleranceDegrees: Float,
    ): Boolean {
        if (edge == Edge.BOTTOM) return true

        val expectedAngle = when (edge) {
            Edge.LEFT -> 0.0     // Rightward = 0°
            Edge.RIGHT -> 180.0  // Leftward = 180°
            Edge.BOTTOM -> 270.0 // Upward = 270°
        }

        val actualAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).let {
            if (it < 0) it + 360.0 else it
        }

        var angleDiff = abs(actualAngle - expectedAngle)
        if (angleDiff > 180.0) angleDiff = 360.0 - angleDiff

        val effectiveTolerance = configToleranceDegrees.coerceAtLeast(40f)
        return angleDiff <= effectiveTolerance
    }
}
