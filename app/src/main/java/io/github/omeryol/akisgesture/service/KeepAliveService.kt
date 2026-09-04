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

        // The user-configured watchdog is a real periodic loop.  Keep the
        // existing screen-triggered repair as an immediate fast path.
        val app = application as? io.github.omeryol.akisgesture.AkisGestureApp
        if (app != null) {
            watchdogJob = serviceScope.launch {
                app.gestureConfigFlow.collectLatest { config ->
                    if (!config.rootWatchdogEnabled) return@collectLatest
                    while (true) {
                        val intervalMs = config.rootWatchdogIntervalSeconds * 1_000L
                        AccessibilityControl.repairIfNeeded(
                            this@KeepAliveService,
                            repairCooldownMs = intervalMs,
                        )
                        delay(intervalMs)
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scheduleHealthCheck()
        return START_STICKY
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(screenReceiver) }
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun scheduleHealthCheck() {
        healthCheckJob?.cancel()
        healthCheckJob = serviceScope.launch {
            // Fast-path: if the accessibility service is already active and healthy,
            // no delay or repair is needed.
            if (GestureAccessibilityService.instance != null && AccessibilityControl.isEnabled(this@KeepAliveService)) {
                RuntimeDiagnostics.healthCheckEvaluated("screen_wake", "healthy_skipped")
                return@launch
            }
            // If the service is not currently bound (e.g. system waking from sleep/doze),
            // wait a short grace period to allow Android to bind it before triggering a repair.
            delay(1_500L)
            RuntimeDiagnostics.healthCheckEvaluated(
                "screen_wake",
                "evaluating_repair",
                mapOf("service_connected" to (GestureAccessibilityService.instance != null).toString()),
            )
            AccessibilityControl.repairIfNeeded(
                this@KeepAliveService,
                repairCooldownMs = 15_000L,
            )
        }
    }
}
