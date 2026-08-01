package io.github.omeryol.akisgesture.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.service.GestureAccessibilityService

/**
 * Tasker / MacroDroid ve dış otomasyon uygulamaları için Broadcast Receiver.
 * Action: "io.github.omeryol.akisgesture.action.TRIGGER_GESTURE"
 * Extra: "action_id" (ör. "back", "home", "recents", "screenshot", "toggle_flashlight", "split_screen")
 */
class GestureCommandReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(LOG_TAG, "Broadcast received with action: $action")
        when (action) {
            ACTION_START, LEGACY_ACTION_START -> {
                io.github.omeryol.akisgesture.service.AccessibilityControl.setDesired(context, true)
            }
            ACTION_STOP, LEGACY_ACTION_STOP -> {
                io.github.omeryol.akisgesture.service.AccessibilityControl.setDesired(context, false)
            }
            ACTION_TOGGLE, LEGACY_ACTION_TOGGLE -> {
                val current = io.github.omeryol.akisgesture.service.AccessibilityControl.isDesired(context)
                io.github.omeryol.akisgesture.service.AccessibilityControl.setDesired(context, !current)
            }
            ACTION_TRIGGER, LEGACY_ACTION_TRIGGER -> {
                val actionId = intent.getStringExtra(EXTRA_ACTION_ID) ?: return
                when (actionId) {
                    "start_gestures", "start" -> io.github.omeryol.akisgesture.service.AccessibilityControl.setDesired(context, true)
                    "stop_gestures", "stop" -> io.github.omeryol.akisgesture.service.AccessibilityControl.setDesired(context, false)
                    "toggle_gestures", "toggle" -> {
                        val current = io.github.omeryol.akisgesture.service.AccessibilityControl.isDesired(context)
                        io.github.omeryol.akisgesture.service.AccessibilityControl.setDesired(context, !current)
                    }
                    else -> {
                        val actionNode = ActionNode.fromId(actionId) ?: return
                        GestureAccessibilityService.instance?.dispatchActionFromExternal(actionNode)
                    }
                }
            }
        }
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
