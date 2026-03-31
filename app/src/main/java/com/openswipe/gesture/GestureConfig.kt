package com.openswipe.gesture

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

data class GestureConfig(
    // 触发区域
    val edgeTriggerWidthDp: Float = 20f,
    val bottomTriggerHeightDp: Float = 48f,
    val bottomTriggerMode: BottomTriggerMode = BottomTriggerMode.TOUCH,
    val leftEnabled: Boolean = true,
    val rightEnabled: Boolean = true,
    val bottomEnabled: Boolean = true,

    // 灵敏度
    val dampingFactor: Float = 2.0f,
    val peakThreshold: Float = 200f,
    val minSwipeThresholdPx: Float = 30f,

    // 分区
    val sectionCount: Int = 1,
    val sensorLength: Float = 0f,

    // 反馈
    val hapticEnabled: Boolean = true,

    // 长按
    val holdTimeMs: Long = 500L,
) {
    companion object {
        val KEY_LEFT_ENABLED = booleanPreferencesKey("edge_left_enabled")
        val KEY_RIGHT_ENABLED = booleanPreferencesKey("edge_right_enabled")
        val KEY_BOTTOM_ENABLED = booleanPreferencesKey("edge_bottom_enabled")
        val KEY_BOTTOM_TRIGGER_HEIGHT = floatPreferencesKey("bottom_trigger_height_dp")
        val KEY_BOTTOM_TRIGGER_MODE = stringPreferencesKey("bottom_trigger_mode")
    }
}

enum class BottomTriggerMode {
    TOUCH,
    SWIPE,
}
