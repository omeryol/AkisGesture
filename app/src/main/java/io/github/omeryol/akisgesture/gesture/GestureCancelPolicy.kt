package io.github.omeryol.akisgesture.gesture

object GestureCancelPolicy {
    /**
     * @param wasArmed whether the quick-swipe threshold was once crossed
     * @param inwardDisplacement current damped inward displacement
     * @param activationThreshold the per-edge activation threshold in px
     * @param hysteresisRatio how far back finger must go to cancel (0.1–0.9)
     * @param maxInwardDisplacement peak inward displacement reached during current touch
     * @return true if gesture should be cancelled
     */
    fun shouldCancel(
        wasArmed: Boolean,
        inwardDisplacement: Float,
        activationThreshold: Float,
        hysteresisRatio: Float = 0.75f,
        maxInwardDisplacement: Float = inwardDisplacement,
    ): Boolean {
        if (activationThreshold <= 0f) return false

        if (wasArmed) {
            val returnRatio = (1f - hysteresisRatio).coerceIn(0.1f, 0.95f)
            return inwardDisplacement < activationThreshold * returnRatio
        }

        // Cancel partially-pulled gesture if finger retreats 50% back toward edge from peak
        return maxInwardDisplacement > 8f && inwardDisplacement <= maxInwardDisplacement * 0.50f
    }
}

