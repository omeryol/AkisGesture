package com.omer.akisgesture.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.omer.akisgesture.AkisGestureApp
import com.omer.akisgesture.gesture.GestureConfig
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.omer.akisgesture.root.RootCommandExecutor
import com.omer.akisgesture.root.RootResult

enum class RootAccessState { CHECKING, AVAILABLE, UNAVAILABLE }

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AkisGestureApp

    val configState: StateFlow<GestureConfig> = app.gestureConfigFlow
    private val _rootAccess = MutableStateFlow(RootAccessState.CHECKING)
    val rootAccess = _rootAccess.asStateFlow()

    init {
        checkRootAccess()
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
}
