package io.github.omeryol.akisgesture.model

/** Stable semantic keys shared by every selectable action icon family. */
enum class ActionIconKey(val resourceName: String) {
    BACK("back"),
    HOME("home"),
    RECENTS("recents"),
    SWITCH_LAST_APP("switch_last_app"),
    SWITCH_NEXT_APP("switch_next_app"),
    LOCK_SCREEN("lock_screen"),
    SCREENSHOT("screenshot"),
    SPLIT_SCREEN("split_screen"),
    POWER_MENU("power_menu"),
    MENU("menu"),
    NOTIFICATION_PANEL("notification_panel"),
    QUICK_SETTINGS("quick_settings"),
    INPUT_METHOD_PICKER("input_method_picker"),
    VOLUME_PANEL("volume_panel"),
    ASSISTANT("assistant"),
    TOGGLE_AUTO_ROTATE("toggle_auto_rotate"),
    FORCE_PORTRAIT("force_portrait"),
    FORCE_LANDSCAPE("force_landscape"),
    ONE_HAND_MODE("one_hand_mode"),
    MEDIA_PLAY_PAUSE("media_play_pause"),
    MEDIA_NEXT("media_next"),
    MEDIA_PREVIOUS("media_previous"),
    VOLUME_UP("volume_up"),
    VOLUME_DOWN("volume_down"),
    VOLUME_MUTE("volume_mute"),
    BRIGHTNESS_UP("brightness_up"),
    BRIGHTNESS_DOWN("brightness_down"),
    VOICE_SEARCH("voice_search"),
    VOICE_ASSISTANT("voice_assistant"),
    APPLICATION("application"),
    SEND_KEY_CODE("send_key_code"),
    FLASHLIGHT("flashlight"),
    FORCE_STOP_FOREGROUND("force_stop_foreground"),
    NONE("none"),
    UNKNOWN("unknown"),
}

/** The only ActionNode -> semantic key mapping in the application. */
fun ActionNode.toIconKey(): ActionIconKey = when (this) {
    ActionNode.Back -> ActionIconKey.BACK
    ActionNode.Home -> ActionIconKey.HOME
    ActionNode.Recents -> ActionIconKey.RECENTS
    ActionNode.SwitchLastApp -> ActionIconKey.SWITCH_LAST_APP
    ActionNode.SwitchNextApp -> ActionIconKey.SWITCH_NEXT_APP
    ActionNode.LockScreen -> ActionIconKey.LOCK_SCREEN
    ActionNode.Screenshot -> ActionIconKey.SCREENSHOT
    ActionNode.SplitScreen -> ActionIconKey.SPLIT_SCREEN
    ActionNode.PowerMenu -> ActionIconKey.POWER_MENU
    ActionNode.Menu -> ActionIconKey.MENU
    ActionNode.NotificationPanel -> ActionIconKey.NOTIFICATION_PANEL
    ActionNode.QuickSettings -> ActionIconKey.QUICK_SETTINGS
    ActionNode.InputMethodPicker -> ActionIconKey.INPUT_METHOD_PICKER
    ActionNode.VolumePanel -> ActionIconKey.VOLUME_PANEL
    ActionNode.Assistant -> ActionIconKey.ASSISTANT
    ActionNode.ToggleAutoRotate -> ActionIconKey.TOGGLE_AUTO_ROTATE
    ActionNode.ForcePortrait -> ActionIconKey.FORCE_PORTRAIT
    ActionNode.ForceLandscape -> ActionIconKey.FORCE_LANDSCAPE
    ActionNode.XiaomiOneHandMode -> ActionIconKey.ONE_HAND_MODE
    ActionNode.MediaPlayPause -> ActionIconKey.MEDIA_PLAY_PAUSE
    ActionNode.MediaNext -> ActionIconKey.MEDIA_NEXT
    ActionNode.MediaPrevious -> ActionIconKey.MEDIA_PREVIOUS
    ActionNode.VolumeUp -> ActionIconKey.VOLUME_UP
    ActionNode.VolumeDown -> ActionIconKey.VOLUME_DOWN
    ActionNode.ToggleMute -> ActionIconKey.VOLUME_MUTE
    ActionNode.BrightnessUp -> ActionIconKey.BRIGHTNESS_UP
    ActionNode.BrightnessDown -> ActionIconKey.BRIGHTNESS_DOWN
    ActionNode.VoiceSearch -> ActionIconKey.VOICE_SEARCH
    ActionNode.VoiceAssistant -> ActionIconKey.VOICE_ASSISTANT
    is ActionNode.LaunchApp, is ActionNode.AppShortcut -> ActionIconKey.APPLICATION
    is ActionNode.SendKeyCode -> ActionIconKey.SEND_KEY_CODE
    ActionNode.ToggleFlashlight -> ActionIconKey.FLASHLIGHT
    ActionNode.ForceStopForeground -> ActionIconKey.FORCE_STOP_FOREGROUND
    ActionNode.NoAction -> ActionIconKey.NONE
}

val ActionIconKey.isKnown: Boolean get() = this != ActionIconKey.UNKNOWN
