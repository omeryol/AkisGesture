package com.openswipe.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.res.Configuration
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.openswipe.OpenSwipeApp
import com.openswipe.action.ActionDispatcher
import com.openswipe.action.ActionDispatcherImpl
import com.openswipe.gesture.GestureEngine
import com.openswipe.overlay.OverlayManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GestureAccessibilityService : AccessibilityService() {

    companion object {
        private val _serviceState = MutableStateFlow(ServiceState.DISCONNECTED)
        val serviceState: StateFlow<ServiceState> = _serviceState.asStateFlow()

        private var instance: GestureAccessibilityService? = null
        fun getInstance(): GestureAccessibilityService? = instance
    }

    private lateinit var windowManager: WindowManager
    private lateinit var overlayManager: OverlayManager
    private lateinit var gestureEngine: GestureEngine
    private lateinit var actionDispatcher: ActionDispatcher
    private var currentForegroundPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        serviceInfo = serviceInfo?.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.DEFAULT
            notificationTimeout = 200L
        }

        val configFlow = (application as OpenSwipeApp).gestureConfigFlow
        overlayManager = OverlayManager(this, windowManager)
        actionDispatcher = ActionDispatcherImpl(this)
        gestureEngine = GestureEngine(configFlow, actionDispatcher, overlayManager)

        gestureEngine.start()
        _serviceState.value = ServiceState.CONNECTED
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            if (pkg != currentForegroundPackage) {
                currentForegroundPackage = pkg
                gestureEngine.onForegroundAppChanged(pkg)
            }
        }
    }

    override fun onInterrupt() {
        // 手势类服务不提供持续反馈，无需中断处理
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        gestureEngine.onConfigurationChanged(newConfig)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        cleanup()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        cleanup()
        super.onDestroy()
    }

    private fun cleanup() {
        if (::gestureEngine.isInitialized) gestureEngine.stop()
        if (::overlayManager.isInitialized) overlayManager.removeAll()
        instance = null
        _serviceState.value = ServiceState.DISCONNECTED
    }

    fun doPerformGlobalAction(actionId: Int): Boolean {
        return performGlobalAction(actionId)
    }

    enum class ServiceState { DISCONNECTED, CONNECTED }
}
