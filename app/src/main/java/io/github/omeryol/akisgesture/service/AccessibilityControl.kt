package io.github.omeryol.akisgesture.service

import android.content.Context
import android.content.ComponentName
import android.content.Intent
import io.github.omeryol.akisgesture.root.RootResult
import io.github.omeryol.akisgesture.diagnostics.RuntimeDiagnostics
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
        val services = getEnabledServicesString(context) ?: return false
        return services.split(':').any { it.isNotBlank() && sameComponent(it, component) }
    }

    private fun getEnabledServicesString(context: Context): String? {
        val resolverSetting = runCatching {
            android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            )
        }.getOrNull()
        if (resolverSetting != null) {
            return resolverSetting
        }
        val current = runRoot("settings get secure enabled_accessibility_services")
        return if (current is CommandResult.Success) current.output else null
    }

    fun setEnabled(context: Context, enabled: Boolean): RootResult {
        val component = componentName(context)
        val watchdogEnabled = (context.applicationContext as? io.github.omeryol.akisgesture.AkisGestureApp)
            ?.gestureConfigFlow?.value?.rootWatchdogEnabled == true
        val read = getEnabledServicesString(context)
        if (read == null) {
            return RootResult.Failure("Erişilebilirlik listesi okunamadı")
        }
        val services = read
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
        // With the watchdog enabled, an in-app stop is treated as a temporary
        // interruption. Keep the guard service alive so it can restore the
        // accessibility entry at the configured interval.
        setDesired(context, enabled || watchdogEnabled)
        if (!enabled && !watchdogEnabled) {
            context.stopService(Intent(context, KeepAliveService::class.java))
        }
        return RootResult.Success
    }

    suspend fun repairIfNeeded(
        context: Context,
        serviceConnected: Boolean = GestureAccessibilityService.instance != null,
        nowMillis: Long = System.currentTimeMillis(),
        repairCooldownMs: Long = AccessibilityHealthPolicy.REPAIR_COOLDOWN_MS,
    ): RootResult {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val lastRepair = prefs.getLong(KEY_LAST_REPAIR, 0L)
        val action = AccessibilityHealthPolicy.decide(
            desired = isDesired(context),
            settingEnabled = isEnabled(context),
            serviceConnected = serviceConnected,
            millisSinceLastRepair = nowMillis - lastRepair,
            repairCooldownMs = repairCooldownMs,
        )
        if (action == AccessibilityHealthPolicy.Action.NONE) return RootResult.Success
        prefs.edit().putLong(KEY_LAST_REPAIR, nowMillis).apply()
        val result = when (action) {
            AccessibilityHealthPolicy.Action.ENABLE_SETTING -> setEnabled(context, true)
            AccessibilityHealthPolicy.Action.REBIND_SERVICE -> rebind(context)
            AccessibilityHealthPolicy.Action.NONE -> RootResult.Success
        }
        RuntimeDiagnostics.repairFinished(action.name, result)
        return result
    }

    private suspend fun rebind(context: Context): RootResult {
        val component = componentName(context)
        val read = getEnabledServicesString(context)
        if (read == null) {
            return RootResult.Failure("Erişilebilirlik listesi okunamadı")
        }
        val otherServices = read
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
        val rootResult = try {
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
        if (rootResult is CommandResult.Success) return rootResult

        // Shizuku fallback
        if (io.github.omeryol.akisgesture.shizuku.ShizukuManager.hasPermission()) {
            val shizukuOutput = io.github.omeryol.akisgesture.shizuku.ShizukuManager.executeShell(command)
            if (shizukuOutput != null) {
                return CommandResult.Success(shizukuOutput)
            }
        }

        return CommandResult.Failure
    }

    private sealed interface CommandResult {
        data class Success(val output: String) : CommandResult
        data object Failure : CommandResult
    }
}
