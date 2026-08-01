package io.github.omeryol.akisgesture.ui.util

import io.github.omeryol.akisgesture.model.ActionNode

/**
 * Shared action category definitions used by ActionPickerDialog and AddRuleDialog.
 */
fun actionCategories(actions: List<ActionNode> = ActionNode.allFixed()): List<Pair<String, List<ActionNode>>> = listOf(
    "Gezinme" to actions.filter {
        it is ActionNode.Back || it is ActionNode.Home || it is ActionNode.Recents ||
            it is ActionNode.SwitchLastApp || it is ActionNode.SwitchNextApp
    },
    "Sistem" to actions.filter {
        it is ActionNode.LockScreen || it is ActionNode.Screenshot || it is ActionNode.SplitScreen ||
            it is ActionNode.PowerMenu || it is ActionNode.Menu
    },
    "Paneller" to actions.filter {
        it is ActionNode.NotificationPanel || it is ActionNode.QuickSettings ||
            it is ActionNode.InputMethodPicker || it is ActionNode.VolumePanel
    },
    "Asistan" to actions.filter { it is ActionNode.Assistant || it is ActionNode.VoiceSearch || it is ActionNode.VoiceAssistant },
    "Döndürme" to actions.filter {
        it is ActionNode.ToggleAutoRotate || it is ActionNode.ForcePortrait ||
            it is ActionNode.ForceLandscape || it is ActionNode.XiaomiOneHandMode
    },
    "Medya" to actions.filter {
        it is ActionNode.MediaPlayPause || it is ActionNode.MediaNext ||
            it is ActionNode.MediaPrevious || it is ActionNode.VolumeUp ||
            it is ActionNode.VolumeDown || it is ActionNode.ToggleMute
    },
    "Ekran" to actions.filter {
        it is ActionNode.BrightnessUp || it is ActionNode.BrightnessDown
    },
    "Donanım" to actions.filter { it is ActionNode.ToggleFlashlight },
    "Sistem Arayüzü" to actions.filter { it is ActionNode.ToggleNavBar },
    "Root" to actions.filter { it is ActionNode.ForceStopForeground },
    "Diğer" to actions.filter { it is ActionNode.NoAction },
)
