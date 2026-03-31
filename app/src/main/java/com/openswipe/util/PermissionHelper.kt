package com.openswipe.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.openswipe.service.GestureAccessibilityService

/**
 * 权限检查工具类。
 */
object PermissionHelper {

    /**
     * 检查无障碍服务是否已启用。
     * 通过 AccessibilityManager 查询已启用的服务列表，匹配自身包名和类名。
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
     * 跳转到系统无障碍设置页面。
     */
    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * 检查应用是否已被加入电池优化白名单。
     */
    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            ?: return false
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * 请求忽略电池优化。
     * 弹出系统对话框让用户确认。
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
