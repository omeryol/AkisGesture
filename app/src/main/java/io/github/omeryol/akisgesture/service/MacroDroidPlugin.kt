package io.github.omeryol.akisgesture.service

import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle

class MacroDroidPluginActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val labels = arrayOf(
            getString(io.github.omeryol.akisgesture.R.string.macro_start),
            getString(io.github.omeryol.akisgesture.R.string.macro_stop),
            getString(io.github.omeryol.akisgesture.R.string.macro_toggle),
        )
        val commands = arrayOf("start", "stop", "toggle")
        AlertDialog.Builder(this)
            .setTitle(getString(io.github.omeryol.akisgesture.R.string.macro_title))
            .setItems(labels) { _, which ->
                val bundle = Bundle().apply { putString(KEY_COMMAND, commands[which]) }
                setResult(
                    RESULT_OK,
                    Intent()
                        .putExtra(EXTRA_BUNDLE, bundle)
                        .putExtra(EXTRA_BLURB, getString(io.github.omeryol.akisgesture.R.string.macro_blurb, labels[which])),
                )
                finish()
            }
            .setOnCancelListener { finish() }
            .show()
    }
}

class MacroDroidPluginReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val command = intent.getBundleExtra(EXTRA_BUNDLE)?.getString(KEY_COMMAND) ?: return
        val enabled = when (command) {
            "start" -> true
            "stop" -> false
            "toggle" -> !AccessibilityControl.isEnabled(context)
            else -> return
        }
        val pending = goAsync()
        Thread {
            try {
                AccessibilityControl.setEnabled(context, enabled)
            } finally {
                pending.finish()
            }
        }.start()
    }
}

private const val KEY_COMMAND = "akis_command"
private const val EXTRA_BUNDLE = "com.twofortyfouram.locale.intent.extra.BUNDLE"
private const val EXTRA_BLURB = "com.twofortyfouram.locale.intent.extra.BLURB"
