package com.openswipe

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.openswipe.gesture.BottomTriggerMode
import com.openswipe.gesture.GestureConfig
import com.openswipe.overlay.Edge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class OpenSwipeApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    lateinit var gestureConfigFlow: StateFlow<GestureConfig>
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        gestureConfigFlow = settingsDataStore.data
            .map { prefs ->
                GestureConfig(
                    leftEnabled = prefs[GestureConfig.KEY_LEFT_ENABLED] ?: true,
                    rightEnabled = prefs[GestureConfig.KEY_RIGHT_ENABLED] ?: true,
                    bottomEnabled = prefs[GestureConfig.KEY_BOTTOM_ENABLED] ?: true,
                    bottomTriggerHeightDp = prefs[GestureConfig.KEY_BOTTOM_TRIGGER_HEIGHT] ?: 48f,
                    bottomTriggerMode = prefs[GestureConfig.KEY_BOTTOM_TRIGGER_MODE]
                        ?.let { runCatching { BottomTriggerMode.valueOf(it) }.getOrNull() }
                        ?: BottomTriggerMode.TOUCH,
                )
            }
            .stateIn(appScope, SharingStarted.Eagerly, GestureConfig())
    }

    suspend fun updateEdgeEnabled(edge: Edge, enabled: Boolean) {
        val key = when (edge) {
            Edge.LEFT -> GestureConfig.KEY_LEFT_ENABLED
            Edge.RIGHT -> GestureConfig.KEY_RIGHT_ENABLED
            Edge.BOTTOM -> GestureConfig.KEY_BOTTOM_ENABLED
        }
        settingsDataStore.edit { prefs ->
            prefs[key] = enabled
        }
    }

    suspend fun updateBottomTriggerHeight(dp: Float) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_BOTTOM_TRIGGER_HEIGHT] = dp
        }
    }

    suspend fun updateBottomTriggerMode(mode: BottomTriggerMode) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_BOTTOM_TRIGGER_MODE] = mode.name
        }
    }

    companion object {
        private lateinit var instance: OpenSwipeApp
        fun getInstance(): OpenSwipeApp = instance
    }
}
