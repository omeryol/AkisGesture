package io.github.omeryol.akisgesture.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VerticalSplit

import androidx.compose.ui.graphics.vector.ImageVector
import io.github.omeryol.akisgesture.model.ActionIconPack
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.toSymbol
import io.github.omeryol.akisgesture.ui.theme.ActionHardwareColor
import io.github.omeryol.akisgesture.ui.theme.ActionMediaColor
import io.github.omeryol.akisgesture.ui.theme.ActionNavColor
import io.github.omeryol.akisgesture.ui.theme.ActionPanelColor
import io.github.omeryol.akisgesture.ui.theme.ActionRootColor
import io.github.omeryol.akisgesture.ui.theme.ActionSystemColor

fun actionEmoji(action: ActionNode, pack: ActionIconPack = ActionIconPack.EMOJI_MODERN): String = action.toSymbol(pack)

fun actionImageVector(action: ActionNode): ImageVector = when (action) {
    is ActionNode.Back -> Icons.AutoMirrored.Filled.ArrowBack
    is ActionNode.Home -> Icons.Filled.Home
    is ActionNode.Recents -> Icons.Filled.Apps
    is ActionNode.SwitchLastApp -> Icons.Filled.SwapHoriz
    is ActionNode.SwitchNextApp -> Icons.Filled.SwapHoriz
    is ActionNode.LockScreen -> Icons.Filled.Lock
    is ActionNode.Screenshot -> Icons.Filled.CameraAlt
    is ActionNode.SplitScreen -> Icons.Filled.VerticalSplit
    is ActionNode.PowerMenu -> Icons.Filled.PowerSettingsNew
    is ActionNode.Menu -> Icons.Filled.Menu
    is ActionNode.NotificationPanel -> Icons.Filled.Notifications
    is ActionNode.QuickSettings -> Icons.Filled.Settings
    is ActionNode.InputMethodPicker -> Icons.Filled.Keyboard
    is ActionNode.VolumePanel -> Icons.AutoMirrored.Filled.VolumeUp
    is ActionNode.Assistant -> Icons.Filled.Smartphone
    is ActionNode.ToggleAutoRotate -> Icons.Filled.ScreenRotation
    is ActionNode.ForcePortrait -> Icons.Filled.Smartphone
    is ActionNode.ForceLandscape -> Icons.Filled.ScreenRotation
    is ActionNode.XiaomiOneHandMode -> Icons.Filled.TouchApp
    is ActionNode.MediaPlayPause -> Icons.Filled.PlayArrow
    is ActionNode.MediaNext -> Icons.Filled.FastForward
    is ActionNode.MediaPrevious -> Icons.Filled.FastRewind
    is ActionNode.VolumeUp -> Icons.AutoMirrored.Filled.VolumeUp
    is ActionNode.VolumeDown -> Icons.AutoMirrored.Filled.VolumeDown
    is ActionNode.ToggleMute -> Icons.AutoMirrored.Filled.VolumeOff
    is ActionNode.VoiceSearch -> Icons.Filled.Mic
    is ActionNode.VoiceAssistant -> Icons.Filled.Mic
    is ActionNode.AppShortcut -> Icons.Filled.Apps
    is ActionNode.SendKeyCode -> Icons.Filled.Keyboard
    is ActionNode.ToggleFlashlight -> Icons.Filled.FlashlightOn
    is ActionNode.ToggleNavBar -> Icons.Filled.SwapHoriz
    is ActionNode.ForceStopForeground -> Icons.Filled.StopCircle
    is ActionNode.LaunchApp -> Icons.Filled.Apps
    is ActionNode.NoAction -> Icons.Filled.Block
    else -> Icons.Filled.Refresh
}

fun actionCategoryColor(action: ActionNode): androidx.compose.ui.graphics.Color = when (action) {
    is ActionNode.Back, is ActionNode.Home, is ActionNode.Recents,
    is ActionNode.SwitchLastApp, is ActionNode.SwitchNextApp -> ActionNavColor
    is ActionNode.LockScreen, is ActionNode.Screenshot, is ActionNode.SplitScreen,
    is ActionNode.PowerMenu, is ActionNode.Menu -> ActionSystemColor
    is ActionNode.NotificationPanel, is ActionNode.QuickSettings,
    is ActionNode.InputMethodPicker, is ActionNode.VolumePanel -> ActionPanelColor
    is ActionNode.MediaPlayPause, is ActionNode.MediaNext, is ActionNode.MediaPrevious,
    is ActionNode.VolumeUp, is ActionNode.VolumeDown, is ActionNode.ToggleMute -> ActionMediaColor
    is ActionNode.ToggleFlashlight, is ActionNode.ToggleNavBar -> ActionHardwareColor
    is ActionNode.ForceStopForeground -> ActionRootColor
    else -> ActionNavColor
}
