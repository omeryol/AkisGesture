package com.omer.akisgesture.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.omer.akisgesture.AkisGestureApp
import com.omer.akisgesture.gesture.GestureConfig
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AkisGestureApp

    val configState: StateFlow<GestureConfig> = app.gestureConfigFlow

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
}
