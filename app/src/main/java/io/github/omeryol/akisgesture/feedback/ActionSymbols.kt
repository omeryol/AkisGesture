package io.github.omeryol.akisgesture.feedback

import io.github.omeryol.akisgesture.model.ActionNode

/**
 * Identity-aligned, meaningful symbols for gesture actions.
 */
object ActionSymbols {
    fun symbolFor(action: ActionNode?): String = when (action) {
        null -> ""
        is ActionNode.Back -> "◀"
        is ActionNode.Home -> "⌂"
        is ActionNode.Recents -> "▤"
        is ActionNode.SwitchLastApp -> "↶"
        is ActionNode.SwitchNextApp -> "↷"
        is ActionNode.LockScreen -> "▣"
        is ActionNode.Screenshot -> "▣"
        is ActionNode.SplitScreen -> "▥"
        is ActionNode.PowerMenu -> "⏻"
        is ActionNode.Menu -> "☰"
        is ActionNode.NotificationPanel -> "◉"
        is ActionNode.QuickSettings -> "⚙"
        is ActionNode.InputMethodPicker -> "⌨"
        is ActionNode.VolumePanel -> "◖"
        is ActionNode.Assistant -> "✦"
        is ActionNode.ToggleAutoRotate -> "↻"
        is ActionNode.ForcePortrait -> "▯"
        is ActionNode.ForceLandscape -> "▭"
        is ActionNode.XiaomiOneHandMode -> "⌁"
        is ActionNode.MediaPlayPause -> "▶Ⅱ"
        is ActionNode.MediaNext -> "▶▶"
        is ActionNode.MediaPrevious -> "◀◀"
        is ActionNode.VolumeUp -> "◖"
        is ActionNode.VolumeDown -> "◗"
        is ActionNode.ToggleMute -> "⊘"
        is ActionNode.BrightnessUp -> "☼"
        is ActionNode.BrightnessDown -> "☾"
        is ActionNode.VoiceSearch -> "◌"
        is ActionNode.VoiceAssistant -> "✧"
        is ActionNode.AppShortcut -> "↗"
        is ActionNode.SendKeyCode -> "⌨"
        is ActionNode.ToggleFlashlight -> "✦"
        is ActionNode.ToggleNavBar -> "▥"
        is ActionNode.ForceStopForeground -> "■"
        is ActionNode.LaunchApp -> "↗"
        is ActionNode.NoAction -> ""
    }
}
