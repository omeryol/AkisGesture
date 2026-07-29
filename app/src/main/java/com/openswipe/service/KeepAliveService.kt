package com.omer.akisgesture.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.omer.akisgesture.R
import com.omer.akisgesture.ui.MainActivity

class KeepAliveService : Service() {

    companion object {
        private const val CHANNEL_ID = "openswipe_keepalive"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Hareket hizmeti",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
                description = "Hareket hizmetini arka planda kullanılabilir tutar"
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        // 点击通知打开 App 主界面
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_gesture_24)
            .setContentTitle("Akış Gesture hareket hizmeti çalışıyor")
            .setContentText("Kenar hareketleri hazır. Ayarları açmak için dokunun.")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}
