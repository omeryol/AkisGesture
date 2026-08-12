package io.github.omeryol.akisgesture.model

import androidx.annotation.DrawableRes
import io.github.omeryol.akisgesture.R

object ActionVisualResolver {
    fun resolve(action: ActionNode, pack: ActionIconPack): ActionVisual {
        val fallback = drawableFor(pack, action.toIconKey())
        val packageName = when (action) {
            is ActionNode.LaunchApp -> action.packageName
            is ActionNode.AppShortcut -> action.packageName
            else -> null
        }
        return if (packageName != null) {
            ActionVisual.ApplicationIcon(packageName, fallback)
        } else {
            ActionVisual.DrawableResource(fallback)
        }
    }

    @DrawableRes
    fun drawableFor(pack: ActionIconPack, key: ActionIconKey): Int = when (pack) {
        ActionIconPack.PHOSPHOR -> when (key) {
            ActionIconKey.BACK -> R.drawable.action_phosphor_back
            ActionIconKey.HOME -> R.drawable.action_phosphor_home
            ActionIconKey.RECENTS -> R.drawable.action_phosphor_recents
            ActionIconKey.SWITCH_LAST_APP -> R.drawable.action_phosphor_switch_last_app
            ActionIconKey.SWITCH_NEXT_APP -> R.drawable.action_phosphor_switch_next_app
            ActionIconKey.LOCK_SCREEN -> R.drawable.action_phosphor_lock_screen
            ActionIconKey.SCREENSHOT -> R.drawable.action_phosphor_screenshot
            ActionIconKey.SPLIT_SCREEN -> R.drawable.action_phosphor_split_screen
            ActionIconKey.POWER_MENU -> R.drawable.action_phosphor_power_menu
            ActionIconKey.MENU -> R.drawable.action_phosphor_menu
            ActionIconKey.NOTIFICATION_PANEL -> R.drawable.action_phosphor_notification_panel
            ActionIconKey.QUICK_SETTINGS -> R.drawable.action_phosphor_quick_settings
            ActionIconKey.INPUT_METHOD_PICKER -> R.drawable.action_phosphor_input_method_picker
            ActionIconKey.VOLUME_PANEL -> R.drawable.action_phosphor_volume_panel
            ActionIconKey.ASSISTANT -> R.drawable.action_phosphor_assistant
            ActionIconKey.TOGGLE_AUTO_ROTATE -> R.drawable.action_phosphor_toggle_auto_rotate
            ActionIconKey.FORCE_PORTRAIT -> R.drawable.action_phosphor_force_portrait
            ActionIconKey.FORCE_LANDSCAPE -> R.drawable.action_phosphor_force_landscape
            ActionIconKey.ONE_HAND_MODE -> R.drawable.action_phosphor_one_hand_mode
            ActionIconKey.MEDIA_PLAY_PAUSE -> R.drawable.action_phosphor_media_play_pause
            ActionIconKey.MEDIA_NEXT -> R.drawable.action_phosphor_media_next
            ActionIconKey.MEDIA_PREVIOUS -> R.drawable.action_phosphor_media_previous
            ActionIconKey.VOLUME_UP -> R.drawable.action_phosphor_volume_up
            ActionIconKey.VOLUME_DOWN -> R.drawable.action_phosphor_volume_down
            ActionIconKey.VOLUME_MUTE -> R.drawable.action_phosphor_volume_mute
            ActionIconKey.BRIGHTNESS_UP -> R.drawable.action_phosphor_brightness_up
            ActionIconKey.BRIGHTNESS_DOWN -> R.drawable.action_phosphor_brightness_down
            ActionIconKey.VOICE_SEARCH -> R.drawable.action_phosphor_voice_search
            ActionIconKey.VOICE_ASSISTANT -> R.drawable.action_phosphor_voice_assistant
            ActionIconKey.APPLICATION -> R.drawable.action_phosphor_application
            ActionIconKey.SEND_KEY_CODE -> R.drawable.action_phosphor_send_key_code
            ActionIconKey.FLASHLIGHT -> R.drawable.action_phosphor_flashlight
            ActionIconKey.FORCE_STOP_FOREGROUND -> R.drawable.action_phosphor_force_stop_foreground
            ActionIconKey.NONE -> R.drawable.action_phosphor_none
            ActionIconKey.UNKNOWN -> R.drawable.action_phosphor_none
        }
        ActionIconPack.TABLER -> when (key) {
            ActionIconKey.BACK -> R.drawable.action_tabler_back
            ActionIconKey.HOME -> R.drawable.action_tabler_home
            ActionIconKey.RECENTS -> R.drawable.action_tabler_recents
            ActionIconKey.SWITCH_LAST_APP -> R.drawable.action_tabler_switch_last_app
            ActionIconKey.SWITCH_NEXT_APP -> R.drawable.action_tabler_switch_next_app
            ActionIconKey.LOCK_SCREEN -> R.drawable.action_tabler_lock_screen
            ActionIconKey.SCREENSHOT -> R.drawable.action_tabler_screenshot
            ActionIconKey.SPLIT_SCREEN -> R.drawable.action_tabler_split_screen
            ActionIconKey.POWER_MENU -> R.drawable.action_tabler_power_menu
            ActionIconKey.MENU -> R.drawable.action_tabler_menu
            ActionIconKey.NOTIFICATION_PANEL -> R.drawable.action_tabler_notification_panel
            ActionIconKey.QUICK_SETTINGS -> R.drawable.action_tabler_quick_settings
            ActionIconKey.INPUT_METHOD_PICKER -> R.drawable.action_tabler_input_method_picker
            ActionIconKey.VOLUME_PANEL -> R.drawable.action_tabler_volume_panel
            ActionIconKey.ASSISTANT -> R.drawable.action_tabler_assistant
            ActionIconKey.TOGGLE_AUTO_ROTATE -> R.drawable.action_tabler_toggle_auto_rotate
            ActionIconKey.FORCE_PORTRAIT -> R.drawable.action_tabler_force_portrait
            ActionIconKey.FORCE_LANDSCAPE -> R.drawable.action_tabler_force_landscape
            ActionIconKey.ONE_HAND_MODE -> R.drawable.action_tabler_one_hand_mode
            ActionIconKey.MEDIA_PLAY_PAUSE -> R.drawable.action_tabler_media_play_pause
            ActionIconKey.MEDIA_NEXT -> R.drawable.action_tabler_media_next
            ActionIconKey.MEDIA_PREVIOUS -> R.drawable.action_tabler_media_previous
            ActionIconKey.VOLUME_UP -> R.drawable.action_tabler_volume_up
            ActionIconKey.VOLUME_DOWN -> R.drawable.action_tabler_volume_down
            ActionIconKey.VOLUME_MUTE -> R.drawable.action_tabler_volume_mute
            ActionIconKey.BRIGHTNESS_UP -> R.drawable.action_tabler_brightness_up
            ActionIconKey.BRIGHTNESS_DOWN -> R.drawable.action_tabler_brightness_down
            ActionIconKey.VOICE_SEARCH -> R.drawable.action_tabler_voice_search
            ActionIconKey.VOICE_ASSISTANT -> R.drawable.action_tabler_voice_assistant
            ActionIconKey.APPLICATION -> R.drawable.action_tabler_application
            ActionIconKey.SEND_KEY_CODE -> R.drawable.action_tabler_send_key_code
            ActionIconKey.FLASHLIGHT -> R.drawable.action_tabler_flashlight
            ActionIconKey.FORCE_STOP_FOREGROUND -> R.drawable.action_tabler_force_stop_foreground
            ActionIconKey.NONE -> R.drawable.action_tabler_none
            ActionIconKey.UNKNOWN -> R.drawable.action_tabler_none
        }
        ActionIconPack.ICONOIR -> when (key) {
            ActionIconKey.BACK -> R.drawable.action_iconoir_back
            ActionIconKey.HOME -> R.drawable.action_iconoir_home
            ActionIconKey.RECENTS -> R.drawable.action_iconoir_recents
            ActionIconKey.SWITCH_LAST_APP -> R.drawable.action_iconoir_switch_last_app
            ActionIconKey.SWITCH_NEXT_APP -> R.drawable.action_iconoir_switch_next_app
            ActionIconKey.LOCK_SCREEN -> R.drawable.action_iconoir_lock_screen
            ActionIconKey.SCREENSHOT -> R.drawable.action_iconoir_screenshot
            ActionIconKey.SPLIT_SCREEN -> R.drawable.action_iconoir_split_screen
            ActionIconKey.POWER_MENU -> R.drawable.action_iconoir_power_menu
            ActionIconKey.MENU -> R.drawable.action_iconoir_menu
            ActionIconKey.NOTIFICATION_PANEL -> R.drawable.action_iconoir_notification_panel
            ActionIconKey.QUICK_SETTINGS -> R.drawable.action_iconoir_quick_settings
            ActionIconKey.INPUT_METHOD_PICKER -> R.drawable.action_iconoir_input_method_picker
            ActionIconKey.VOLUME_PANEL -> R.drawable.action_iconoir_volume_panel
            ActionIconKey.ASSISTANT -> R.drawable.action_iconoir_assistant
            ActionIconKey.TOGGLE_AUTO_ROTATE -> R.drawable.action_iconoir_toggle_auto_rotate
            ActionIconKey.FORCE_PORTRAIT -> R.drawable.action_iconoir_force_portrait
            ActionIconKey.FORCE_LANDSCAPE -> R.drawable.action_iconoir_force_landscape
            ActionIconKey.ONE_HAND_MODE -> R.drawable.action_iconoir_one_hand_mode
            ActionIconKey.MEDIA_PLAY_PAUSE -> R.drawable.action_iconoir_media_play_pause
            ActionIconKey.MEDIA_NEXT -> R.drawable.action_iconoir_media_next
            ActionIconKey.MEDIA_PREVIOUS -> R.drawable.action_iconoir_media_previous
            ActionIconKey.VOLUME_UP -> R.drawable.action_iconoir_volume_up
            ActionIconKey.VOLUME_DOWN -> R.drawable.action_iconoir_volume_down
            ActionIconKey.VOLUME_MUTE -> R.drawable.action_iconoir_volume_mute
            ActionIconKey.BRIGHTNESS_UP -> R.drawable.action_iconoir_brightness_up
            ActionIconKey.BRIGHTNESS_DOWN -> R.drawable.action_iconoir_brightness_down
            ActionIconKey.VOICE_SEARCH -> R.drawable.action_iconoir_voice_search
            ActionIconKey.VOICE_ASSISTANT -> R.drawable.action_iconoir_voice_assistant
            ActionIconKey.APPLICATION -> R.drawable.action_iconoir_application
            ActionIconKey.SEND_KEY_CODE -> R.drawable.action_iconoir_send_key_code
            ActionIconKey.FLASHLIGHT -> R.drawable.action_iconoir_flashlight
            ActionIconKey.FORCE_STOP_FOREGROUND -> R.drawable.action_iconoir_force_stop_foreground
            ActionIconKey.NONE -> R.drawable.action_iconoir_none
            ActionIconKey.UNKNOWN -> R.drawable.action_iconoir_none
        }
        ActionIconPack.HEROICONS -> when (key) {
            ActionIconKey.BACK -> R.drawable.action_heroicons_back
            ActionIconKey.HOME -> R.drawable.action_heroicons_home
            ActionIconKey.RECENTS -> R.drawable.action_heroicons_recents
            ActionIconKey.SWITCH_LAST_APP -> R.drawable.action_heroicons_switch_last_app
            ActionIconKey.SWITCH_NEXT_APP -> R.drawable.action_heroicons_switch_next_app
            ActionIconKey.LOCK_SCREEN -> R.drawable.action_heroicons_lock_screen
            ActionIconKey.SCREENSHOT -> R.drawable.action_heroicons_screenshot
            ActionIconKey.SPLIT_SCREEN -> R.drawable.action_heroicons_split_screen
            ActionIconKey.POWER_MENU -> R.drawable.action_heroicons_power_menu
            ActionIconKey.MENU -> R.drawable.action_heroicons_menu
            ActionIconKey.NOTIFICATION_PANEL -> R.drawable.action_heroicons_notification_panel
            ActionIconKey.QUICK_SETTINGS -> R.drawable.action_heroicons_quick_settings
            ActionIconKey.INPUT_METHOD_PICKER -> R.drawable.action_heroicons_input_method_picker
            ActionIconKey.VOLUME_PANEL -> R.drawable.action_heroicons_volume_panel
            ActionIconKey.ASSISTANT -> R.drawable.action_heroicons_assistant
            ActionIconKey.TOGGLE_AUTO_ROTATE -> R.drawable.action_heroicons_toggle_auto_rotate
            ActionIconKey.FORCE_PORTRAIT -> R.drawable.action_heroicons_force_portrait
            ActionIconKey.FORCE_LANDSCAPE -> R.drawable.action_heroicons_force_landscape
            ActionIconKey.ONE_HAND_MODE -> R.drawable.action_heroicons_one_hand_mode
            ActionIconKey.MEDIA_PLAY_PAUSE -> R.drawable.action_heroicons_media_play_pause
            ActionIconKey.MEDIA_NEXT -> R.drawable.action_heroicons_media_next
            ActionIconKey.MEDIA_PREVIOUS -> R.drawable.action_heroicons_media_previous
            ActionIconKey.VOLUME_UP -> R.drawable.action_heroicons_volume_up
            ActionIconKey.VOLUME_DOWN -> R.drawable.action_heroicons_volume_down
            ActionIconKey.VOLUME_MUTE -> R.drawable.action_heroicons_volume_mute
            ActionIconKey.BRIGHTNESS_UP -> R.drawable.action_heroicons_brightness_up
            ActionIconKey.BRIGHTNESS_DOWN -> R.drawable.action_heroicons_brightness_down
            ActionIconKey.VOICE_SEARCH -> R.drawable.action_heroicons_voice_search
            ActionIconKey.VOICE_ASSISTANT -> R.drawable.action_heroicons_voice_assistant
            ActionIconKey.APPLICATION -> R.drawable.action_heroicons_application
            ActionIconKey.SEND_KEY_CODE -> R.drawable.action_heroicons_send_key_code
            ActionIconKey.FLASHLIGHT -> R.drawable.action_heroicons_flashlight
            ActionIconKey.FORCE_STOP_FOREGROUND -> R.drawable.action_heroicons_force_stop_foreground
            ActionIconKey.NONE -> R.drawable.action_heroicons_none
            ActionIconKey.UNKNOWN -> R.drawable.action_heroicons_none
        }
        ActionIconPack.BOOTSTRAP -> when (key) {
            ActionIconKey.BACK -> R.drawable.action_bootstrap_back
            ActionIconKey.HOME -> R.drawable.action_bootstrap_home
            ActionIconKey.RECENTS -> R.drawable.action_bootstrap_recents
            ActionIconKey.SWITCH_LAST_APP -> R.drawable.action_bootstrap_switch_last_app
            ActionIconKey.SWITCH_NEXT_APP -> R.drawable.action_bootstrap_switch_next_app
            ActionIconKey.LOCK_SCREEN -> R.drawable.action_bootstrap_lock_screen
            ActionIconKey.SCREENSHOT -> R.drawable.action_bootstrap_screenshot
            ActionIconKey.SPLIT_SCREEN -> R.drawable.action_bootstrap_split_screen
            ActionIconKey.POWER_MENU -> R.drawable.action_bootstrap_power_menu
            ActionIconKey.MENU -> R.drawable.action_bootstrap_menu
            ActionIconKey.NOTIFICATION_PANEL -> R.drawable.action_bootstrap_notification_panel
            ActionIconKey.QUICK_SETTINGS -> R.drawable.action_bootstrap_quick_settings
            ActionIconKey.INPUT_METHOD_PICKER -> R.drawable.action_bootstrap_input_method_picker
            ActionIconKey.VOLUME_PANEL -> R.drawable.action_bootstrap_volume_panel
            ActionIconKey.ASSISTANT -> R.drawable.action_bootstrap_assistant
            ActionIconKey.TOGGLE_AUTO_ROTATE -> R.drawable.action_bootstrap_toggle_auto_rotate
            ActionIconKey.FORCE_PORTRAIT -> R.drawable.action_bootstrap_force_portrait
            ActionIconKey.FORCE_LANDSCAPE -> R.drawable.action_bootstrap_force_landscape
            ActionIconKey.ONE_HAND_MODE -> R.drawable.action_bootstrap_one_hand_mode
            ActionIconKey.MEDIA_PLAY_PAUSE -> R.drawable.action_bootstrap_media_play_pause
            ActionIconKey.MEDIA_NEXT -> R.drawable.action_bootstrap_media_next
            ActionIconKey.MEDIA_PREVIOUS -> R.drawable.action_bootstrap_media_previous
            ActionIconKey.VOLUME_UP -> R.drawable.action_bootstrap_volume_up
            ActionIconKey.VOLUME_DOWN -> R.drawable.action_bootstrap_volume_down
            ActionIconKey.VOLUME_MUTE -> R.drawable.action_bootstrap_volume_mute
            ActionIconKey.BRIGHTNESS_UP -> R.drawable.action_bootstrap_brightness_up
            ActionIconKey.BRIGHTNESS_DOWN -> R.drawable.action_bootstrap_brightness_down
            ActionIconKey.VOICE_SEARCH -> R.drawable.action_bootstrap_voice_search
            ActionIconKey.VOICE_ASSISTANT -> R.drawable.action_bootstrap_voice_assistant
            ActionIconKey.APPLICATION -> R.drawable.action_bootstrap_application
            ActionIconKey.SEND_KEY_CODE -> R.drawable.action_bootstrap_send_key_code
            ActionIconKey.FLASHLIGHT -> R.drawable.action_bootstrap_flashlight
            ActionIconKey.FORCE_STOP_FOREGROUND -> R.drawable.action_bootstrap_force_stop_foreground
            ActionIconKey.NONE -> R.drawable.action_bootstrap_none
            ActionIconKey.UNKNOWN -> R.drawable.action_bootstrap_none
        }
        ActionIconPack.EVA -> when (key) {
            ActionIconKey.BACK -> R.drawable.action_eva_back
            ActionIconKey.HOME -> R.drawable.action_eva_home
            ActionIconKey.RECENTS -> R.drawable.action_eva_recents
            ActionIconKey.SWITCH_LAST_APP -> R.drawable.action_eva_switch_last_app
            ActionIconKey.SWITCH_NEXT_APP -> R.drawable.action_eva_switch_next_app
            ActionIconKey.LOCK_SCREEN -> R.drawable.action_eva_lock_screen
            ActionIconKey.SCREENSHOT -> R.drawable.action_eva_screenshot
            ActionIconKey.SPLIT_SCREEN -> R.drawable.action_eva_split_screen
            ActionIconKey.POWER_MENU -> R.drawable.action_eva_power_menu
            ActionIconKey.MENU -> R.drawable.action_eva_menu
            ActionIconKey.NOTIFICATION_PANEL -> R.drawable.action_eva_notification_panel
            ActionIconKey.QUICK_SETTINGS -> R.drawable.action_eva_quick_settings
            ActionIconKey.INPUT_METHOD_PICKER -> R.drawable.action_eva_input_method_picker
            ActionIconKey.VOLUME_PANEL -> R.drawable.action_eva_volume_panel
            ActionIconKey.ASSISTANT -> R.drawable.action_eva_assistant
            ActionIconKey.TOGGLE_AUTO_ROTATE -> R.drawable.action_eva_toggle_auto_rotate
            ActionIconKey.FORCE_PORTRAIT -> R.drawable.action_eva_force_portrait
            ActionIconKey.FORCE_LANDSCAPE -> R.drawable.action_eva_force_landscape
            ActionIconKey.ONE_HAND_MODE -> R.drawable.action_eva_one_hand_mode
            ActionIconKey.MEDIA_PLAY_PAUSE -> R.drawable.action_eva_media_play_pause
            ActionIconKey.MEDIA_NEXT -> R.drawable.action_eva_media_next
            ActionIconKey.MEDIA_PREVIOUS -> R.drawable.action_eva_media_previous
            ActionIconKey.VOLUME_UP -> R.drawable.action_eva_volume_up
            ActionIconKey.VOLUME_DOWN -> R.drawable.action_eva_volume_down
            ActionIconKey.VOLUME_MUTE -> R.drawable.action_eva_volume_mute
            ActionIconKey.BRIGHTNESS_UP -> R.drawable.action_eva_brightness_up
            ActionIconKey.BRIGHTNESS_DOWN -> R.drawable.action_eva_brightness_down
            ActionIconKey.VOICE_SEARCH -> R.drawable.action_eva_voice_search
            ActionIconKey.VOICE_ASSISTANT -> R.drawable.action_eva_voice_assistant
            ActionIconKey.APPLICATION -> R.drawable.action_eva_application
            ActionIconKey.SEND_KEY_CODE -> R.drawable.action_eva_send_key_code
            ActionIconKey.FLASHLIGHT -> R.drawable.action_eva_flashlight
            ActionIconKey.FORCE_STOP_FOREGROUND -> R.drawable.action_eva_force_stop_foreground
            ActionIconKey.NONE -> R.drawable.action_eva_none
            ActionIconKey.UNKNOWN -> R.drawable.action_eva_none
        }
    }
}

