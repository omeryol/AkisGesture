package com.omer.akisgesture.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.graphics.vector.ImageVector
import com.omer.akisgesture.model.ActionNode

fun actionImageVector(action: ActionNode): ImageVector = when (action) {
    is ActionNode.Back -> Icons.Filled.ArrowBack
    is ActionNode.Home -> Icons.Filled.Home
    is ActionNode.Recents -> Icons.Filled.Apps
    is ActionNode.SwitchLastApp -> Icons.Filled.SwapHoriz
    is ActionNode.LockScreen -> Icons.Filled.Lock
    is ActionNode.Screenshot -> Icons.Filled.CameraAlt
    is ActionNode.SplitScreen -> Icons.Filled.VerticalSplit
    is ActionNode.PowerMenu -> Icons.Filled.PowerSettingsNew
    is ActionNode.NotificationPanel -> Icons.Filled.Notifications
    is ActionNode.QuickSettings -> Icons.Filled.Settings
    is ActionNode.InputMethodPicker -> Icons.Filled.Keyboard
    is ActionNode.VolumePanel -> Icons.Filled.VolumeUp
    is ActionNode.Assistant -> Icons.Filled.Smartphone
    is ActionNode.MediaPlayPause -> Icons.Filled.PlayArrow
    is ActionNode.MediaNext -> Icons.Filled.FastForward
    is ActionNode.MediaPrevious -> Icons.Filled.FastRewind
    is ActionNode.VolumeUp -> Icons.Filled.VolumeUp
    is ActionNode.VolumeDown -> Icons.Filled.VolumeDown
    is ActionNode.ToggleMute -> Icons.Filled.VolumeOff
    is ActionNode.ToggleFlashlight -> Icons.Filled.FlashlightOn
    is ActionNode.ForceStopForeground -> Icons.Filled.StopCircle
    is ActionNode.LaunchApp -> Icons.Filled.Apps
    is ActionNode.NoAction -> Icons.Filled.Block
    else -> Icons.Filled.Refresh
}
