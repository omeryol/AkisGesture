package com.openswipe.action

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.view.KeyEvent
import android.view.ViewConfiguration
import com.openswipe.model.ActionNode
import com.openswipe.service.GestureAccessibilityService
import kotlinx.coroutines.delay

interface ActionDispatcher {
    @Deprecated("Use dispatch(ActionNode) instead", replaceWith = ReplaceWith("dispatch(actionNode)"))
    suspend fun dispatch(action: ActionType): ActionResult
    suspend fun dispatch(action: ActionNode): ActionResult
}

sealed class ActionResult {
    data object Success : ActionResult()
    data class Failed(val reason: String) : ActionResult()
    data class RequiresMinApi(val api: Int) : ActionResult()
}

class ActionDispatcherImpl(
    private val service: GestureAccessibilityService,
) : ActionDispatcher {

    private val audioManager by lazy {
        service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override suspend fun dispatch(action: ActionNode): ActionResult = when (action) {
        is ActionNode.NoAction -> ActionResult.Success
        is ActionNode.Back -> globalAction(GLOBAL_ACTION_BACK)
        is ActionNode.Home -> globalAction(GLOBAL_ACTION_HOME)
        is ActionNode.Recents -> globalAction(GLOBAL_ACTION_RECENTS)
        is ActionNode.SwitchLastApp -> switchLastApp()
        is ActionNode.SplitScreen -> requireApi(24) { globalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN) }
        is ActionNode.PowerMenu -> globalAction(GLOBAL_ACTION_POWER_DIALOG)
        is ActionNode.LockScreen -> requireApi(28) { globalAction(GLOBAL_ACTION_LOCK_SCREEN) }
        is ActionNode.Screenshot -> requireApi(28) { globalAction(GLOBAL_ACTION_TAKE_SCREENSHOT) }
        is ActionNode.NotificationPanel -> globalAction(4)
        is ActionNode.QuickSettings -> globalAction(5)
        is ActionNode.MediaPlayPause -> mediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        is ActionNode.MediaPrevious -> mediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        is ActionNode.MediaNext -> mediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
        is ActionNode.VolumeUp -> {
            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
            ActionResult.Success
        }
        is ActionNode.VolumeDown -> {
            audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
            ActionResult.Success
        }
        is ActionNode.ToggleFlashlight -> ActionResult.Failed("Not implemented yet")
        is ActionNode.LaunchApp -> launchApp(action.packageName, "${action.packageName}.MainActivity")
    }

    @Deprecated("Use dispatch(ActionNode) instead")
    override suspend fun dispatch(action: ActionType): ActionResult = when (action) {
        is ActionType.None -> ActionResult.Success

        is ActionType.Navigation.Back -> globalAction(GLOBAL_ACTION_BACK)
        is ActionType.Navigation.Home -> globalAction(GLOBAL_ACTION_HOME)
        is ActionType.Navigation.Recents -> globalAction(GLOBAL_ACTION_RECENTS)
        is ActionType.Navigation.SwitchLastApp -> switchLastApp()
        is ActionType.Navigation.SplitScreen -> requireApi(24) {
            globalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
        }
        is ActionType.Navigation.PowerDialog -> globalAction(GLOBAL_ACTION_POWER_DIALOG)
        is ActionType.Navigation.LockScreen -> requireApi(28) {
            globalAction(GLOBAL_ACTION_LOCK_SCREEN)
        }
        is ActionType.Navigation.TakeScreenshot -> requireApi(28) {
            globalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
        }
        is ActionType.Navigation.Notifications -> globalAction(4)
        is ActionType.Navigation.QuickSettings -> globalAction(5)

        is ActionType.Media.PlayPause -> mediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        is ActionType.Media.Previous -> mediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        is ActionType.Media.Next -> mediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
        is ActionType.Media.VolumeUp -> {
            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
            ActionResult.Success
        }
        is ActionType.Media.VolumeDown -> {
            audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
            ActionResult.Success
        }
        is ActionType.Media.ToggleMute -> {
            audioManager.adjustVolume(AudioManager.ADJUST_TOGGLE_MUTE, AudioManager.FLAG_SHOW_UI)
            ActionResult.Success
        }

        is ActionType.LaunchApp -> launchApp(action.packageName, action.activityName)

        is ActionType.ToggleFlashlight -> ActionResult.Failed("Not implemented yet")
        is ActionType.SwitchInputMethod -> ActionResult.Failed("Not implemented yet")
    }

    private fun globalAction(id: Int): ActionResult {
        return if (service.doPerformGlobalAction(id)) ActionResult.Success
        else ActionResult.Failed("performGlobalAction($id) returned false")
    }

    private inline fun requireApi(api: Int, block: () -> ActionResult): ActionResult {
        return if (Build.VERSION.SDK_INT >= api) block()
        else ActionResult.RequiresMinApi(api)
    }

    private fun mediaKey(keyCode: Int): ActionResult {
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        return ActionResult.Success
    }

    private suspend fun switchLastApp(): ActionResult {
        return requireApi(24) {
            service.doPerformGlobalAction(GLOBAL_ACTION_RECENTS)
            delay(ViewConfiguration.getDoubleTapTimeout().toLong())
            globalAction(GLOBAL_ACTION_RECENTS)
        }
    }

    private fun launchApp(pkg: String, activity: String): ActionResult = try {
        service.startActivity(Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            component = ComponentName(pkg, activity)
        })
        ActionResult.Success
    } catch (e: Exception) {
        ActionResult.Failed(e.message ?: "Launch failed")
    }

    companion object {
        const val GLOBAL_ACTION_BACK = 1
        const val GLOBAL_ACTION_HOME = 2
        const val GLOBAL_ACTION_RECENTS = 3
        const val GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN = 7
        const val GLOBAL_ACTION_POWER_DIALOG = 6
        const val GLOBAL_ACTION_LOCK_SCREEN = 8
        const val GLOBAL_ACTION_TAKE_SCREENSHOT = 9
    }
}
