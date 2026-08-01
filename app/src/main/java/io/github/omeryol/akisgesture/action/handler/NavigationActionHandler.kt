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

    suspend fun handleSwitchLastApp(): ActionResult = switchApp(delta = 1)

    suspend fun handleSwitchNextApp(): ActionResult = switchApp(delta = -1)

    private fun switchApp(delta: Int): ActionResult {
        val current = service.foregroundPackage()
        val now = SystemClock.elapsedRealtime()
        val sessionInvalid = switchSession.isEmpty() ||
            now - lastSwitchAt > SWITCH_SESSION_TIMEOUT_MS ||
            (expectedPackage != null && current != expectedPackage)

        if (sessionInvalid) {
            switchSession = service.recentForegroundPackages()
                .asSequence()
                .filter { it != service.packageName }
                .distinct()
                .toList()
            switchIndex = switchSession.indexOf(current).takeIf { it >= 0 } ?: 0
        }

        val targetIndex = switchIndex + delta
        val targetPackage = switchSession.getOrNull(targetIndex)
            ?: return ActionResult.Failed(
                if (delta > 0) "Önceki uygulama bulunamadı" else "Sonraki uygulama bulunamadı",
            )
        val result = launchApp(targetPackage)
        if (result is ActionResult.Success) {
            switchIndex = targetIndex
            expectedPackage = targetPackage
            lastSwitchAt = now
        }
        return result
    }

    private fun launchApp(pkg: String): ActionResult = try {
        val launchIntent = service.packageManager.getLaunchIntentForPackage(pkg)
            ?: return ActionResult.Failed("Uygulamanın açılış ekranı bulunamadı")
        service.startActivity(launchIntent.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        })
        ActionResult.Success
    } catch (e: Exception) {
        ActionResult.Failed(e.message ?: "Uygulama açılamadı")
    }

    private fun globalAction(id: Int): ActionResult {
        return if (service.doPerformGlobalAction(id)) ActionResult.Success
        else ActionResult.Failed("performGlobalAction($id) returned false")
    }

    companion object {
        private const val SWITCH_SESSION_TIMEOUT_MS = 15_000L
    }
}
