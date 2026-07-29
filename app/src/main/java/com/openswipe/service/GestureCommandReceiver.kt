package com.omer.akisgesture.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class GestureCommandReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val enabled = when (intent.action) {
            ACTION_START -> true
            ACTION_STOP -> false
            ACTION_TOGGLE -> !AccessibilityControl.isEnabled(context)
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

    companion object {
        const val ACTION_START = "com.omer.akisgesture.action.START"
        const val ACTION_STOP = "com.omer.akisgesture.action.STOP"
        const val ACTION_TOGGLE = "com.omer.akisgesture.action.TOGGLE"
    }
}
