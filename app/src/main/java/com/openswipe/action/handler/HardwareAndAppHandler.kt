package com.omer.akisgesture.action.handler

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.view.KeyEvent
import com.omer.akisgesture.action.ActionResult
import com.omer.akisgesture.root.RootCommandExecutor
import com.omer.akisgesture.root.RootResult
import com.omer.akisgesture.service.GestureAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HardwareAndAppHandler(
    private val service: GestureAccessibilityService,
    private val rootCommands: RootCommandExecutor,
    private val audioManager: AudioManager,
    private val cameraManager: CameraManager,
) {
    @Volatile
    private var torchEnabled = false
    @Volatile
    private var torchCallbackRegistered = false

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            torchEnabled = enabled
        }
    }

    suspend fun handleToggleFlashlight(): ActionResult = withContext(Dispatchers.IO) {
        when (val grant = rootCommands.grantCameraPermission()) {
            is RootResult.Failure -> return@withContext ActionResult.Failed(grant.reason)
            RootResult.Success -> Unit
        }
        try {
            if (!torchCallbackRegistered) {
                cameraManager.registerTorchCallback(torchCallback, null)
                torchCallbackRegistered = true
            }
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val info = cameraManager.getCameraCharacteristics(id)
                info.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                    info.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: return@withContext ActionResult.Failed("Fener desteklenmiyor")
            cameraManager.setTorchMode(cameraId, !torchEnabled)
            ActionResult.Success
        } catch (error: Exception) {
            ActionResult.Failed(error.message ?: "Fener değiştirilemedi")
        }
    }

    fun handleLaunchApp(pkg: String): ActionResult = try {
        val launchIntent = service.packageManager.getLaunchIntentForPackage(pkg)
            ?: return ActionResult.Failed("Uygulamanın açılış ekranı bulunamadı")
        service.startActivity(launchIntent.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        })
        ActionResult.Success
    } catch (e: Exception) {
        ActionResult.Failed(e.message ?: "Uygulama açılamadı")
    }

    fun handleAppShortcut(packageName: String, shortcutId: String): ActionResult = try {
        val shortcutManager = service.getSystemService(Context.SHORTCUT_SERVICE) as? android.content.pm.ShortcutManager
            ?: return ActionResult.Failed("ShortcutManager kullanılamıyor")
        val intent = shortcutManager.createShortcutResultIntent(
            android.content.pm.ShortcutInfo.Builder(service, shortcutId)
                .setShortLabel(shortcutId)
                .build(),
        )
        service.startActivity(intent.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        ActionResult.Success
    } catch (e: Exception) {
        handleLaunchApp(packageName)
    }

    fun handleToggleAutoRotate(): ActionResult = try {
        val current = android.provider.Settings.System.getInt(
            service.contentResolver,
            android.provider.Settings.System.ACCELEROMETER_ROTATION,
            0,
        )
        android.provider.Settings.System.putInt(
            service.contentResolver,
            android.provider.Settings.System.ACCELEROMETER_ROTATION,
            if (current == 1) 0 else 1,
        )
        ActionResult.Success
    } catch (e: SecurityException) {
        ActionResult.Failed("Döndürme izni için sistem ayarı gerekiyor")
    } catch (e: Exception) {
        ActionResult.Failed(e.message ?: "Döndürme değiştirilemedi")
    }

    fun handleForceOrientation(orientation: Int): ActionResult = try {
        android.provider.Settings.System.putInt(
            service.contentResolver,
            android.provider.Settings.System.ACCELEROMETER_ROTATION, 0,
        )
        android.provider.Settings.System.putInt(
            service.contentResolver,
            android.provider.Settings.System.USER_ROTATION,
            when (orientation) {
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> 0
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> 1
                else -> 0
            },
        )
        ActionResult.Success
    } catch (e: SecurityException) {
        ActionResult.Failed("Yön değiştirme izni yok")
    } catch (e: Exception) {
        ActionResult.Failed(e.message ?: "Yön değiştirilemedi")
    }

    fun handleXiaomiOneHandMode(): ActionResult = try {
        val intent = Intent("miui.intent.action.ONE_HAND_SWITCH").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        service.sendBroadcast(intent)
        android.provider.Settings.Secure.putInt(service.contentResolver, "one_handed_mode", 1)
        ActionResult.Success
    } catch (e: Exception) {
        ActionResult.Failed("Tek el modu yalnızca Xiaomi/MIUI/HyperOS cihazlarda desteklenir")
    }

    fun handleSendKeyEvent(keyCode: Int): ActionResult {
        service.doPerformGlobalAction(keyCode)
        return try {
            audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
            ActionResult.Success
        } catch (_: Exception) {
            ActionResult.Failed("Tuş kodu gönderilemedi: $keyCode")
        }
    }

    fun handleVoiceSearch(): ActionResult = try {
        service.startActivity(Intent(android.speech.RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        ActionResult.Success
    } catch (e: Exception) {
        ActionResult.Failed(e.message ?: "Sesli arama açılamadı")
    }

    fun handleVoiceAssistant(): ActionResult = try {
        service.startActivity(Intent(Intent.ACTION_VOICE_COMMAND).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        ActionResult.Success
    } catch (e: Exception) {
        ActionResult.Failed(e.message ?: "Sesli asistan açılamadı")
    }

    suspend fun handleForceStopForeground(): ActionResult = withContext(Dispatchers.IO) {
        val packageName = service.foregroundPackage()
            ?: return@withContext ActionResult.Failed("Öndeki uygulama belirlenemedi")
        when (val result = rootCommands.forceStopPersonalProfile(packageName)) {
            RootResult.Success -> ActionResult.Success
            is RootResult.Failure -> ActionResult.Failed(result.reason)
        }
    }

    suspend fun handleToggleNavBar(): ActionResult = withContext(Dispatchers.IO) {
        when (val result = rootCommands.toggleNavBar()) {
            RootResult.Success -> ActionResult.Success
            is RootResult.Failure -> ActionResult.Failed(result.reason)
        }
    }
}
