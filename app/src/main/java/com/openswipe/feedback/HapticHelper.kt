package com.openswipe.feedback

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * 振动反馈工具类。
 *
 * 三个触发时机（来自 STB/XDA 分析）：
 * 1. 触摸进入触发区域 → LIGHT
 * 2. 跨越 halfPeak 阈值（单→双箭头）→ MEDIUM
 * 3. 释放执行动作 → HEAVY
 */
object HapticHelper {

    enum class HapticType {
        /** 轻触觉 - 手势开始 */
        LIGHT,
        /** 中等触觉 - 跨越阈值 */
        MEDIUM,
        /** 重触觉 - 动作确认 */
        HEAVY,
    }

    /**
     * 通过 View 的 haptic feedback 执行振动。
     * 优先使用系统 HapticFeedbackConstants，兼容性最好。
     */
    fun performHaptic(view: View, type: HapticType) {
        val constant = when (type) {
            HapticType.LIGHT -> {
                if (Build.VERSION.SDK_INT >= 30) {
                    HapticFeedbackConstants.GESTURE_START
                } else {
                    HapticFeedbackConstants.CLOCK_TICK
                }
            }
            HapticType.MEDIUM -> HapticFeedbackConstants.CLOCK_TICK
            HapticType.HEAVY -> {
                if (Build.VERSION.SDK_INT >= 30) {
                    HapticFeedbackConstants.CONFIRM
                } else {
                    HapticFeedbackConstants.LONG_PRESS
                }
            }
        }
        view.performHapticFeedback(constant, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
    }

    /**
     * 通过 Vibrator 直接振动（当没有 View 可用时）。
     * XDA 的自定义振动方案。
     */
    fun performHaptic(context: Context, type: HapticType) {
        val vibrator = getVibrator(context) ?: return
        val (durationMs, amplitude) = when (type) {
            HapticType.LIGHT -> 20L to 50
            HapticType.MEDIUM -> 30L to 120
            HapticType.HEAVY -> 50L to 200
        }
        vibrate(vibrator, durationMs, amplitude)
    }

    private fun vibrate(vibrator: Vibrator, durationMs: Long, amplitude: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    durationMs,
                    amplitude.coerceIn(1, 255)
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
