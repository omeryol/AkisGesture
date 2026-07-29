package com.omer.akisgesture.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.omer.akisgesture.AkisGestureApp
import com.omer.akisgesture.gesture.GestureConfig
import com.omer.akisgesture.feedback.FeedbackAnimation
import com.omer.akisgesture.feedback.FeedbackIcon
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.omer.akisgesture.root.RootCommandExecutor
import com.omer.akisgesture.root.RootResult

enum class RootAccessState { CHECKING, AVAILABLE, UNAVAILABLE }

data class SelectableApp(
    val packageName: String,
    val label: String,
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AkisGestureApp

    val configState: StateFlow<GestureConfig> = app.gestureConfigFlow
    val pausedPackages: StateFlow<Set<String>> = app.pausedPackagesFlow
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
