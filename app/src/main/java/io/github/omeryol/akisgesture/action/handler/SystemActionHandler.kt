package io.github.omeryol.akisgesture.action.handler

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.view.inputmethod.InputMethodManager
import io.github.omeryol.akisgesture.action.ActionResult
import io.github.omeryol.akisgesture.root.RootCommandExecutor
import io.github.omeryol.akisgesture.root.RootResult
import io.github.omeryol.akisgesture.service.GestureAccessibilityService

class SystemActionHandler(
    private val service: GestureAccessibilityService,
    private val rootCommands: RootCommandExecutor,
    private val audioManager: AudioManager,
) {
    fun handleLockScreen(): ActionResult = requireApi(28) { globalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN) }

    fun handleScreenshot(): ActionResult = requireApi(28) { globalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT) }

    fun handleSplitScreen(): ActionResult = requireApi(24) { globalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN) }

    fun handlePowerMenu(): ActionResult = globalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)

    fun handleNotificationPanel(): ActionResult = globalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)

    fun handleQuickSettings(): ActionResult = globalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)

    fun handleInputMethodPicker(): ActionResult = try {
        val imm = service.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()
        ActionResult.Success
    } catch (e: Exception) {
        ActionResult.Failed(e.message ?: "Klavye seçici açılamadı")
    }

    fun handleVolumePanel(): ActionResult = try {
        audioManager.adjustVolume(AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
        ActionResult.Success
    } catch (e: Exception) {
        ActionResult.Failed(e.message ?: "Ses paneli açılamadı")
    }

    fun handleAssistant(): ActionResult = try {
        service.startActivity(Intent(Intent.ACTION_ASSIST).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        ActionResult.Success
    } catch (e: Exception) {
        ActionResult.Failed(e.message ?: "Sistem asistanı açılamadı")
    }

    fun handleBrightness(increase: Boolean): ActionResult = try {
        val resolver = service.contentResolver
        val current = try {
            android.provider.Settings.System.getInt(resolver, android.provider.Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Exception) { 128 }
        val delta = if (increase) 30 else -30
        val next = (current + delta).coerceIn(15, 255)
        if (android.provider.Settings.System.canWrite(service)) {
            android.provider.Settings.System.putInt(resolver, android.provider.Settings.System.SCREEN_BRIGHTNESS, next)
            ActionResult.Success
        } else {
            val res = rootCommands.execute("settings put system screen_brightness $next")
            if (res is RootResult.Success) ActionResult.Success
            else ActionResult.Failed("Ekran parlaklığını değiştirmek için sistem izni veya Root gerekli")
        }
    } catch (e: Exception) {
        ActionResult.Failed("Parlaklık ayarlanamadı: ${e.message}")
    }

    private fun globalAction(id: Int): ActionResult {
        return if (service.doPerformGlobalAction(id)) ActionResult.Success
        else ActionResult.Failed("performGlobalAction($id) returned false")
    }

    private inline fun requireApi(api: Int, block: () -> ActionResult): ActionResult {
        return if (Build.VERSION.SDK_INT >= api) block()
        else ActionResult.RequiresMinApi(api)
    }
}
