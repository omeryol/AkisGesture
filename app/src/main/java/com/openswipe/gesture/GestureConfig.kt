package com.omer.akisgesture.gesture

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.omer.akisgesture.feedback.FeedbackAnimation
import com.omer.akisgesture.feedback.FeedbackIcon

data class GestureConfig(
    // 触发区域
    val edgeTriggerWidthDp: Float = 20f,
    val bottomTriggerHeightDp: Float = 40f,
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
    val feedbackColorArgb: Int = 0xFF3D5AFE.toInt(),
    val feedbackOpacity: Float = 0.57f,
    val feedbackAnimation: FeedbackAnimation = FeedbackAnimation.FLUID,
    val quickFeedbackIcon: FeedbackIcon = FeedbackIcon.CHEVRON,
    val holdFeedbackIcon: FeedbackIcon = FeedbackIcon.STAR,

    // 长按
    val holdTimeMs: Long = 280L,
) {
    companion object {
        val KEY_LEFT_ENABLED = booleanPreferencesKey("edge_left_enabled")
        val KEY_RIGHT_ENABLED = booleanPreferencesKey("edge_right_enabled")
        val KEY_BOTTOM_ENABLED = booleanPreferencesKey("edge_bottom_enabled")
        val KEY_EDGE_TRIGGER_WIDTH = floatPreferencesKey("edge_trigger_width_dp")
        val KEY_BOTTOM_TRIGGER_HEIGHT = floatPreferencesKey("bottom_trigger_height_dp")
        val KEY_HOLD_TIME = longPreferencesKey("gesture_hold_time_ms")
        val KEY_FEEDBACK_COLOR = intPreferencesKey("feedback_color_argb")
        val KEY_FEEDBACK_OPACITY = floatPreferencesKey("feedback_opacity")
        val KEY_FEEDBACK_ANIMATION = stringPreferencesKey("feedback_animation")
        val KEY_QUICK_FEEDBACK_ICON = stringPreferencesKey("quick_feedback_icon")
        val KEY_HOLD_FEEDBACK_ICON = stringPreferencesKey("hold_feedback_icon")
    }
}
