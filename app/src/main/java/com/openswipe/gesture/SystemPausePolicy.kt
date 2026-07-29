package com.omer.akisgesture.gesture

object SystemPausePolicy {
    fun shouldPause(
        config: GestureConfig,
        lockScreenVisible: Boolean,
        keyboardVisible: Boolean,
        landscape: Boolean,
    ): Boolean =
        (config.pauseOnLockScreen && lockScreenVisible) ||
            (config.pauseWhenKeyboardVisible && keyboardVisible) ||
            (config.pauseInLandscape && landscape)
}
