package com.omer.akisgesture.service

import android.content.Context
import com.omer.akisgesture.root.RootResult
import java.util.concurrent.TimeUnit

object AccessibilityControl {
    private const val PREFS = "gesture_service_control"
    private const val KEY_DESIRED = "desired_enabled"

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
            current.output.split(':').any { it.equals(component, ignoreCase = true) }
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
            .filter { it.isNotBlank() && !it.equals(component, ignoreCase = true) }
            .toMutableList()
        if (enabled) services += component
        val safeValue = services.joinToString(":")
        val write = runRoot(
            "settings put secure enabled_accessibility_services '${safeValue.replace("'", "")}'",
        )
        if (write !is CommandResult.Success) {
            return RootResult.Failure("Erişilebilirlik durumu değiştirilemedi")
        }
        if (enabled) runRoot("settings put secure accessibility_enabled 1")
        setDesired(context, enabled)
        return RootResult.Success
    }

    fun repairIfNeeded(context: Context): RootResult {
        if (!isDesired(context) || isEnabled(context)) return RootResult.Success
        return setEnabled(context, true)
    }

    private fun componentName(context: Context): String =
        "${context.packageName}/${GestureAccessibilityService::class.java.canonicalName}"

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
