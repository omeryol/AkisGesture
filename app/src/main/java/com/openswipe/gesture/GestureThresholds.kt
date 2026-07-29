package com.omer.akisgesture.gesture

/**
 * Pure threshold rules used by both the real edge detector and the in-app
 * gesture rehearsal. This keeps calibration feedback honest.
 */
object GestureThresholds {
    fun dampedDisplacement(rawDisplacement: Float, dampingFactor: Float): Float =
        rawDisplacement.coerceAtLeast(0f) / dampingFactor.coerceAtLeast(0.1f)

    fun isQuickArmed(dampedDisplacement: Float, threshold: Float): Boolean =
        dampedDisplacement >= threshold

    fun isHoldArmed(
        dampedDisplacement: Float,
        threshold: Float,
        elapsedMs: Long,
        holdTimeMs: Long,
    ): Boolean =
        isQuickArmed(dampedDisplacement, threshold) && elapsedMs >= holdTimeMs
}
