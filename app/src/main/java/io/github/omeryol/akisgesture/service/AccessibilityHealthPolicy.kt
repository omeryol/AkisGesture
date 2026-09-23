package io.github.omeryol.akisgesture.service

object AccessibilityHealthPolicy {
    const val REPAIR_COOLDOWN_MS = 30_000L

    /**
     * Art arda bu kadar başarısız onarım denemesinden sonra [AccessibilityControl]
     * soğuma süresini [EXTENDED_COOLDOWN_MS]'e genişletir — tam durdurmaz (kalıcı
     * kilitlenme kullanıcıyı uygulamayı yeniden başlatmaya zorlar), sadece deneme
     * sıklığını ciddi biçimde azaltır.
     */
    const val MAX_REPAIR_ATTEMPTS = 5

    /** Art arda [MAX_REPAIR_ATTEMPTS] başarısızlıktan sonra kullanılan soğuma süresi. */
    const val EXTENDED_COOLDOWN_MS = 30 * 60_000L

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
