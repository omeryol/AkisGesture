package io.github.omeryol.akisgesture.action.handler

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Process
import android.view.KeyEvent
import io.github.omeryol.akisgesture.action.ActionResult
import io.github.omeryol.akisgesture.root.RootCommandExecutor
import io.github.omeryol.akisgesture.root.RootResult
import io.github.omeryol.akisgesture.service.GestureAccessibilityService
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
        val hasPerm = androidx.core.content.ContextCompat.checkSelfPermission(
            service,
            android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasPerm) {
            val grant = rootCommands.grantCameraPermission()
            if (grant is RootResult.Failure) {
                return@withContext ActionResult.Failed("Kamera izni gerekiyor")
            }
        }
        try {
            if (!torchCallbackRegistered) {
                try {
                    cameraManager.registerTorchCallback(torchCallback, null)
                    torchCallbackRegistered = true
                } catch (_: Exception) {}
            }
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val info = cameraManager.getCameraCharacteristics(id)
                info.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                    info.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: cameraManager.cameraIdList.firstOrNull { id ->
                val info = cameraManager.getCameraCharacteristics(id)
                info.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return@withContext ActionResult.Failed("Fener desteklenmiyor")

            val targetMode = !torchEnabled
            cameraManager.setTorchMode(cameraId, targetMode)
            torchEnabled = targetMode
            ActionResult.Success
        } catch (error: Exception) {
            ActionResult.Failed(error.message ?: "Fener değiştirilemedi")
        }
    }

    fun handleLaunchApp(pkg: String): ActionResult = try {
        val launchIntent = service.packageManager.getLaunchIntentForPackage(pkg)
            ?: return ActionResult.Failed("Uygulamanın açılış ekranı bulunamadı")

        launchIntent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                Intent.FLAG_ACTIVITY_SINGLE_TOP,
        )

        val pendingIntent = android.app.PendingIntent.getActivity(
            service,
            0,
            launchIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
        )

        if (android.os.Build.VERSION.SDK_INT >= 34) {
            val options = android.app.ActivityOptions.makeBasic()
            options.setPendingIntentBackgroundActivityStartMode(android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
            pendingIntent.send(service, 0, null, null, null, null, options.toBundle())
        } else {
            pendingIntent.send()
        }
        ActionResult.Success
    } catch (_: Exception) {
        try {
            val launchIntent = service.packageManager.getLaunchIntentForPackage(pkg)
                ?: return ActionResult.Failed("Uygulamanın açılış ekranı bulunamadı")
            service.startActivity(launchIntent.apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP,
                )
            })
            ActionResult.Success
        } catch (e: Exception) {
            ActionResult.Failed(e.message ?: "Uygulama açılamadı")
        }
    }

    fun handleAppShortcut(packageName: String, shortcutId: String): ActionResult = try {
        val launcherApps = service.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            ?: return ActionResult.Failed("Uygulama kısayolları kullanılamıyor")
        launcherApps.startShortcut(packageName, shortcutId, null, null, Process.myUserHandle())
        ActionResult.Success
    } catch (e: Exception) {
        ActionResult.Failed(e.message ?: "Uygulama kısayolu açılamadı")
    }

    fun handleToggleAutoRotate(): ActionResult = try {
        val current = android.provider.Settings.System.getInt(
            service.contentResolver,
            android.provider.Settings.System.ACCELEROMETER_ROTATION,
            0,
        )
        val written = android.provider.Settings.System.putInt(
            service.contentResolver,
            android.provider.Settings.System.ACCELEROMETER_ROTATION,
            if (current == 1) 0 else 1,
        )
        if (written) ActionResult.Success else ActionResult.Failed("Döndürme ayarı değiştirilemedi")
    } catch (e: SecurityException) {
        ActionResult.Failed("Döndürme izni için sistem ayarı gerekiyor")
    } catch (e: Exception) {
        ActionResult.Failed(e.message ?: "Döndürme değiştirilemedi")
    }

    fun handleForceOrientation(orientation: Int): ActionResult = try {
        val rotationDisabled = android.provider.Settings.System.putInt(
            service.contentResolver,
            android.provider.Settings.System.ACCELEROMETER_ROTATION, 0,
        )
        val orientationWritten = android.provider.Settings.System.putInt(
            service.contentResolver,
            android.provider.Settings.System.USER_ROTATION,
            when (orientation) {
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> 0
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> 1
                else -> 0
            },
        )
        if (rotationDisabled && orientationWritten) ActionResult.Success
        else ActionResult.Failed("Yön ayarı değiştirilemedi")
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

    suspend fun handleSendKeyEvent(keyCode: Int): ActionResult = withContext(Dispatchers.IO) {
        if (keyCode !in 0..KeyEvent.getMaxKeyCode()) {
            return@withContext ActionResult.Failed("Geçersiz tuş kodu: $keyCode")
        }
        when (val rootResult = rootCommands.execute("input keyevent $keyCode")) {
            RootResult.Success -> ActionResult.Success
            is RootResult.Failure -> {
                if (keyCode in MEDIA_KEY_CODES) {
                    try {
                        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
                        ActionResult.Success
                    } catch (_: Exception) {
                        ActionResult.Failed(rootResult.reason)
                    }
                } else {
                    ActionResult.Failed(rootResult.reason)
                }
            }
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
        val packageName = service.foregroundAppPackage()
            ?: return@withContext ActionResult.Failed("Öndeki uygulama belirlenemedi")
        when (val result = rootCommands.forceStopPersonalProfile(packageName)) {
            RootResult.Success -> ActionResult.Success
            is RootResult.Failure -> ActionResult.Failed(result.reason)
        }
    }


    companion object {
        private val MEDIA_KEY_CODES = setOf(
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PAUSE,
            KeyEvent.KEYCODE_MEDIA_NEXT,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            KeyEvent.KEYCODE_MEDIA_STOP,
        )
    }
}
