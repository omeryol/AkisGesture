package io.github.omeryol.akisgesture.action

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.view.KeyEvent
import io.github.omeryol.akisgesture.action.handler.HardwareAndAppHandler
import io.github.omeryol.akisgesture.action.handler.MediaActionHandler
import io.github.omeryol.akisgesture.action.handler.NavigationActionHandler
import io.github.omeryol.akisgesture.action.handler.SystemActionHandler
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.root.RootCommandExecutor
import io.github.omeryol.akisgesture.service.GestureAccessibilityService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface ActionDispatcher {
    suspend fun dispatch(action: ActionNode): ActionResult
}

sealed class ActionResult {
    data object Success : ActionResult()
    data class Failed(val reason: String) : ActionResult()
    data class RequiresMinApi(val api: Int) : ActionResult()
}

/**
 * Modular Action Dispatcher.
 * Delegates action execution to domain-specific handlers:
 * - [NavigationActionHandler] for Back, Home, Recents, App Switching
 * - [SystemActionHandler] for Panels, Screenshot, LockScreen, Brightness
 * - [MediaActionHandler] for Playback and Volume
 * - [HardwareAndAppHandler] for Flashlight, App Launch, Orientation, Shortcuts
 */
class ActionDispatcherImpl(
    private val service: GestureAccessibilityService,
) : ActionDispatcher {

    private val dispatchMutex = Mutex()

    private val audioManager by lazy {
        service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private val rootCommands by lazy { RootCommandExecutor(service) }
    private val cameraManager by lazy {
        service.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private val navHandler by lazy { NavigationActionHandler(service) }
    private val systemHandler by lazy { SystemActionHandler(service, rootCommands, audioManager) }
    private val mediaHandler by lazy { MediaActionHandler(audioManager) }
    private val hardwareHandler by lazy { HardwareAndAppHandler(service, rootCommands, audioManager, cameraManager) }

    override suspend fun dispatch(action: ActionNode): ActionResult = dispatchMutex.withLock {
        try {
            dispatchInternal(action)
        } catch (error: Exception) {
            ActionResult.Failed(error.message ?: "Aksiyon çalıştırılamadı")
        }
    }

    private suspend fun dispatchInternal(action: ActionNode): ActionResult = when (action) {
        is ActionNode.NoAction -> ActionResult.Success

        // ═══ Navigation ═══
        is ActionNode.Back -> navHandler.handleBack()
        is ActionNode.Home -> navHandler.handleHome()
        is ActionNode.Recents -> navHandler.handleRecents()
        is ActionNode.SwitchLastApp -> navHandler.handleSwitchLastApp()
        is ActionNode.SwitchNextApp -> navHandler.handleSwitchNextApp()

        // ═══ System & Panels ═══
        is ActionNode.LockScreen -> systemHandler.handleLockScreen()
        is ActionNode.Screenshot -> systemHandler.handleScreenshot()
        is ActionNode.SplitScreen -> systemHandler.handleSplitScreen()
        is ActionNode.PowerMenu -> systemHandler.handlePowerMenu()
        is ActionNode.NotificationPanel -> systemHandler.handleNotificationPanel()
        is ActionNode.QuickSettings -> systemHandler.handleQuickSettings()
        is ActionNode.InputMethodPicker -> systemHandler.handleInputMethodPicker()
        is ActionNode.VolumePanel -> systemHandler.handleVolumePanel()
        is ActionNode.Assistant -> systemHandler.handleAssistant()
        is ActionNode.BrightnessUp -> systemHandler.handleBrightness(increase = true)
        is ActionNode.BrightnessDown -> systemHandler.handleBrightness(increase = false)

        // ═══ Media & Volume ═══
        is ActionNode.MediaPlayPause -> mediaHandler.handleMediaPlayPause()
        is ActionNode.MediaPrevious -> mediaHandler.handleMediaPrevious()
        is ActionNode.MediaNext -> mediaHandler.handleMediaNext()
        is ActionNode.VolumeUp -> mediaHandler.handleVolumeUp()
        is ActionNode.VolumeDown -> mediaHandler.handleVolumeDown()
        is ActionNode.ToggleMute -> mediaHandler.handleToggleMute()

        // ═══ Hardware, Rotation & Apps ═══
        is ActionNode.ToggleFlashlight -> hardwareHandler.handleToggleFlashlight()
        is ActionNode.LaunchApp -> hardwareHandler.handleLaunchApp(action.packageName)
        is ActionNode.AppShortcut -> hardwareHandler.handleAppShortcut(action.packageName, action.shortcutId)
        is ActionNode.Menu -> hardwareHandler.handleSendKeyEvent(KeyEvent.KEYCODE_MENU)
        is ActionNode.ToggleAutoRotate -> hardwareHandler.handleToggleAutoRotate()
        is ActionNode.ForcePortrait -> hardwareHandler.handleForceOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        is ActionNode.ForceLandscape -> hardwareHandler.handleForceOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        is ActionNode.XiaomiOneHandMode -> hardwareHandler.handleXiaomiOneHandMode()
        is ActionNode.VoiceSearch -> hardwareHandler.handleVoiceSearch()
        is ActionNode.VoiceAssistant -> hardwareHandler.handleVoiceAssistant()
        is ActionNode.SendKeyCode -> hardwareHandler.handleSendKeyEvent(action.keyCode)
        is ActionNode.ForceStopForeground -> hardwareHandler.handleForceStopForeground()
    }
}
