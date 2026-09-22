package io.github.omeryol.akisgesture.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.github.omeryol.akisgesture.R
import io.github.omeryol.akisgesture.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.github.omeryol.akisgesture.diagnostics.RuntimeDiagnostics
import kotlinx.coroutines.flow.collectLatest

class KeepAliveService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var healthCheckJob: Job? = null
    private var watchdogJob: Job? = null

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_ON ||
                intent.action == Intent.ACTION_USER_PRESENT ||
                intent.action == GestureTileService.ACTION_TILE_STATE_CHANGED
            ) {
                scheduleHealthCheck()
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "openswipe_keepalive"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_REACTIVE_REPAIR = "io.github.omeryol.akisgesture.action.REACTIVE_REPAIR"

        fun triggerReactiveRepair(context: Context, reason: String = "service_unbind") {
            val intent = Intent(context, KeepAliveService::class.java).apply {
                action = ACTION_REACTIVE_REPAIR
                putExtra("reason", reason)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenReceiver, filter)
        }
        val visible = (application as? io.github.omeryol.akisgesture.AkisGestureApp)
            ?.gestureConfigFlow?.value?.foregroundNotificationVisible != false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Hareket hizmeti",
                if (visible) NotificationManager.IMPORTANCE_LOW else NotificationManager.IMPORTANCE_MIN
            ).apply {
                setShowBadge(false)
                description = getString(io.github.omeryol.akisgesture.R.string.service_channel_description)
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        // Bildirime dokununca ana ekranı aç
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_gesture_24)
            .setContentTitle(getString(io.github.omeryol.akisgesture.R.string.service_running))
            .setContentText(getString(io.github.omeryol.akisgesture.R.string.service_ready_tap))
            .setContentIntent(pendingIntent)
            .setPriority(if (visible) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_MIN)
            .setSilent(!visible)
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)

        // Tier 2: Agresif Periyodik Denetim (Watchdog)
        // Yalnızca kullanıcı ayarlardan açıkça etkinleştirirse periyodik arka plan döngüsü çalıştırılır.
        val app = application as? io.github.omeryol.akisgesture.AkisGestureApp
        if (app != null) {
            watchdogJob = serviceScope.launch {
                app.gestureConfigFlow.collectLatest { config ->
                    if (!config.rootWatchdogEnabled) return@collectLatest
                    while (true) {
                        val intervalMs = (config.rootWatchdogIntervalSeconds.coerceAtLeast(15)) * 1_000L
                        delay(intervalMs)
                        // Hızlı yol: Servis zaten bağlı ve sağlıklıysa gereksiz kabuk (shell) işlemi yürütme
                        val instance = GestureAccessibilityService.instance
                        if (instance == null || !instance.isOverlayHealthy() || !AccessibilityControl.isEnabled(this@KeepAliveService)) {
                            AccessibilityControl.repairIfNeeded(
                                this@KeepAliveService,
                                repairCooldownMs = intervalMs / 2,
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_REACTIVE_REPAIR) {
            val reason = intent.getStringExtra("reason") ?: "reactive"
            scheduleHealthCheck(gracePeriodMs = 1_000L, source = reason)
        } else {
            scheduleHealthCheck(gracePeriodMs = 1_500L, source = "start_command")
        }
        return START_STICKY
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(screenReceiver) }
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun scheduleHealthCheck(gracePeriodMs: Long = 1_500L, source: String = "screen_wake") {
        healthCheckJob?.cancel()
        healthCheckJob = serviceScope.launch {
            // Hızlı yol (Tier 1): Servis zaten etkin ve bağlıysa 0 ms, 0 işlemci yükü ile geç
            if (GestureAccessibilityService.instance?.isOverlayHealthy() == true &&
                AccessibilityControl.isEnabled(this@KeepAliveService)
            ) {
                RuntimeDiagnostics.healthCheckEvaluated(source, "healthy_skipped")
                return@launch
            }
            // Servis henüz bağlı değilse Android'in doğal olarak bağlanması için kısa bir yetki/bekleme süresi tanı
            if (gracePeriodMs > 0) {
                delay(gracePeriodMs)
            }
            if (GestureAccessibilityService.instance?.isOverlayHealthy() == true &&
                AccessibilityControl.isEnabled(this@KeepAliveService)
            ) {
                RuntimeDiagnostics.healthCheckEvaluated(source, "healthy_after_grace")
                return@launch
            }
            RuntimeDiagnostics.healthCheckEvaluated(
                source,
                "evaluating_repair",
                mapOf("service_connected" to (GestureAccessibilityService.instance != null).toString()),
            )
            AccessibilityControl.repairIfNeeded(
                this@KeepAliveService,
                repairCooldownMs = 10_000L,
            )
        }
    }
}
