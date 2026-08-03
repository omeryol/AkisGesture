package io.github.omeryol.akisgesture.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.service.AccessibilityControl
import io.github.omeryol.akisgesture.service.GestureAccessibilityService

/**
 * Tasker / MacroDroid ve dış otomasyon uygulamaları için Broadcast Receiver.
 *
 * START / STOP / TOGGLE intentleri root varsa erişilebilirlik hizmetini
 * doğrudan etkinleştirir/devre dışı bırakır. Root yoksa yalnızca istenilen
 * durumu kaydeder (desired state).
 *
 * TRIGGER_GESTURE intenti:
 *   Extra: "action_id" (ör. "back", "home", "recents", "screenshot" vb.)
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
            ACTION_TRIGGER, LEGACY_ACTION_TRIGGER -> {
                val actionId = intent.getStringExtra(EXTRA_ACTION_ID) ?: return
                when (actionId) {
                    "start_gestures", "start" -> enableService(context, true)
                    "stop_gestures", "stop" -> enableService(context, false)
                    "toggle_gestures", "toggle" -> enableService(context, null)
                    else -> {
                        val actionNode = ActionNode.fromId(actionId) ?: return
                        GestureAccessibilityService.instance?.dispatchActionFromExternal(actionNode)
                    }
                }
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
        const val ACTION_TRIGGER = "io.github.omeryol.akisgesture.action.TRIGGER_GESTURE"
        const val ACTION_START = "io.github.omeryol.akisgesture.action.START"
        const val ACTION_STOP = "io.github.omeryol.akisgesture.action.STOP"
        const val ACTION_TOGGLE = "io.github.omeryol.akisgesture.action.TOGGLE"
        const val EXTRA_ACTION_ID = "action_id"
        private const val LOG_TAG = "GestureCommandReceiver"
        private const val LEGACY_ACTION_TRIGGER = "com.openswipe.ACTION_TRIGGER_GESTURE"
        private const val LEGACY_ACTION_START = "com.openswipe.action.START"
        private const val LEGACY_ACTION_STOP = "com.openswipe.action.STOP"
        private const val LEGACY_ACTION_TOGGLE = "com.openswipe.action.TOGGLE"

        fun sendGestureFiredBroadcast(context: Context, edge: String, gestureType: String, actionId: String) {
            val broadcastIntent = Intent("io.github.omeryol.akisgesture.GESTURE_FIRED").apply {
                putExtra("edge", edge)
                putExtra("gesture_type", gestureType)
                putExtra("action_id", actionId)
            }
            context.sendBroadcast(broadcastIntent)
        }
    }
}
