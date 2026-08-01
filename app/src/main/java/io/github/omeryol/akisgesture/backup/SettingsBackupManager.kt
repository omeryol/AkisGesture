package io.github.omeryol.akisgesture.backup

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.omeryol.akisgesture.AkisGestureApp
import io.github.omeryol.akisgesture.gesture.GestureConfig
import io.github.omeryol.akisgesture.service.AccessibilityControl
import io.github.omeryol.akisgesture.rule.RuleSerializer.toGestureRuleGraph
import io.github.omeryol.akisgesture.rule.AppRuleProfilesSerializer
import io.github.omeryol.akisgesture.rule.Presets
import io.github.omeryol.akisgesture.settingsDataStore
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

object SettingsBackupManager {
    private const val FORMAT = "akis-gesture-backup"
    private const val VERSION = 2

    suspend fun export(app: AkisGestureApp): String {
        val entries = JSONArray()
        val storedPreferences = app.settingsDataStore.data.first().asMap()
        val mergedPreferences = linkedMapOf<String, Pair<Preferences.Key<*>, Any>>()
        storedPreferences.forEach { (key, value) ->
            mergedPreferences[key.name] = kotlin.Pair(key, value)
        }

        val config = app.gestureConfigFlow.value
        fun <T> snapshot(key: Preferences.Key<T>, value: T): kotlin.Pair<Preferences.Key<*>, Any> =
            kotlin.Pair(key, value as Any)
        val resolvedConfigEntries = listOf(
            snapshot(GestureConfig.KEY_LEFT_ENABLED, config.leftEnabled),
            snapshot(GestureConfig.KEY_RIGHT_ENABLED, config.rightEnabled),
            snapshot(GestureConfig.KEY_BOTTOM_ENABLED, config.bottomEnabled),
            snapshot(GestureConfig.KEY_LEFT_TRIGGER_WIDTH, config.leftTriggerWidthDp),
            snapshot(GestureConfig.KEY_RIGHT_TRIGGER_WIDTH, config.rightTriggerWidthDp),
            snapshot(GestureConfig.KEY_BOTTOM_TRIGGER_HEIGHT, config.bottomTriggerHeightDp),
            snapshot(GestureConfig.KEY_HOLD_TIME, config.holdTimeMs),
            snapshot(GestureConfig.KEY_HOLD_FIRE_MODE, config.holdFireMode.name),
            snapshot(GestureConfig.KEY_FEEDBACK_COLOR, config.feedbackColorArgb),
            snapshot(GestureConfig.KEY_SECONDARY_COLOR, config.secondaryColorArgb),
            snapshot(GestureConfig.KEY_L_SWIPE_COLOR, config.lSwipeColorArgb),
            snapshot(GestureConfig.KEY_USE_APP_ADAPTIVE_COLOR, config.useAppAdaptiveColor),
            snapshot(GestureConfig.KEY_FEEDBACK_OPACITY, config.feedbackOpacity),
            snapshot(GestureConfig.KEY_FEEDBACK_ANIMATION, config.feedbackAnimation.name),
            snapshot(GestureConfig.KEY_QUICK_FEEDBACK_ICON, config.quickFeedbackIcon.name),
            snapshot(GestureConfig.KEY_HOLD_FEEDBACK_ICON, config.holdFeedbackIcon.name),
            snapshot(GestureConfig.KEY_PAUSE_ON_LOCK_SCREEN, config.pauseOnLockScreen),
            snapshot(GestureConfig.KEY_PAUSE_WHEN_KEYBOARD_VISIBLE, config.pauseWhenKeyboardVisible),
            snapshot(GestureConfig.KEY_PAUSE_IN_LANDSCAPE, config.pauseInLandscape),
            snapshot(GestureConfig.KEY_PAUSE_ON_FULL_SCREEN, config.pauseOnFullScreen),
            snapshot(GestureConfig.KEY_PAUSE_ON_PERMISSION_SCREEN, config.pauseOnPermissionScreen),
            snapshot(GestureConfig.KEY_HAPTIC_INTENSITY, config.hapticIntensity),
            snapshot(GestureConfig.KEY_HAPTIC_SOUND_ENABLED, config.hapticSoundEnabled),
            snapshot(GestureConfig.KEY_ANIMATION_SPEED, config.animationSpeed),
            snapshot(GestureConfig.KEY_ANIMATION_SIZE, config.animationSize),
            snapshot(GestureConfig.KEY_HAPTIC_ENABLED, config.hapticEnabled),
            snapshot(GestureConfig.KEY_LEFT_DAMPING, config.leftDamping),
            snapshot(GestureConfig.KEY_RIGHT_DAMPING, config.rightDamping),
            snapshot(GestureConfig.KEY_BOTTOM_DAMPING, config.bottomDamping),
            snapshot(GestureConfig.KEY_LEFT_SWIPE_THRESHOLD_DP, config.leftSwipeThresholdDp),
            snapshot(GestureConfig.KEY_RIGHT_SWIPE_THRESHOLD_DP, config.rightSwipeThresholdDp),
            snapshot(GestureConfig.KEY_BOTTOM_SWIPE_THRESHOLD_DP, config.bottomSwipeThresholdDp),
            snapshot(GestureConfig.KEY_LEFT_VERTICAL_START, config.leftVerticalStart),
            snapshot(GestureConfig.KEY_LEFT_VERTICAL_END, config.leftVerticalEnd),
            snapshot(GestureConfig.KEY_RIGHT_VERTICAL_START, config.rightVerticalStart),
            snapshot(GestureConfig.KEY_RIGHT_VERTICAL_END, config.rightVerticalEnd),
            snapshot(GestureConfig.KEY_DIRECTION_TOLERANCE, config.directionToleranceDegrees),
            snapshot(GestureConfig.KEY_HYSTERESIS_RATIO, config.hysteresisRatio),
            snapshot(GestureConfig.KEY_L_SWIPE_THRESHOLD_DP, config.lSwipeThresholdDp),
        )
        resolvedConfigEntries.forEach { entry ->
            val key = entry.first
            val value = entry.second
            mergedPreferences.putIfAbsent(key.name, kotlin.Pair(key, value))
        }

        mergedPreferences.toSortedMap()
            .values
            .forEach { (key, value) ->
                val item = JSONObject().put("key", key.name)
                when (value) {
                    is Boolean -> item.put("type", "boolean").put("value", value)
                    is Int -> item.put("type", "int").put("value", value)
                    is Long -> item.put("type", "long").put("value", value)
                    is Float -> item.put("type", "float").put("value", value.toDouble())
                    is String -> item.put("type", "string").put("value", value)
                    is Set<*> -> item.put("type", "string_set").put(
                        "value",
                        JSONArray(value.filterIsInstance<String>().sorted()),
                    )
                    else -> return@forEach
                }
                entries.put(item)
            }
        return JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put("createdAt", System.currentTimeMillis())
            .put(
                "accessibility",
                JSONObject().put("desiredEnabled", AccessibilityControl.isDesired(app)),
            )
            .put("entries", entries)
            .toString(2)
    }

    suspend fun import(app: AkisGestureApp, json: String) {
        val root = JSONObject(json)
        require(root.optString("format") == FORMAT) { "Bu dosya Akış Gesture yedeği değil" }
        val version = root.optInt("version")
        require(version in 1..VERSION) { "Yedek sürümü desteklenmiyor" }
        val entries = root.getJSONArray("entries")

        // Validate rules before replacing any current setting.
        for (index in 0 until entries.length()) {
            val item = entries.getJSONObject(index)
            if (item.getString("key") == "gesture_rules_json") {
                item.getString("value").toGestureRuleGraph()
            }
            if (item.getString("key") == "app_rule_profiles_json") {
                AppRuleProfilesSerializer.fromJson(item.getString("value"))
            }
        }

        app.settingsDataStore.edit { prefs ->
            prefs.clear()
            for (index in 0 until entries.length()) {
                val item = entries.getJSONObject(index)
                val key = item.getString("key")
                when (item.getString("type")) {
                    "boolean" -> prefs[booleanPreferencesKey(key)] = item.getBoolean("value")
                    "int" -> prefs[intPreferencesKey(key)] = item.getInt("value")
                    "long" -> prefs[longPreferencesKey(key)] = item.getLong("value")
                    "float" -> prefs[floatPreferencesKey(key)] =
                        item.getDouble("value").toFloat()
                    "string" -> prefs[stringPreferencesKey(key)] = item.getString("value")
                    "string_set" -> {
                        val values = item.getJSONArray("value")
                        prefs[stringSetPreferencesKey(key)] =
                            (0 until values.length()).map(values::getString).toSet()
                    }
                    else -> error("Bilinmeyen yedek alanı")
                }
            }
        }
        root.optJSONObject("accessibility")
            ?.takeIf { it.has("desiredEnabled") }
            ?.let { AccessibilityControl.setDesired(app, it.getBoolean("desiredEnabled")) }
        app.applyRules(app.loadSavedRules() ?: Presets.DEFAULT)
    }
}
