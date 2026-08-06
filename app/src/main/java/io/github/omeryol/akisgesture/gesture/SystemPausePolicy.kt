package io.github.omeryol.akisgesture.gesture

object SystemPausePolicy {
    fun shouldPause(
        config: GestureConfig,
        lockScreenVisible: Boolean,
        keyboardVisible: Boolean,
        landscape: Boolean,
        fullScreen: Boolean = false,
        permissionScreen: Boolean = false,
        cameraActive: Boolean = false,
        phoneCallActive: Boolean = false,
    ): Boolean =
        (config.pauseOnLockScreen && lockScreenVisible) ||
            (config.pauseWhenKeyboardVisible && keyboardVisible) ||
            (config.pauseInLandscape && landscape) ||
            (config.pauseOnFullScreen && fullScreen) ||
            (config.pauseOnPermissionScreen && permissionScreen) ||
            (config.pauseOnCamera && cameraActive) ||
            (config.pauseOnPhoneCall && phoneCallActive)
}

