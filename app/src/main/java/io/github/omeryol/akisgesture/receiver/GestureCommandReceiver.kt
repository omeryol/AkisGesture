package io.github.omeryol.akisgesture.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.service.GestureAccessibilityService

/**
 * Tasker / MacroDroid ve dış otomasyon uygulamaları için Broadcast Receiver.
 * Action: "com.openswipe.ACTION_TRIGGER_GESTURE"
 * Extra: "action_id" (ör. "back", "home", "recents", "screenshot", "toggle_flashlight", "split_screen")
 */
class GestureCommandReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(LOG_TAG, "Broadcast received with action: $action")
        when (action) {
            ACTION_START, "io.github.omeryol.akisgesture.action.START" -> {
                io.github.omeryol.akisgesture.service.AccessibilityControl.setDesired(context, true)
            }
            ACTION_STOP, "io.github.omeryol.akisgesture.action.STOP" -> {
                io.github.omeryol.akisgesture.service.AccessibilityControl.setDesired(context, false)
            }
            ACTION_TOGGLE, "io.github.omeryol.akisgesture.action.TOGGLE" -> {
                val current = io.github.omeryol.akisgesture.service.AccessibilityControl.isDesired(context)
                io.github.omeryol.akisgesture.service.AccessibilityControl.setDesired(context, !current)
            }
            ACTION_TRIGGER -> {
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
        const val ACTION_TRIGGER = "com.openswipe.ACTION_TRIGGER_GESTURE"
        const val ACTION_START = "com.openswipe.action.START"
        const val ACTION_STOP = "com.openswipe.action.STOP"
        const val ACTION_TOGGLE = "com.openswipe.action.TOGGLE"
        const val EXTRA_ACTION_ID = "action_id"
        private const val LOG_TAG = "GestureCommandReceiver"

        fun sendGestureFiredBroadcast(context: Context, edge: String, gestureType: String, actionId: String) {
            val broadcastIntent = Intent("com.openswipe.GESTURE_FIRED").apply {
                putExtra("edge", edge)
                putExtra("gesture_type", gestureType)
                putExtra("action_id", actionId)
            }
            context.sendBroadcast(broadcastIntent)
        }
    }
}
