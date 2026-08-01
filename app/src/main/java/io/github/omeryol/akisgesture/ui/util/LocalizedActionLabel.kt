package io.github.omeryol.akisgesture.ui.util

import android.content.Context
import io.github.omeryol.akisgesture.R
import io.github.omeryol.akisgesture.model.ActionNode

fun ActionNode.localizedLabel(context: Context): String = when (this) {
    ActionNode.Back -> context.getString(R.string.action_back)
    ActionNode.Home -> context.getString(R.string.action_home)
    ActionNode.Recents -> context.getString(R.string.action_recents)
    ActionNode.SwitchLastApp -> context.getString(R.string.action_previous_app)
    ActionNode.SwitchNextApp -> context.getString(R.string.action_next_app)
    ActionNode.LockScreen -> context.getString(R.string.action_lock_screen)
    ActionNode.Screenshot -> context.getString(R.string.action_screenshot)
    ActionNode.SplitScreen -> context.getString(R.string.action_split_screen)
    ActionNode.PowerMenu -> context.getString(R.string.action_power_menu)
    ActionNode.Menu -> context.getString(R.string.action_menu)
    ActionNode.NotificationPanel -> context.getString(R.string.action_notifications)
    ActionNode.QuickSettings -> context.getString(R.string.action_quick_settings)
    ActionNode.InputMethodPicker -> context.getString(R.string.action_keyboard_picker)
    ActionNode.VolumePanel -> context.getString(R.string.action_volume_panel)
    ActionNode.Assistant -> context.getString(R.string.action_system_assistant)
    ActionNode.ToggleAutoRotate -> context.getString(R.string.action_auto_rotate)
    ActionNode.ForcePortrait -> context.getString(R.string.action_portrait)
    ActionNode.ForceLandscape -> context.getString(R.string.action_landscape)
    ActionNode.XiaomiOneHandMode -> context.getString(R.string.action_one_hand)
    ActionNode.MediaPlayPause -> context.getString(R.string.action_play_pause)
    ActionNode.MediaNext -> context.getString(R.string.action_next_track)
    ActionNode.MediaPrevious -> context.getString(R.string.action_previous_track)
    ActionNode.VolumeUp -> context.getString(R.string.action_volume_up)
    ActionNode.VolumeDown -> context.getString(R.string.action_volume_down)
    ActionNode.ToggleMute -> context.getString(R.string.action_toggle_mute)
    ActionNode.VoiceSearch -> context.getString(R.string.action_voice_search)
    ActionNode.VoiceAssistant -> context.getString(R.string.action_voice_assistant)
    is ActionNode.SendKeyCode -> context.getString(R.string.action_keycode, keyLabel)
    ActionNode.ToggleNavBar -> context.getString(R.string.action_nav_bar)
    ActionNode.ToggleFlashlight -> context.getString(R.string.action_flashlight)
    ActionNode.ForceStopForeground -> context.getString(R.string.action_force_stop)
    ActionNode.BrightnessUp -> context.getString(R.string.action_brightness_up)
    ActionNode.BrightnessDown -> context.getString(R.string.action_brightness_down)
    ActionNode.NoAction -> context.getString(R.string.action_none)
    is ActionNode.AppShortcut -> shortcutLabel
    is ActionNode.LaunchApp -> appName
}
