package io.github.omeryol.akisgesture.gesture

object GestureCancelPolicy {
    /**
     * @param wasArmed whether the quick-swipe threshold was once crossed
     * @param inwardDisplacement current damped inward displacement
     * @param activationThreshold the per-edge activation threshold in px
     * @param hysteresisRatio how far back finger must go to cancel (0.1–0.9)
     * @return true if gesture should be cancelled
     */
    fun shouldCancel(
        wasArmed: Boolean,
        inwardDisplacement: Float,
        activationThreshold: Float,
        hysteresisRatio: Float = 0.45f,
    ): Boolean {
        if (!wasArmed || activationThreshold <= 0f) return false
        return inwardDisplacement <= activationThreshold * hysteresisRatio.coerceIn(0.1f, 0.9f)
    }
}
