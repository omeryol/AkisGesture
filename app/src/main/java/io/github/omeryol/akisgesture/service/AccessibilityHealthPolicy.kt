package io.github.omeryol.akisgesture.service

object AccessibilityHealthPolicy {
    const val REPAIR_COOLDOWN_MS = 30_000L

    enum class Action {
        NONE,
        ENABLE_SETTING,
        REBIND_SERVICE,
    }

    fun decide(
        desired: Boolean,
        settingEnabled: Boolean,
        serviceConnected: Boolean,
        millisSinceLastRepair: Long,
        repairCooldownMs: Long = REPAIR_COOLDOWN_MS,
    ): Action {
        if (!desired) return Action.NONE
        if (millisSinceLastRepair in 0 until repairCooldownMs) return Action.NONE
        if (!settingEnabled) return Action.ENABLE_SETTING
        if (!serviceConnected) return Action.REBIND_SERVICE
        return Action.NONE
    }
}
