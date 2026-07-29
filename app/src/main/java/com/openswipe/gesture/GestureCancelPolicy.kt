package com.omer.akisgesture.gesture

object GestureCancelPolicy {
    private const val RETURN_TO_EDGE_RATIO = 0.25f

    fun shouldCancel(
        wasArmed: Boolean,
        inwardDisplacement: Float,
        activationThreshold: Float,
    ): Boolean {
        if (!wasArmed || activationThreshold <= 0f) return false
        return inwardDisplacement <= activationThreshold * RETURN_TO_EDGE_RATIO
    }
}
