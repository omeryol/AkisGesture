package io.github.omeryol.akisgesture.service

import android.app.Activity
import android.os.Bundle

abstract class GestureCommandActivity : Activity() {
    abstract fun requestedState(): Boolean?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val state = requestedState()
        Thread {
            if (state == null) {
                AccessibilityControl.setEnabled(this, !AccessibilityControl.isEnabled(this))
            } else {
                AccessibilityControl.setEnabled(this, state)
            }
            runOnUiThread { finishAndRemoveTask() }
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
