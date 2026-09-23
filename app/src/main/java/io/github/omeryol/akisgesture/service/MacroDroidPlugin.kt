package io.github.omeryol.akisgesture.service

import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.github.omeryol.akisgesture.R
import io.github.omeryol.akisgesture.automation.AutomationCommand
import io.github.omeryol.akisgesture.automation.AutomationGate
import io.github.omeryol.akisgesture.automation.LocalePluginProtocol

/**
 * MacroDroid ve Tasker (Locale eklenti protokolü) girişleri.
 *
 * Eklenti yalnızca otomasyon anahtarı açıkken yapılandırılabilir ve
 * tetiklenebilir. Anahtar kapalıyken bu bileşenler `PackageManager` üzerinden
 * devre dışı bırakıldığı için otomasyon uygulaması eklentiyi hiç görmez;
 * buradaki kontroller ikinci savunma katmanıdır.
 */
class MacroDroidPluginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Güvenli varsayılan: seçim yapılmadan çıkılırsa yapılandırma iptal edilir.
        setResult(RESULT_CANCELED)

        Thread {
            val enabled = runCatching { AutomationGate.isEnabledBlocking(this) }.getOrDefault(false)
            val preselected = if (enabled) configuredCommand() else null
            runOnUiThread {
                if (enabled) {
                    showPicker(preselected)
                } else {
                    finish()
                }
            }
        }.start()
    }

    private fun showPicker(preselected: AutomationCommand.Command?) {
        val labels = COMMANDS.map { getString(labelOf(it)) }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.macro_title))
            .setSingleChoiceItems(labels, COMMANDS.indexOf(preselected)) { dialog, which ->
                val command = COMMANDS[which]
                setResult(RESULT_OK, resultIntent(command))
                dialog.dismiss()
                finish()
            }
            .setOnCancelListener { finish() }
            .show()
    }

    /** Eklentiyi yeniden düzenlerken mevcut seçimi okur. */
    private fun configuredCommand(): AutomationCommand.Command? {
        val bundle = intent?.getBundleExtra(LocalePluginProtocol.EXTRA_BUNDLE) ?: return null
        if (!LocalePluginProtocol.isAcceptedBundle(bundle.keySet())) return null
        return AutomationCommand.fromPluginValue(bundle.getString(LocalePluginProtocol.KEY_COMMAND))
    }

    private fun resultIntent(command: AutomationCommand.Command): Intent {
        val bundle = Bundle().apply {
            putString(LocalePluginProtocol.KEY_COMMAND, AutomationCommand.pluginValueOf(command))
        }
        return Intent()
            .putExtra(LocalePluginProtocol.EXTRA_BUNDLE, bundle)
            .putExtra(
                LocalePluginProtocol.EXTRA_BLURB,
                getString(R.string.macro_blurb, getString(labelOf(command))),
            )
    }

    private fun labelOf(command: AutomationCommand.Command): Int = when (command) {
        AutomationCommand.Command.START -> R.string.macro_start
        AutomationCommand.Command.STOP -> R.string.macro_stop
        AutomationCommand.Command.TOGGLE -> R.string.macro_toggle
    }

    private companion object {
        val COMMANDS = listOf(
            AutomationCommand.Command.START,
            AutomationCommand.Command.STOP,
            AutomationCommand.Command.TOGGLE,
        )
    }
}

/**
 * Otomasyon uygulamasının eklenti adımını tetiklediği nokta.
 *
 * Yalnızca protokolün ateşleme action'ı (veya action taşımayan açık bileşen
 * çağrısı) kabul edilir; paket içeriği bilinen tek anahtarla sınırlıdır.
 */
class MacroDroidPluginReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != null && action != LocalePluginProtocol.ACTION_FIRE_SETTING) return

        val bundle = intent.getBundleExtra(LocalePluginProtocol.EXTRA_BUNDLE) ?: return
        if (!LocalePluginProtocol.isAcceptedBundle(bundle.keySet())) return
        val command = AutomationCommand.fromPluginValue(
            bundle.getString(LocalePluginProtocol.KEY_COMMAND),
        ) ?: return

        val pending = goAsync()
        Thread {
            try {
                if (!AutomationGate.isEnabledBlocking(context)) return@Thread
                AccessibilityControl.applyAutomationCommand(context, command)
            } catch (_: Exception) {
            } finally {
                pending.finish()
            }
        }.start()
    }
}
