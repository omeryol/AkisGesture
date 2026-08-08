package io.github.omeryol.akisgesture.service

import android.content.Context
import android.content.ComponentName
import android.content.Intent
import io.github.omeryol.akisgesture.root.RootResult
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

object AccessibilityControl {
    private const val PREFS = "gesture_service_control"
    private const val KEY_DESIRED = "desired_enabled"
    private const val KEY_LAST_REPAIR = "last_repair_ms"

    fun isDesired(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_DESIRED, true)

    fun setDesired(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DESIRED, enabled)
            .apply()
    }

    fun isEnabled(context: Context): Boolean {
        val component = componentName(context)
        val current = runRoot("settings get secure enabled_accessibility_services")
        return current is CommandResult.Success &&
            current.output.split(':').any { sameComponent(it, component) }
    }

    fun setEnabled(context: Context, enabled: Boolean): RootResult {
        val component = componentName(context)
        val read = runRoot("settings get secure enabled_accessibility_services")
        if (read !is CommandResult.Success) {
            return RootResult.Failure("Erişilebilirlik listesi okunamadı")
        }
        val services = read.output
            .takeUnless { it == "null" }
            .orEmpty()
            .split(':')
            .filter { it.isNotBlank() && !sameComponent(it, component) }
            .toMutableList()
        if (enabled) services += component
        val safeValue = services.joinToString(":")
        val write = runRoot(
            "settings put secure enabled_accessibility_services '${safeValue.replace("'", "")}'",
        )
        if (write !is CommandResult.Success) {
            return RootResult.Failure("Erişilebilirlik durumu değiştirilemedi")
        }
        if (isEnabled(context) != enabled) {
            return RootResult.Failure("Accessibility setting could not be verified")
        }
        if (enabled) runRoot("settings put secure accessibility_enabled 1")
        setDesired(context, enabled)
        if (!enabled) {
            context.stopService(Intent(context, KeepAliveService::class.java))
        }
        return RootResult.Success
    }

    suspend fun repairIfNeeded(
        context: Context,
        serviceConnected: Boolean = GestureAccessibilityService.instance != null,
        nowMillis: Long = System.currentTimeMillis(),
    ): RootResult {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val lastRepair = prefs.getLong(KEY_LAST_REPAIR, 0L)
        val action = AccessibilityHealthPolicy.decide(
            desired = isDesired(context),
            settingEnabled = isEnabled(context),
            serviceConnected = serviceConnected,
            millisSinceLastRepair = nowMillis - lastRepair,
        )
        if (action == AccessibilityHealthPolicy.Action.NONE) return RootResult.Success
        prefs.edit().putLong(KEY_LAST_REPAIR, nowMillis).apply()
        return when (action) {
            AccessibilityHealthPolicy.Action.ENABLE_SETTING -> setEnabled(context, true)
            AccessibilityHealthPolicy.Action.REBIND_SERVICE -> rebind(context)
            AccessibilityHealthPolicy.Action.NONE -> RootResult.Success
        }
    }

    private suspend fun rebind(context: Context): RootResult {
        val component = componentName(context)
        val read = runRoot("settings get secure enabled_accessibility_services")
        if (read !is CommandResult.Success) {
            return RootResult.Failure("Erişilebilirlik listesi okunamadı")
        }
        val otherServices = read.output
            .takeUnless { it == "null" }
            .orEmpty()
            .split(':')
            .filter { it.isNotBlank() && !sameComponent(it, component) }
        if (!writeServices(otherServices)) {
            return RootResult.Failure("Akış hizmeti yeniden bağlanamadı")
        }
        delay(600)
        if (!writeServices(otherServices + component)) {
            return RootResult.Failure("Akış hizmeti yeniden bağlanamadı")
        }
        if (!isEnabled(context)) {
            return RootResult.Failure("Accessibility rebind could not be verified")
        }
        runRoot("settings put secure accessibility_enabled 1")
        return RootResult.Success
    }

    private fun writeServices(services: List<String>): Boolean {
        val safeValue = services.joinToString(":").replace("'", "")
        return runRoot(
            "settings put secure enabled_accessibility_services '$safeValue'",
        ) is CommandResult.Success
    }

    private fun componentName(context: Context): String =
        ComponentName(context, GestureAccessibilityService::class.java).flattenToString()

    private fun sameComponent(value: String, target: String): Boolean {
        val left = ComponentName.unflattenFromString(value) ?: return false
        val right = ComponentName.unflattenFromString(target) ?: return false
        return left == right
    }

    private fun runRoot(command: String): CommandResult {
        return try {
            val process = ProcessBuilder("su", "-c", command)
                .redirectErrorStream(true)
                .start()
            if (!process.waitFor(4, TimeUnit.SECONDS)) {
                process.destroy()
                CommandResult.Failure
            } else {
                val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
                if (process.exitValue() == 0) CommandResult.Success(output)
                else CommandResult.Failure
            }
        } catch (_: Exception) {
            CommandResult.Failure
        }
    }

    private sealed interface CommandResult {
        data class Success(val output: String) : CommandResult
        data object Failure : CommandResult
    }
}
