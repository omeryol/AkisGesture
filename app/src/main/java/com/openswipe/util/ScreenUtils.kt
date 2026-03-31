package com.openswipe.util

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowMetrics

/**
 * 屏幕工具类。
 */
object ScreenUtils {

    fun dpToPx(dp: Float, context: Context): Int {
        return (dp * context.resources.displayMetrics.density + 0.5f).toInt()
    }

    fun pxToDp(px: Float, context: Context): Float {
        return px / context.resources.displayMetrics.density
    }

    fun getScreenWidth(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics: WindowMetrics = wm.currentWindowMetrics
            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars()
            )
            metrics.bounds.width() - insets.left - insets.right
        } else {
            val dm = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(dm)
            dm.widthPixels
        }
    }

    fun getScreenHeight(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics: WindowMetrics = wm.currentWindowMetrics
            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars()
            )
            metrics.bounds.height() - insets.top - insets.bottom
        } else {
            val dm = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(dm)
            dm.heightPixels
        }
    }

    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier(
            "status_bar_height", "dimen", "android"
        )
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            dpToPx(24f, context)
        }
    }

    fun getNavigationBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier(
            "navigation_bar_height", "dimen", "android"
        )
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            dpToPx(48f, context)
        }
    }
}
