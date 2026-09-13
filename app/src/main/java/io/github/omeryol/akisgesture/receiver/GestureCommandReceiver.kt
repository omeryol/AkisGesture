package io.github.omeryol.akisgesture.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.omeryol.akisgesture.automation.AutomationCommand
import io.github.omeryol.akisgesture.automation.AutomationGate
import io.github.omeryol.akisgesture.root.RootResult
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
 *
 * Güvenlik: yalnızca ilan edilen action adları kabul edilir ve komut,
 * işlendiği anda otomasyon anahtarı doğrulanarak uygulanır.
 */
class GestureCommandReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Sıkı action doğrulaması: bilinmeyen veya uydurma action adları yok sayılır.
        val command = AutomationCommand.fromAction(intent.action) ?: return

        val pending = goAsync()
        Thread {
            try {
                // Anahtar, komutun uygulandığı anda okunur; bayat durum bilgisine
                // güvenilmez ve varsayılan değer "kapalı"dır.
                if (!AutomationGate.isEnabledBlocking(context)) return@Thread

                val target = AutomationCommand.targetState(
                    command,
                    AccessibilityControl.isEnabled(context),
                )
                val result = AccessibilityControl.setEnabled(context, target)
                if (result is RootResult.Failure) {
                    // Root kullanılamıyor — en azından desired state'i kaydet
                    AccessibilityControl.setDesired(context, target)
                }
            } catch (_: Exception) {
            } finally {
                pending.finish()
            }
        }.start()
    }
}
