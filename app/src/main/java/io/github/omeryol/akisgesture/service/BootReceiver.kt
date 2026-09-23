package io.github.omeryol.akisgesture.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }
        if (!AccessibilityControl.isDesired(context)) return

        val serviceIntent = Intent(context, KeepAliveService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // goAsync() limiti yaklaşık 60 sn; repairIfNeeded() içindeki rebind() zinciri
        // uzadığında bu limiti aşabilir ve OEM'lerde uyarıya yol açabilir.
        // KeepAliveService zaten başlatıldı ve scheduleHealthCheck() ile onarımı kendisi
        // üstleniyor. Burada sadece Android'in doğal bağlama süresine kısa bir tolerans
        // tanımak yeterli — ağır onarım işini KeepAliveService'e bırak.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Paket güncellemesi ve açılışta Android'in normal erişilebilirlik
                // bağlama sürecini tamamlamasına izin ver.
                delay(5_000)
                // Servis bağlantısını kontrol et; bağlanmadıysa KeepAliveService
                // kendi scheduleHealthCheck() mekanizmasıyla devralır.
                // goAsync()'in 60 sn limitini korumak için burada uzun onarım YAPMA.
                if (GestureAccessibilityService.instance == null) {
                    AccessibilityControl.repairIfNeeded(
                        context.applicationContext,
                        repairCooldownMs = 10_000L,
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
