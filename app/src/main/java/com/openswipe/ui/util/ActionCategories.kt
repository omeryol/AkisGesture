package com.omer.akisgesture.ui.util

import com.omer.akisgesture.model.ActionNode

/**
 * Shared action category definitions used by ActionPickerDialog and AddRuleDialog.
 */
fun actionCategories(actions: List<ActionNode> = ActionNode.allFixed()): List<Pair<String, List<ActionNode>>> = listOf(
    "Gezinme" to actions.filter { it is ActionNode.Back || it is ActionNode.Home || it is ActionNode.Recents || it is ActionNode.SwitchLastApp },
    "Sistem" to actions.filter { it is ActionNode.LockScreen || it is ActionNode.Screenshot || it is ActionNode.SplitScreen || it is ActionNode.PowerMenu },
    "Paneller" to actions.filter { it is ActionNode.NotificationPanel || it is ActionNode.QuickSettings },
    "Medya" to actions.filter { it is ActionNode.MediaPlayPause || it is ActionNode.MediaNext || it is ActionNode.MediaPrevious || it is ActionNode.VolumeUp || it is ActionNode.VolumeDown },
    "Donanım" to actions.filter { it is ActionNode.ToggleFlashlight },
    "Diğer" to actions.filter { it is ActionNode.NoAction },
)
