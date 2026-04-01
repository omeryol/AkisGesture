package com.openswipe.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Path
import android.os.Build
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
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
        val compiledRuleSetFlow = (application as OpenSwipeApp).compiledRuleSet
        overlayManager = OverlayManager(this, windowManager)
        actionDispatcher = ActionDispatcherImpl(this)
        gestureEngine = GestureEngine(configFlow, actionDispatcher, overlayManager, compiledRuleSetFlow)

        gestureEngine.start()
        _serviceState.value = ServiceState.CONNECTED

        // 启动前台保活服务 — 关键：防止进程被杀后不恢复
        try {
            ContextCompat.startForegroundService(
                this, Intent(this, KeepAliveService::class.java)
            )
        } catch (_: Exception) { /* 静默处理，不阻塞核心功能 */ }
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

    fun dispatchTap(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 1)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }

    enum class ServiceState { DISCONNECTED, CONNECTED }
}
