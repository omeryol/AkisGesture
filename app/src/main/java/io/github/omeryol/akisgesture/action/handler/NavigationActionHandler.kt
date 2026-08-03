package io.github.omeryol.akisgesture.action.handler

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import io.github.omeryol.akisgesture.action.ActionResult
import io.github.omeryol.akisgesture.navigation.InternalNavigationBus
import io.github.omeryol.akisgesture.service.GestureAccessibilityService
import android.os.SystemClock

class NavigationActionHandler(
    private val service: GestureAccessibilityService,
) {
    private var switchSession = emptyList<String>()
    private var switchIndex = 0
    private var expectedPackage: String? = null
    private var lastSwitchAt = 0L
    fun handleBack(): ActionResult {
        return if (service.foregroundPackage() == service.packageName) {
            if (InternalNavigationBus.requestBack()) ActionResult.Success
            else globalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        } else {
            globalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }
    }

    fun handleHome(): ActionResult = globalAction(AccessibilityService.GLOBAL_ACTION_HOME)

    fun handleRecents(): ActionResult = globalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)

    suspend fun handleSwitchLastApp(): ActionResult {
        val rootExecutor = io.github.omeryol.akisgesture.root.RootCommandExecutor(service)
        val rootResult = rootExecutor.switchRecentTask(1)
        if (rootResult is io.github.omeryol.akisgesture.root.RootResult.Success) {
            return ActionResult.Success
        }
        return switchApp(delta = 1)
    }

    suspend fun handleSwitchNextApp(): ActionResult {
        val rootExecutor = io.github.omeryol.akisgesture.root.RootCommandExecutor(service)
        val rootResult = rootExecutor.switchRecentTask(-1)
        if (rootResult is io.github.omeryol.akisgesture.root.RootResult.Success) {
            return ActionResult.Success
        }
        return switchApp(delta = -1)
    }

    private fun switchApp(delta: Int): ActionResult {
        val current = service.foregroundPackage()
        val now = SystemClock.elapsedRealtime()

        // 1. Resolve candidate packages list
        var session = switchSession
        val sessionInvalid = session.isEmpty() ||
            now - lastSwitchAt > SWITCH_SESSION_TIMEOUT_MS ||
            (expectedPackage != null && current != expectedPackage)

        if (sessionInvalid) {
            val history = service.recentForegroundPackages()
                .filter { it != service.packageName && service.packageManager.getLaunchIntentForPackage(it) != null }
                .distinct()

            session = if (history.size >= 2) {
                history
            } else {
                val prev = service.previousForegroundPackage()
                val list = mutableListOf<String>()
                if (current != null && current != service.packageName) list.add(current)
                if (prev != null && prev != current && prev != service.packageName) list.add(prev)
                list
            }
            switchSession = session
            switchIndex = if (current != null) session.indexOf(current).coerceAtLeast(0) else 0
        }

        if (session.isEmpty()) {
            // No app history yet — fallback to Recents screen
            return globalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
        }

        // 2. Cyclic target index calculation
        val targetIndex = if (session.size > 1) {
            (switchIndex + delta + session.size) % session.size
        } else 0

        val targetPackage = session.getOrNull(targetIndex)
            ?: service.previousForegroundPackage()
            ?: return globalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)

        if (targetPackage == current && session.size > 1) {
            // Target is same as current — try previous package fallback
            val altPackage = service.previousForegroundPackage()?.takeIf { it != current }
            if (altPackage != null) {
                val altResult = launchApp(altPackage)
                if (altResult is ActionResult.Success) {
                    expectedPackage = altPackage
                    lastSwitchAt = now
                    return altResult
                }
            }
        }

        val result = launchApp(targetPackage)
        if (result is ActionResult.Success) {
            switchIndex = targetIndex
            expectedPackage = targetPackage
            lastSwitchAt = now
        } else {
            // Fallback to Recents if launch app failed
            return globalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
        }
        return result
    }

    private fun launchApp(pkg: String): ActionResult = try {
        val launchIntent = service.packageManager.getLaunchIntentForPackage(pkg)
            ?: return ActionResult.Failed("Uygulamanın açılış ekranı bulunamadı")
        
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

        val pendingIntent = android.app.PendingIntent.getActivity(
            service,
            0,
            launchIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        if (android.os.Build.VERSION.SDK_INT >= 34) { // Android 14+
            val options = android.app.ActivityOptions.makeBasic()
            options.setPendingIntentBackgroundActivityStartMode(android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
            pendingIntent.send(service, 0, null, null, null, null, options.toBundle())
        } else {
            pendingIntent.send()
        }
        ActionResult.Success
    } catch (_: Exception) {
        try {
            val launchIntent = service.packageManager.getLaunchIntentForPackage(pkg)
                ?: return ActionResult.Failed("Uygulamanın açılış ekranı bulunamadı")
            service.startActivity(launchIntent.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
            ActionResult.Success
        } catch (e: Exception) {
            ActionResult.Failed(e.message ?: "Uygulama açılamadı")
        }
    }

    private fun globalAction(id: Int): ActionResult {
        return if (service.doPerformGlobalAction(id)) ActionResult.Success
        else ActionResult.Failed("performGlobalAction($id) returned false")
    }

    companion object {
        private const val SWITCH_SESSION_TIMEOUT_MS = 15_000L
    }
}
