package io.github.omeryol.akisgesture.gesture

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.omeryol.akisgesture.feedback.FeedbackAnimation
import io.github.omeryol.akisgesture.feedback.FeedbackIcon

enum class HoldFireMode(val label: String) {
    ON_RELEASE("Parmak kalkınca"),
    ON_THRESHOLD("Eşik geçilince"),
}

data class GestureConfig(
    // Per-edge trigger width/height in dp
    val leftTriggerWidthDp: Float = 20f,
    val rightTriggerWidthDp: Float = 20f,
    val bottomTriggerHeightDp: Float = 40f,
    val leftEnabled: Boolean = true,
    val rightEnabled: Boolean = true,
    val bottomEnabled: Boolean = true,

    // Legacy — kept for backward compat
    @Deprecated("Use leftTriggerWidthDp / rightTriggerWidthDp / bottomTriggerHeightDp")
    val edgeTriggerWidthDp: Float = 20f,

    // Per-edge sensitivity (damping factor per edge, higher = less sensitive)
    val leftDamping: Float = 2.0f,
    val rightDamping: Float = 2.0f,
    val bottomDamping: Float = 2.0f,

    // Per-edge swipe threshold in dp (converted to px at runtime)
    val leftSwipeThresholdDp: Float = 14f,
    val rightSwipeThresholdDp: Float = 14f,
    val bottomSwipeThresholdDp: Float = 14f,

    // Vertical offset for side edges (0 = top, 1 = bottom portion of screen)
    // Range 0..1, represents the fraction of screen height the trigger occupies
    val leftVerticalStart: Float = 0f,
    val leftVerticalEnd: Float = 1f,
    val rightVerticalStart: Float = 0f,
    val rightVerticalEnd: Float = 1f,

    // Direction accuracy and L-swipe threshold
    val directionToleranceDegrees: Float = 35f,
    val hysteresisRatio: Float = 0.25f,
    val lSwipeThresholdDp: Float = 30f,

    // Hold behavior
    val holdTimeMs: Long = 280L,
    val holdFireMode: HoldFireMode = HoldFireMode.ON_RELEASE,

    // Bölümleme
    val sectionCount: Int = 1,
    val sensorLength: Float = 0f,

    // Görsel ve dokunsal geri bildirim
    val hapticEnabled: Boolean = true,
    val hapticIntensity: Float = 1f,        // 0..1, 1 = tam şiddet
    val hapticSoundEnabled: Boolean = false, // tıklama sesi
    val animationSpeed: Float = 1f,          // 0.5..2.0 animasyon hız çarpanı
    val animationSize: Float = 1f,           // 0.5..2.0 animasyon boyut çarpanı
    val feedbackColorArgb: Int = 0xFF3D5AFE.toInt(),
    val secondaryColorArgb: Int = 0xFFFF9100.toInt(),
    val lSwipeColorArgb: Int = 0xFF00E676.toInt(),
    val feedbackOpacity: Float = 0.57f,
    val feedbackAnimation: FeedbackAnimation = FeedbackAnimation.OCEAN_WAVE,
    val quickFeedbackIcon: FeedbackIcon = FeedbackIcon.CHEVRON,
    val holdFeedbackIcon: FeedbackIcon = FeedbackIcon.STAR,

    // Dynamic Adaptive Colors & Indicators
    val useAppAdaptiveColor: Boolean = false,
    val showGestureIndicatorBar: Boolean = false,
    val indicatorBarOpacity: Float = 0.4f,

    // Çalışmayacağı yerler
    val pauseOnLockScreen: Boolean = true,
    val pauseWhenKeyboardVisible: Boolean = false,
    val pauseInLandscape: Boolean = false,
    val pauseOnFullScreen: Boolean = true,
    val pauseOnPermissionScreen: Boolean = true,
) {
    /** Get damping factor for a specific edge. */
    fun dampingFor(edge: io.github.omeryol.akisgesture.overlay.Edge): Float = when (edge) {
        io.github.omeryol.akisgesture.overlay.Edge.LEFT -> leftDamping
        io.github.omeryol.akisgesture.overlay.Edge.RIGHT -> rightDamping
        io.github.omeryol.akisgesture.overlay.Edge.BOTTOM -> bottomDamping
    }

    /** Get swipe threshold in px for a specific edge (caller must supply dp→px value). */
    fun swipeThresholdDpFor(edge: io.github.omeryol.akisgesture.overlay.Edge): Float = when (edge) {
        io.github.omeryol.akisgesture.overlay.Edge.LEFT -> leftSwipeThresholdDp
        io.github.omeryol.akisgesture.overlay.Edge.RIGHT -> rightSwipeThresholdDp
        io.github.omeryol.akisgesture.overlay.Edge.BOTTOM -> bottomSwipeThresholdDp
    }

    /** Get vertical range for side edge. Returns null for bottom edge. */
    fun verticalRangeFor(edge: io.github.omeryol.akisgesture.overlay.Edge): Pair<Float, Float>? = when (edge) {
        io.github.omeryol.akisgesture.overlay.Edge.LEFT -> leftVerticalStart to leftVerticalEnd
        io.github.omeryol.akisgesture.overlay.Edge.RIGHT -> rightVerticalStart to rightVerticalEnd
        io.github.omeryol.akisgesture.overlay.Edge.BOTTOM -> null
    }

    /** Get trigger width/height in dp for a specific edge. */
    fun triggerSizeDpFor(edge: io.github.omeryol.akisgesture.overlay.Edge): Float = when (edge) {
        io.github.omeryol.akisgesture.overlay.Edge.LEFT -> leftTriggerWidthDp
        io.github.omeryol.akisgesture.overlay.Edge.RIGHT -> rightTriggerWidthDp
        io.github.omeryol.akisgesture.overlay.Edge.BOTTOM -> bottomTriggerHeightDp
    }

    companion object {
        val KEY_LEFT_ENABLED = booleanPreferencesKey("edge_left_enabled")
        val KEY_RIGHT_ENABLED = booleanPreferencesKey("edge_right_enabled")
        val KEY_BOTTOM_ENABLED = booleanPreferencesKey("edge_bottom_enabled")
        val KEY_LEFT_TRIGGER_WIDTH = floatPreferencesKey("left_trigger_width_dp")
        val KEY_RIGHT_TRIGGER_WIDTH = floatPreferencesKey("right_trigger_width_dp")
        @Deprecated("Use KEY_LEFT_TRIGGER_WIDTH / KEY_RIGHT_TRIGGER_WIDTH")
        val KEY_EDGE_TRIGGER_WIDTH = floatPreferencesKey("edge_trigger_width_dp")
        val KEY_BOTTOM_TRIGGER_HEIGHT = floatPreferencesKey("bottom_trigger_height_dp")
        val KEY_HOLD_TIME = longPreferencesKey("gesture_hold_time_ms")
        val KEY_HOLD_FIRE_MODE = stringPreferencesKey("hold_fire_mode")
        val KEY_FEEDBACK_COLOR = intPreferencesKey("feedback_color_argb")
        val KEY_SECONDARY_COLOR = intPreferencesKey("secondary_color_argb")
        val KEY_L_SWIPE_COLOR = intPreferencesKey("l_swipe_color_argb")
        val KEY_USE_APP_ADAPTIVE_COLOR = booleanPreferencesKey("use_app_adaptive_color")
        val KEY_FEEDBACK_OPACITY = floatPreferencesKey("feedback_opacity")
        val KEY_FEEDBACK_ANIMATION = stringPreferencesKey("feedback_animation")
        val KEY_QUICK_FEEDBACK_ICON = stringPreferencesKey("quick_feedback_icon")
        val KEY_HOLD_FEEDBACK_ICON = stringPreferencesKey("hold_feedback_icon")
        val KEY_PAUSE_ON_LOCK_SCREEN = booleanPreferencesKey("pause_on_lock_screen")
        val KEY_PAUSE_WHEN_KEYBOARD_VISIBLE = booleanPreferencesKey("pause_when_keyboard_visible")
        val KEY_PAUSE_IN_LANDSCAPE = booleanPreferencesKey("pause_in_landscape")
        val KEY_PAUSE_ON_FULL_SCREEN = booleanPreferencesKey("pause_on_full_screen")
        val KEY_PAUSE_ON_PERMISSION_SCREEN = booleanPreferencesKey("pause_on_permission_screen")
        val KEY_HAPTIC_INTENSITY = floatPreferencesKey("haptic_intensity")
        val KEY_HAPTIC_SOUND_ENABLED = booleanPreferencesKey("haptic_sound_enabled")
        val KEY_ANIMATION_SPEED = floatPreferencesKey("animation_speed")
        val KEY_ANIMATION_SIZE = floatPreferencesKey("animation_size")
        val KEY_HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        // Per-edge sensitivity
        val KEY_LEFT_DAMPING = floatPreferencesKey("left_damping")
        val KEY_RIGHT_DAMPING = floatPreferencesKey("right_damping")
        val KEY_BOTTOM_DAMPING = floatPreferencesKey("bottom_damping")
        val KEY_LEFT_SWIPE_THRESHOLD_DP = floatPreferencesKey("left_swipe_threshold_dp")
        val KEY_RIGHT_SWIPE_THRESHOLD_DP = floatPreferencesKey("right_swipe_threshold_dp")
        val KEY_BOTTOM_SWIPE_THRESHOLD_DP = floatPreferencesKey("bottom_swipe_threshold_dp")
        // Vertical offset (side edges)
        val KEY_LEFT_VERTICAL_START = floatPreferencesKey("left_vertical_start")
        val KEY_LEFT_VERTICAL_END = floatPreferencesKey("left_vertical_end")
        val KEY_RIGHT_VERTICAL_START = floatPreferencesKey("right_vertical_start")
        val KEY_RIGHT_VERTICAL_END = floatPreferencesKey("right_vertical_end")
        // Direction control
        val KEY_DIRECTION_TOLERANCE = floatPreferencesKey("direction_tolerance_degrees")
        val KEY_HYSTERESIS_RATIO = floatPreferencesKey("hysteresis_ratio")
        val KEY_L_SWIPE_THRESHOLD_DP = floatPreferencesKey("l_swipe_threshold_dp")
    }
}
