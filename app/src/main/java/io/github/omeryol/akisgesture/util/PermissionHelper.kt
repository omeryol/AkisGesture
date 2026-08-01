package io.github.omeryol.akisgesture.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import io.github.omeryol.akisgesture.service.GestureAccessibilityService

/**
 * İzin kontrol yardımcı sınıfı.
 */
object PermissionHelper {

    /**
     * Erişilebilirlik hizmetinin etkin olup olmadığını kontrol eder.
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )
        val targetClassName = GestureAccessibilityService::class.java.name
        return enabledServices.any { info ->
            info.resolveInfo?.serviceInfo?.let { serviceInfo ->
                serviceInfo.packageName == context.packageName &&
                    serviceInfo.name == targetClassName
            } == true
        }
    }

    /**
     * Sistem erişilebilirlik ayarları sayfasını açar.
     */
    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Uygulamanın pil optimizasyonundan muaf olup olmadığını kontrol eder.
     */
    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            ?: return false
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * Pil kısıtlamasını kaldırma isteği gönderir.
     */
    fun requestIgnoreBatteryOptimization(context: Context) {
        if (isBatteryOptimizationIgnored(context)) return
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
