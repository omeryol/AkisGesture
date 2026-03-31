package com.openswipe.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.openswipe.OpenSwipeApp
import com.openswipe.gesture.GestureConfig
import com.openswipe.overlay.Edge
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as OpenSwipeApp

    val configState: StateFlow<GestureConfig> = app.gestureConfigFlow

    fun setLeftEnabled(enabled: Boolean) {
        viewModelScope.launch {
            app.updateEdgeEnabled(Edge.LEFT, enabled)
        }
    }

    fun setRightEnabled(enabled: Boolean) {
        viewModelScope.launch {
            app.updateEdgeEnabled(Edge.RIGHT, enabled)
        }
    }

    fun setBottomEnabled(enabled: Boolean) {
        viewModelScope.launch {
            app.updateEdgeEnabled(Edge.BOTTOM, enabled)
        }
    }
}
