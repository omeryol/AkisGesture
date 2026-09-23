package io.github.omeryol.akisgesture.service

import android.app.Activity
import android.os.Bundle

import android.content.Intent
import io.github.omeryol.akisgesture.automation.AutomationCommand
import io.github.omeryol.akisgesture.automation.AutomationGate

/**
 * Otomasyon araçlarının activity biçiminde gönderebileceği komut girişleri.
 *
 * Bazı otomasyon araçları (örneğin MacroDroid "Intent gönder" adımı) komutu
 * broadcast yerine activity olarak iletir. Aynı action adları bu activity'lerde
 * de ilan edilir; böylece komut her iki teslim biçiminde de çalışır.
 *
 * Activity görünmez bir komut kapısıdır: arayüz göstermeden kapanır ve komutu
 * yalnızca otomasyon anahtarı açıkken uygular.
 */
abstract class GestureCommandActivity : Activity() {

    /** Bu activity'nin temsil ettiği komut. */
    abstract fun requestedCommand(): AutomationCommand.Command

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleCommand()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleCommand()
    }

    private fun handleCommand() {
        val command = requestedCommand()

        // Action taşıyan bir intent, farklı veya bilinmeyen bir komuta işaret
        // ediyorsa komut uygulanmaz.
        val action = intent?.action
        if (action != null && AutomationCommand.fromAction(action) != command) {
            finishAndRemoveTask()
            return
        }

        Thread {
            try {
                if (AutomationGate.isEnabledBlocking(this)) {
                    AccessibilityControl.applyAutomationCommand(this, command)
                }
            } catch (_: Exception) {
            } finally {
                runOnUiThread { finishAndRemoveTask() }
            }
        }.start()
    }
}

class StartGestureActivity : GestureCommandActivity() {
    override fun requestedCommand(): AutomationCommand.Command = AutomationCommand.Command.START
}

class StopGestureActivity : GestureCommandActivity() {
    override fun requestedCommand(): AutomationCommand.Command = AutomationCommand.Command.STOP
}

class ToggleGestureActivity : GestureCommandActivity() {
    override fun requestedCommand(): AutomationCommand.Command = AutomationCommand.Command.TOGGLE
}
