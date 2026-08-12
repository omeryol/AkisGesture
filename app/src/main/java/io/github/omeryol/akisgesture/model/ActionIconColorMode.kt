package io.github.omeryol.akisgesture.model

enum class ActionIconColorMode(val id: String) {
    MONOCHROME("monochrome"),
    THEME("theme"),
    FUNCTIONAL("functional"),
    NEON("neon"),
    ACCENT("accent");

    fun resolveColorInt(action: ActionNode, themeColor: Int? = null): Int {
        return when (this) {
            MONOCHROME -> 0xFFFFFFFF.toInt()
            THEME -> themeColor ?: 0xFF536DFF.toInt()
            ACCENT -> 0xFF008F83.toInt()
            NEON -> when (action) {
                is ActionNode.Back, is ActionNode.Home, is ActionNode.Recents,
                is ActionNode.SwitchLastApp, is ActionNode.SwitchNextApp -> 0xFF00E5FF.toInt()
                is ActionNode.LockScreen, is ActionNode.Screenshot, is ActionNode.SplitScreen,
                is ActionNode.PowerMenu, is ActionNode.Menu -> 0xFFFF5500.toInt()
                is ActionNode.NotificationPanel, is ActionNode.QuickSettings,
                is ActionNode.InputMethodPicker, is ActionNode.VolumePanel -> 0xFF00FF88.toInt()
                is ActionNode.MediaPlayPause, is ActionNode.MediaNext, is ActionNode.MediaPrevious,
                is ActionNode.VolumeUp, is ActionNode.VolumeDown, is ActionNode.ToggleMute -> 0xFFFFEE00.toInt()
                is ActionNode.ToggleFlashlight -> 0xFFE040FB.toInt()
                is ActionNode.ForceStopForeground -> 0xFFFF1744.toInt()
                else -> 0xFF00E5FF.toInt()
            }
            FUNCTIONAL -> when (action) {
                is ActionNode.Back, is ActionNode.Home, is ActionNode.Recents,
                is ActionNode.SwitchLastApp, is ActionNode.SwitchNextApp -> 0xFF6C83FF.toInt()
                is ActionNode.LockScreen, is ActionNode.Screenshot, is ActionNode.SplitScreen,
                is ActionNode.PowerMenu, is ActionNode.Menu -> 0xFFFF8668.toInt()
                is ActionNode.NotificationPanel, is ActionNode.QuickSettings,
                is ActionNode.InputMethodPicker, is ActionNode.VolumePanel -> 0xFF4AD8C0.toInt()
                is ActionNode.MediaPlayPause, is ActionNode.MediaNext, is ActionNode.MediaPrevious,
                is ActionNode.VolumeUp, is ActionNode.VolumeDown, is ActionNode.ToggleMute -> 0xFFFFC857.toInt()
                is ActionNode.ToggleFlashlight -> 0xFFB99CFF.toInt()
                is ActionNode.ForceStopForeground -> 0xFFFF6B6B.toInt()
                else -> 0xFF6C83FF.toInt()
            }
        }
    }

    companion object {
        fun fromId(id: String?): ActionIconColorMode =
            entries.firstOrNull { it.id == id } ?: FUNCTIONAL
    }
}
