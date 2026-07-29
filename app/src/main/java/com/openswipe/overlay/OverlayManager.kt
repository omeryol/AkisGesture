package com.omer.akisgesture.overlay

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.util.Log

class OverlayManager(
    val context: Context,
    private val windowManager: WindowManager,
) {
    private val windows = mutableMapOf<String, OverlayWindow>()

    fun addWindow(tag: String, window: OverlayWindow) {
        if (windows.containsKey(tag)) return
        try {
            windowManager.addView(window.view, window.params)
            windows[tag] = window
        } catch (e: Exception) {
            Log.e("AkisGesture", "overlay_add_failed tag=$tag", e)
        }
    }

    fun removeWindow(tag: String) {
        windows.remove(tag)?.let { window ->
            try {
                if (window.view.windowToken != null) {
                    windowManager.removeView(window.view)
                }
            } catch (_: Exception) {}
        }
    }

    fun updateWindow(tag: String) {
        windows[tag]?.let { window ->
            try {
                windowManager.updateViewLayout(window.view, window.params)
            } catch (_: Exception) {}
        }
    }

    fun removeAll() {
        windows.keys.toList().forEach { removeWindow(it) }
    }
}

data class OverlayWindow(
    val view: View,
    val params: LayoutParams,
)

enum class Edge { LEFT, RIGHT, BOTTOM }
