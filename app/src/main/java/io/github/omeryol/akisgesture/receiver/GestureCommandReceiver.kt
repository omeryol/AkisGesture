package io.github.omeryol.akisgesture.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.github.omeryol.akisgesture.service.AccessibilityControl

/**
 * Otomasyon uygulamaları için Broadcast Receiver.
 *
 * START / STOP / TOGGLE intentleri root varsa erişilebilirlik hizmetini
 * doğrudan etkinleştirir/devre dışı bırakır. Root yoksa yalnızca istenilen
 * durumu kaydeder (desired state).
 *
 * Dış otomasyon yalnızca hizmeti başlatabilir, durdurabilir veya durumunu
 * değiştirebilir; başka erişilebilirlik eylemleri çalıştırılamaz.
 */
class GestureCommandReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(LOG_TAG, "Broadcast received with action: $action")
        when (action) {
            ACTION_START, LEGACY_ACTION_START -> enableService(context, true)
            ACTION_STOP, LEGACY_ACTION_STOP -> enableService(context, false)
            ACTION_TOGGLE, LEGACY_ACTION_TOGGLE -> {
                enableService(context, null)
            }
        }
    }

    /**
     * Root varsa erişilebilirlik hizmetini doğrudan açar/kapar.
     * Root yoksa yalnızca desired state'i kaydeder.
     * @param enabled true = aç, false = kapat, null = toggle
     */
    private fun enableService(context: Context, enabled: Boolean?) {
        val pending = goAsync()
        Thread {
            try {
                val target = enabled ?: !AccessibilityControl.isEnabled(context)
                val result = AccessibilityControl.setEnabled(context, target)
                if (result is io.github.omeryol.akisgesture.root.RootResult.Failure) {
                    // Root kullanılamıyor — en azından desired state'i kaydet
                    AccessibilityControl.setDesired(context, target)
                    Log.w(LOG_TAG, "Root unavailable, saved desired state: $target")
                } else {
                    Log.d(LOG_TAG, "Service ${if (target) "enabled" else "disabled"} via root")
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "enableService failed", e)
            } finally {
                pending.finish()
            }
        }.start()
    }

    companion object {
        const val ACTION_START = "io.github.omeryol.akisgesture.action.START"
        const val ACTION_STOP = "io.github.omeryol.akisgesture.action.STOP"
        const val ACTION_TOGGLE = "io.github.omeryol.akisgesture.action.TOGGLE"
        private const val LOG_TAG = "GestureCommandReceiver"
        private const val LEGACY_ACTION_START = "com.openswipe.action.START"
        private const val LEGACY_ACTION_STOP = "com.openswipe.action.STOP"
        private const val LEGACY_ACTION_TOGGLE = "com.openswipe.action.TOGGLE"
    }
}
