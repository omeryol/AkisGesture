package com.omer.akisgesture.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.preferences.core.stringPreferencesKey
import com.omer.akisgesture.AkisGestureApp
import com.omer.akisgesture.settingsDataStore
import com.omer.akisgesture.feedback.FeedbackAnimation
import com.omer.akisgesture.feedback.FeedbackIcon
import com.omer.akisgesture.gesture.GestureConfig
import com.omer.akisgesture.gesture.HoldFireMode
import com.omer.akisgesture.model.GestureRule
import com.omer.akisgesture.overlay.Edge
import com.omer.akisgesture.root.RootCommandExecutor
import com.omer.akisgesture.root.RootResult
import com.omer.akisgesture.rule.Presets
import com.omer.akisgesture.rule.RuleSerializer.toGestureRuleGraph
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

enum class RootAccessState { CHECKING, AVAILABLE, UNAVAILABLE }

data class SelectableApp(
    val packageName: String,
    val label: String,
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AkisGestureApp

    val configState: StateFlow<GestureConfig> = app.gestureConfigFlow
    val pausedPackages: StateFlow<Set<String>> = app.pausedPackagesFlow
    val rules: StateFlow<List<GestureRule>> = app.settingsDataStore.data.map { prefs ->
        val json = prefs[stringPreferencesKey("gesture_rules_json")]
            ?: return@map Presets.DEFAULT.rules.toList()
        runCatching { json.toGestureRuleGraph() }
            .getOrElse { Presets.DEFAULT }.rules.toList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Presets.DEFAULT.rules.toList())
    private val _selectableApps = MutableStateFlow<List<SelectableApp>>(emptyList())
    val selectableApps: StateFlow<List<SelectableApp>> = _selectableApps.asStateFlow()
    private val _rootAccess = MutableStateFlow(RootAccessState.CHECKING)
    val rootAccess = _rootAccess.asStateFlow()

    init {
        checkRootAccess()
        loadSelectableApps()
    }

    fun checkRootAccess() {
        _rootAccess.value = RootAccessState.CHECKING
        viewModelScope.launch(Dispatchers.IO) {
            _rootAccess.value = when (RootCommandExecutor(app).checkAccess()) {
                RootResult.Success -> RootAccessState.AVAILABLE
                is RootResult.Failure -> RootAccessState.UNAVAILABLE
            }
        }
    }

    fun setEdgeTriggerWidth(dp: Float) {
        viewModelScope.launch {
            app.updateEdgeTriggerWidth(dp)
        }
    }

    fun setBottomTriggerHeight(dp: Float) {
        viewModelScope.launch {
            app.updateBottomTriggerHeight(dp)
        }
    }

    fun setHoldTime(milliseconds: Long) {
        viewModelScope.launch {
            app.updateHoldTime(milliseconds)
        }
    }

    fun setPackagePaused(packageName: String, paused: Boolean) {
        viewModelScope.launch {
            app.setPackagePaused(packageName, paused)
        }
    }

    fun setFeedbackColor(argb: Int) {
        viewModelScope.launch { app.updateFeedbackColor(argb) }
    }

    fun setFeedbackOpacity(opacity: Float) {
        viewModelScope.launch { app.updateFeedbackOpacity(opacity) }
    }

    fun setFeedbackAnimation(animation: FeedbackAnimation) {
        viewModelScope.launch { app.updateFeedbackAnimation(animation) }
    }

    fun setQuickFeedbackIcon(icon: FeedbackIcon) {
        viewModelScope.launch { app.updateQuickFeedbackIcon(icon) }
    }

    fun setHoldFeedbackIcon(icon: FeedbackIcon) {
        viewModelScope.launch { app.updateHoldFeedbackIcon(icon) }
    }

    fun setPauseOnLockScreen(enabled: Boolean) {
        viewModelScope.launch { app.updatePauseOnLockScreen(enabled) }
    }

    fun setPauseWhenKeyboardVisible(enabled: Boolean) {
        viewModelScope.launch { app.updatePauseWhenKeyboardVisible(enabled) }
    }

    fun setPauseInLandscape(enabled: Boolean) {
        viewModelScope.launch { app.updatePauseInLandscape(enabled) }
    }

    fun setPauseOnFullScreen(enabled: Boolean) {
        viewModelScope.launch { app.updatePauseOnFullScreen(enabled) }
    }

    fun setPauseOnPermissionScreen(enabled: Boolean) {
        viewModelScope.launch { app.updatePauseOnPermissionScreen(enabled) }
    }

    // ── Per-edge sensitivity ──

    fun setEdgeTriggerSize(edge: Edge, dp: Float) {
        viewModelScope.launch { app.updateEdgeTriggerSize(edge, dp) }
    }

    fun setEdgeDamping(edge: Edge, value: Float) {
        viewModelScope.launch { app.updateEdgeDamping(edge, value) }
    }

    fun setEdgeSwipeThreshold(edge: Edge, dp: Float) {
        viewModelScope.launch { app.updateEdgeSwipeThreshold(edge, dp) }
    }

    fun setLSwipeThreshold(dp: Float) {
        viewModelScope.launch { app.updateLSwipeThreshold(dp) }
    }

    fun setEdgeVerticalRange(edge: Edge, start: Float, end: Float) {
        viewModelScope.launch { app.updateEdgeVerticalRange(edge, start, end) }
    }

    fun setHoldFireMode(mode: HoldFireMode) {
        viewModelScope.launch { app.updateHoldFireMode(mode) }
    }

    fun setHapticIntensity(intensity: Float) {
        viewModelScope.launch {
            app.updateHapticIntensity(intensity)
            if (intensity > 0f) {
                com.omer.akisgesture.feedback.HapticHelper.intensity = intensity
                com.omer.akisgesture.feedback.HapticHelper.performHaptic(app, com.omer.akisgesture.feedback.HapticHelper.HapticType.LIGHT)
            }
        }
    }

    fun setHapticSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { app.updateHapticSoundEnabled(enabled) }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch { app.updateHapticEnabled(enabled) }
    }

    fun setAnimationSpeed(speed: Float) {
        viewModelScope.launch { app.updateAnimationSpeed(speed) }
    }

    fun setAnimationSize(size: Float) {
        viewModelScope.launch { app.updateAnimationSize(size) }
    }

    private fun loadSelectableApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            _selectableApps.value = app.packageManager
                .queryIntentActivities(launcherIntent, 0)
                .asSequence()
                .filter { it.activityInfo.packageName != app.packageName }
                .map {
                    SelectableApp(
                        packageName = it.activityInfo.packageName,
                        label = it.loadLabel(app.packageManager).toString(),
                    )
                }
                .distinctBy { it.packageName }
                .sortedBy { it.label.lowercase() }
                .toList()
        }
    }
}
