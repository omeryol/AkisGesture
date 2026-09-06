package io.github.omeryol.akisgesture.service

import android.app.Activity
import android.os.Bundle

import android.content.Intent
import io.github.omeryol.akisgesture.AkisGestureApp

abstract class GestureCommandActivity : Activity() {
    abstract fun requestedState(): Boolean?

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
        val app = applicationContext as? AkisGestureApp
        val automationEnabled = app?.gestureConfigFlow?.value?.automationAppsEnabled == true
        if (!automationEnabled) {
            finishAndRemoveTask()
            return
        }

        val state = requestedState()
        Thread {
            try {
                if (state == null) {
                    AccessibilityControl.setEnabled(this, !AccessibilityControl.isEnabled(this))
                } else {
                    AccessibilityControl.setEnabled(this, state)
                }
            } finally {
                runOnUiThread { finishAndRemoveTask() }
            }
        }.start()
    }
}

class StartGestureActivity : GestureCommandActivity() {
    override fun requestedState(): Boolean = true
}

class StopGestureActivity : GestureCommandActivity() {
    override fun requestedState(): Boolean = false
}

class ToggleGestureActivity : GestureCommandActivity() {
    override fun requestedState(): Boolean? = null
}
