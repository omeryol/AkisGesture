package com.openswipe

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.openswipe.gesture.GestureConfig
import com.openswipe.rule.CompiledRuleSet
import com.openswipe.rule.GestureRuleGraph
import com.openswipe.rule.Presets
import com.openswipe.rule.RuleSerializer.toGestureRuleGraph
import com.openswipe.rule.RuleSerializer.toJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class OpenSwipeApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    lateinit var gestureConfigFlow: StateFlow<GestureConfig>
        private set

    private val _compiledRuleSet = MutableStateFlow(CompiledRuleSet.EMPTY)
    val compiledRuleSet: StateFlow<CompiledRuleSet> = _compiledRuleSet.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        instance = this

        gestureConfigFlow = settingsDataStore.data
            .map { prefs ->
                GestureConfig(
                    leftEnabled = prefs[GestureConfig.KEY_LEFT_ENABLED] ?: true,
                    rightEnabled = prefs[GestureConfig.KEY_RIGHT_ENABLED] ?: true,
                    bottomEnabled = prefs[GestureConfig.KEY_BOTTOM_ENABLED] ?: true,
                    edgeTriggerWidthDp = prefs[GestureConfig.KEY_EDGE_TRIGGER_WIDTH] ?: 20f,
                    bottomTriggerHeightDp = prefs[GestureConfig.KEY_BOTTOM_TRIGGER_HEIGHT] ?: 40f,
                )
            }
            .stateIn(appScope, SharingStarted.Eagerly, GestureConfig())

        // Load rules from DataStore on startup
        appScope.launch(Dispatchers.IO) {
            val prefs = settingsDataStore.data.first()
            val json = prefs[KEY_RULES_JSON]
            val graph = if (json != null) {
                runCatching { json.toGestureRuleGraph() }.getOrElse { Presets.DEFAULT }
            } else {
                Presets.DEFAULT
            }
            _compiledRuleSet.value = graph.compile()
        }
    }

    suspend fun applyRules(graph: GestureRuleGraph) {
        val json = graph.toJson()
        settingsDataStore.edit { prefs ->
            prefs[KEY_RULES_JSON] = json
        }
        _compiledRuleSet.value = graph.compile()
    }

    suspend fun updateEdgeTriggerWidth(dp: Float) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_EDGE_TRIGGER_WIDTH] = dp
        }
    }

    suspend fun updateBottomTriggerHeight(dp: Float) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_BOTTOM_TRIGGER_HEIGHT] = dp
        }
    }

    suspend fun loadSavedRules(): GestureRuleGraph? {
        val prefs = settingsDataStore.data.first()
        val json = prefs[KEY_RULES_JSON] ?: return null
        return runCatching { json.toGestureRuleGraph() }.getOrNull()
    }

    companion object {
        private val KEY_RULES_JSON = stringPreferencesKey("gesture_rules_json")
        private lateinit var instance: OpenSwipeApp
        fun getInstance(): OpenSwipeApp = instance
    }
}
