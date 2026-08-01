package io.github.omeryol.akisgesture.action.handler

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import io.github.omeryol.akisgesture.action.ActionResult
import io.github.omeryol.akisgesture.navigation.InternalNavigationBus
import io.github.omeryol.akisgesture.root.RootCommandExecutor
import io.github.omeryol.akisgesture.root.RootResult
import io.github.omeryol.akisgesture.service.GestureAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NavigationActionHandler(
    private val service: GestureAccessibilityService,
    private val rootCommands: RootCommandExecutor,
) {
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
        val targetPkg = service.recentForegroundPackages()
            .firstOrNull { it != service.foregroundPackage() && it != service.packageName }
            ?: service.previousForegroundPackage()
        if (!targetPkg.isNullOrBlank() && targetPkg != service.packageName) {
            val res = launchApp(targetPkg)
            if (res is ActionResult.Success) return ActionResult.Success
        }
        val rootRes = switchRecentTask(1)
        if (rootRes is ActionResult.Success) return ActionResult.Success

        return globalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
    }

    suspend fun handleSwitchNextApp(): ActionResult {
        val rootRes = switchRecentTask(-1)
        if (rootRes is ActionResult.Success) return ActionResult.Success

        return globalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
    }

    private suspend fun switchRecentTask(direction: Int): ActionResult =
        withContext(Dispatchers.IO) {
            when (val result = rootCommands.switchRecentTask(direction)) {
                RootResult.Success -> ActionResult.Success
                is RootResult.Failure -> ActionResult.Failed(result.reason)
            }
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
}
