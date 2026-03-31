package com.openswipe.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.TextUtils

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            // 检查无障碍服务是否已启用，如果是则启动保活服务
            if (isAccessibilityServiceEnabled(context)) {
                val serviceIntent = Intent(context, KeepAliveService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = "${context.packageName}/${GestureAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(expectedComponentName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
