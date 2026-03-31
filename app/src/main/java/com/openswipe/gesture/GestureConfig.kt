package com.openswipe.gesture

import androidx.datastore.preferences.core.booleanPreferencesKey

data class GestureConfig(
    // 触发区域
    val edgeTriggerWidthDp: Float = 20f,
    val bottomTriggerHeightDp: Float = 48f,
    val leftEnabled: Boolean = true,
    val rightEnabled: Boolean = true,
    val bottomEnabled: Boolean = true,

    // 灵敏度
    val dampingFactor: Float = 5.0f,
    val peakThreshold: Float = 100f,
    val minSwipeThresholdPx: Float = 15f,   // ~5dp at mdpi

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
    }
}
