package io.github.omeryol.akisgesture.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.app.KeyguardManager
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import androidx.core.content.ContextCompat
import io.github.omeryol.akisgesture.AkisGestureApp
import io.github.omeryol.akisgesture.action.ActionDispatcher
import io.github.omeryol.akisgesture.action.ActionDispatcherImpl
import io.github.omeryol.akisgesture.gesture.GestureEngine
import io.github.omeryol.akisgesture.overlay.OverlayManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GestureAccessibilityService : AccessibilityService() {

    companion object {
        private val _serviceState = MutableStateFlow(ServiceState.DISCONNECTED)
        val serviceState: StateFlow<ServiceState> = _serviceState.asStateFlow()

        @JvmStatic
        var instance: GestureAccessibilityService? = null
            internal set

        private val PERMISSION_PACKAGES = setOf(
            "com.android.packageinstaller",
            "com.google.android.packageinstaller",
            "com.miui.packageinstaller",
            "com.android.permissioncontroller",
            "com.google.android.permissioncontroller",
            "com.miui.permissioncontroller",
            "com.miui.securitycenter",
        )
    }

    private lateinit var windowManager: WindowManager
    private lateinit var overlayManager: OverlayManager
    private lateinit var gestureEngine: GestureEngine
    private lateinit var actionDispatcher: ActionDispatcher
    private var currentForegroundPackage: String? = null
    private var previousForegroundPackage: String? = null
    private val foregroundHistory = ArrayDeque<String>()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // 1x1px saydam overlay penceresi — süreç önceliğini korur
    private var keepAliveView: View? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        serviceInfo = serviceInfo?.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.DEFAULT
            notificationTimeout = 200L
        }

        val app = application as AkisGestureApp

        // Kurallar henüz yüklenmediyse senkron yükle
        if (app.compiledRuleSet.value === io.github.omeryol.akisgesture.rule.CompiledRuleSet.EMPTY) {
            app.ensureRulesLoadedSync()
        }

        val configFlow = app.gestureConfigFlow
        val compiledRuleSetFlow = app.compiledRuleSet
        overlayManager = OverlayManager(this, windowManager)
        actionDispatcher = ActionDispatcherImpl(this)
        gestureEngine = GestureEngine(
            configFlow,
            actionDispatcher,
            overlayManager,
            compiledRuleSetFlow,
            app.pausedPackagesFlow,
            app.compiledRuleProfilesFlow,
        )

        _serviceState.value = ServiceState.CONNECTED

        // Ön plan koruma servisi başlat
        try {
            ContextCompat.startForegroundService(
                this, Intent(this, KeepAliveService::class.java)
            )
        } catch (_: Exception) { /* Sessiz işle */ }

        // 1x1px saydam overlay penceresi ekle
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
        } catch (_: Exception) { /* Overlay eklenemezse devam et */ }

        // HyperOS/Android 15 can call onServiceConnected before the accessibility
        // overlay token is fully registered. Starting on the next main-loop turn
        // prevents all sensor windows from failing with BadTokenException.
        Handler(Looper.getMainLooper()).post {
            if (instance === this && ::gestureEngine.isInitialized) {
                gestureEngine.start()
                updateSystemContext()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) {
            updateSystemContext()
        }
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            if (pkg != currentForegroundPackage) {
                currentForegroundPackage
                    ?.takeIf(::isAppHistoryCandidate)
                    ?.let { previousForegroundPackage = it }
                currentForegroundPackage = pkg
                if (isAppHistoryCandidate(pkg)) {
                    foregroundHistory.remove(pkg)
                    foregroundHistory.addFirst(pkg)
                    while (foregroundHistory.size > 8) foregroundHistory.removeLast()
                }
                if (::gestureEngine.isInitialized) {
                    gestureEngine.onForegroundAppChanged(pkg, null)
                }
                serviceScope.launch {
                    val adaptiveColor = extractAppDominantColor(pkg)
                    Handler(Looper.getMainLooper()).post {
                        if (instance === this@GestureAccessibilityService &&
                            currentForegroundPackage == pkg &&
                            ::gestureEngine.isInitialized
                        ) {
                            gestureEngine.onForegroundAppChanged(pkg, adaptiveColor)
                        }
                    }
                }
            }
        }
    }

    private fun extractAppDominantColor(packageName: String): Int? {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val drawable = packageManager.getApplicationIcon(appInfo)
            val bitmap = if (drawable is android.graphics.drawable.BitmapDrawable) {
                drawable.bitmap
            } else {
                val bmp = android.graphics.Bitmap.createBitmap(
                    drawable.intrinsicWidth.coerceAtLeast(1),
                    drawable.intrinsicHeight.coerceAtLeast(1),
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bmp
            }
            var redSum = 0L
            var greenSum = 0L
            var blueSum = 0L
            var count = 0
            val stepX = (bitmap.width / 8).coerceAtLeast(1)
            val stepY = (bitmap.height / 8).coerceAtLeast(1)
            for (x in stepX until bitmap.width - stepX step stepX) {
                for (y in stepY until bitmap.height - stepY step stepY) {
                    val pixel = bitmap.getPixel(x, y)
                    val a = Color.alpha(pixel)
                    if (a > 128) {
                        redSum += Color.red(pixel)
                        greenSum += Color.green(pixel)
                        blueSum += Color.blue(pixel)
                        count++
                    }
                }
            }
            if (count > 0) Color.rgb((redSum / count).toInt(), (greenSum / count).toInt(), (blueSum / count).toInt())
            else null
        } catch (_: Exception) { null }
    }

    override fun onInterrupt() {
        // Kesinti işleyici gerektirmez
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::gestureEngine.isInitialized) {
            gestureEngine.onConfigurationChanged(newConfig)
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        cleanup()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        cleanup()
        super.onDestroy()
    }

    /** Updates the visible edge sensor during map dragging without persisting a setting. */
    fun previewEdgeVerticalRange(edge: io.github.omeryol.akisgesture.overlay.Edge, start: Float, end: Float) {
        if (::gestureEngine.isInitialized) gestureEngine.previewEdgeVerticalRange(edge, start, end)
    }

    private fun cleanup() {
        serviceScope.cancel()
        if (::gestureEngine.isInitialized) gestureEngine.stop()
        if (::overlayManager.isInitialized) overlayManager.removeAll()

        // 1x1 koruma overlay penceresini kaldır
        keepAliveView?.let { view ->
            try {
                if (view.windowToken != null) windowManager.removeView(view)
            } catch (_: Exception) {}
            keepAliveView = null
        }

        instance = null
        _serviceState.value = ServiceState.DISCONNECTED
    }


    fun doPerformGlobalAction(actionId: Int): Boolean {
        return performGlobalAction(actionId)
    }

    private fun updateSystemContext() {
        if (!::gestureEngine.isInitialized) return
        val locked = getSystemService(KeyguardManager::class.java)?.isKeyguardLocked == true
        val keyboardWindow = runCatching {
            windows.firstOrNull { it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD }
        }.getOrNull()
        val keyboard = keyboardWindow != null
        val keyboardTopRatio = if (keyboardWindow != null) {
            val bounds = android.graphics.Rect()
            keyboardWindow.getBoundsInScreen(bounds)
            val displayHeight = resources.displayMetrics.heightPixels
            if (bounds.top > 0 && displayHeight > 0) {
                (bounds.top.toFloat() / displayHeight).coerceIn(0.2f, 1.0f)
            } else 1.0f
        } else 1.0f
        val landscape =
            resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        // Full-screen detection: check if system windows are absent in landscape (e.g. immersive video/games)
        val fullScreen = runCatching {
            val hasSystemBars = windows.any { it.type == AccessibilityWindowInfo.TYPE_SYSTEM }
            !hasSystemBars && landscape && windows.any { it.type == AccessibilityWindowInfo.TYPE_APPLICATION }
        }.getOrDefault(false)
        // Permission screen detection: package installer or permission controller
        val permissionScreen = foregroundPackage() in PERMISSION_PACKAGES

        val fgPkg = foregroundPackage()?.lowercase() ?: ""
        val cameraActive = fgPkg.contains("camera")
        val phoneCallActive = fgPkg.contains("incallui") || fgPkg.contains("telecom") || fgPkg.contains("dialer") || fgPkg == "com.android.phone"

        gestureEngine.onSystemContextChanged(
            locked, keyboard, landscape, fullScreen, permissionScreen, keyboardTopRatio,
            cameraActive = cameraActive, phoneCallActive = phoneCallActive
        )
    }

    fun foregroundPackage(): String? = currentForegroundPackage

    fun foregroundAppPackage(): String? =
        currentForegroundPackage?.takeIf(::isAppHistoryCandidate)
            ?: foregroundHistory.firstOrNull()

    fun recentForegroundPackages(): List<String> = foregroundHistory.toList()

    fun previousForegroundPackage(): String? =
        foregroundHistory.firstOrNull { it != currentForegroundPackage }
            ?: previousForegroundPackage?.takeIf { it != currentForegroundPackage }

    private fun isAppHistoryCandidate(packageName: String): Boolean =
        packageName != this.packageName &&
            packageName != "com.android.systemui" &&
            packageManager.getLaunchIntentForPackage(packageName) != null

    fun dispatchTap(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 1)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }

    enum class ServiceState { DISCONNECTED, CONNECTED }
}
