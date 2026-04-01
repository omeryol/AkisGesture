package com.openswipe.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
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

    // Phase 1 保活增强: 1x1px 透明 overlay 窗口，提高进程优先级
    // 参考 GKD A11yService.useAliveOverlayView() 策略
    private var keepAliveView: View? = null

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

        val app = application as OpenSwipeApp

        // 关键：如果规则尚未加载（EMPTY），在此同步加载
        // 学习 STB 的策略：onServiceConnected 必须是完整的自包含初始化
        if (app.compiledRuleSet.value === com.openswipe.rule.CompiledRuleSet.EMPTY) {
            app.ensureRulesLoadedSync()
        }

        val configFlow = app.gestureConfigFlow
        val compiledRuleSetFlow = app.compiledRuleSet
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

        // Phase 1 保活增强: 添加 1x1px 透明 overlay 窗口
        // 持有 TYPE_ACCESSIBILITY_OVERLAY 窗口使系统对进程有更高保护优先级
        // Issue #5: 保活策略持续改进中
        try {
            keepAliveView = View(this).apply {
                setBackgroundColor(Color.TRANSPARENT)
            }
            val keepAliveParams = WindowManager.LayoutParams(
                1, 1,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.START or Gravity.TOP
                x = 0; y = 0
            }
            windowManager.addView(keepAliveView, keepAliveParams)
        } catch (_: Exception) { /* overlay 添加失败不影响核心功能 */ }
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

        // 移除 1x1 保活 overlay 窗口
        keepAliveView?.let { view ->
            try {
                if (view.windowToken != null) windowManager.removeView(view)
            } catch (_: Exception) {}
            keepAliveView = null
        }

        instance = null
        _serviceState.value = ServiceState.DISCONNECTED
        // 注意: 不主动停止 KeepAliveService，让系统自动重启 AccessibilityService
        // 时能重新走 onServiceConnected 流程 (Phase 1 改进 3)
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
