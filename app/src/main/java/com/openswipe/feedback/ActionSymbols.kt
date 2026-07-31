package com.omer.akisgesture.feedback

import com.omer.akisgesture.model.ActionNode

/**
 * Expressive Smooth Circular Emoji symbols for gesture actions.
 */
object ActionSymbols {
    fun symbolFor(action: ActionNode?): String = when (action) {
        null -> ""
        is ActionNode.Back -> "🔴"          // Smooth round red orb / back button
        is ActionNode.Home -> "🔘"          // Smooth round radio button / home
        is ActionNode.Recents -> "⚪"       // Smooth round white orb / recents
        is ActionNode.SwitchLastApp -> "🔄" // Round loop arrow
        is ActionNode.SwitchNextApp -> "🔄" // Round loop arrow
        is ActionNode.LockScreen -> "🟣"    // Smooth round purple orb / lock
        is ActionNode.Screenshot -> "🔮"    // Smooth round crystal orb / screenshot
        is ActionNode.SplitScreen -> "🔵"   // Smooth round blue orb / split screen
        is ActionNode.PowerMenu -> "🟡"     // Smooth round yellow orb / power
        is ActionNode.Menu -> "🔘"          // Smooth round menu button
        is ActionNode.NotificationPanel -> "🔔" // Round bell
        is ActionNode.QuickSettings -> "⚙️"  // Round gear settings
        is ActionNode.InputMethodPicker -> "⌨️"
        is ActionNode.VolumePanel -> "🔊"   // Round speaker
        is ActionNode.Assistant -> "🌟"     // Round glowing star
        is ActionNode.ToggleAutoRotate -> "🔄"
        is ActionNode.ForcePortrait -> "🔴"
        is ActionNode.ForceLandscape -> "🔵"
        is ActionNode.XiaomiOneHandMode -> "🎯"
        is ActionNode.MediaPlayPause -> "⏯️"
        is ActionNode.MediaNext -> "⏭️"
        is ActionNode.MediaPrevious -> "⏮️"
        is ActionNode.VolumeUp -> "🔊"
        is ActionNode.VolumeDown -> "🔉"
        is ActionNode.ToggleMute -> "🔇"
        is ActionNode.BrightnessUp -> "☀️" // Round sun
        is ActionNode.BrightnessDown -> "🌙" // Round moon
        is ActionNode.VoiceSearch -> "🎙️"
        is ActionNode.VoiceAssistant -> "🤖"
        is ActionNode.AppShortcut -> "🚀"
        is ActionNode.SendKeyCode -> "⌨️"
        is ActionNode.ToggleFlashlight -> "💡" // Round bulb
        is ActionNode.ToggleNavBar -> "🔘"
        is ActionNode.ForceStopForeground -> "🛑" // Round stop
        is ActionNode.LaunchApp -> "🎯"      // Round target
        is ActionNode.NoAction -> ""
    }
}
