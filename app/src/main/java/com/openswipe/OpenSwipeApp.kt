package com.omer.akisgesture

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.omer.akisgesture.gesture.GestureConfig
import com.omer.akisgesture.rule.CompiledRuleSet
import com.omer.akisgesture.rule.GestureRuleGraph
import com.omer.akisgesture.rule.Presets
import com.omer.akisgesture.rule.RuleSerializer.toGestureRuleGraph
import com.omer.akisgesture.rule.RuleSerializer.toJson
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

class AkisGestureApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    lateinit var gestureConfigFlow: StateFlow<GestureConfig>
        private set

    private val _compiledRuleSet = MutableStateFlow(CompiledRuleSet.EMPTY)
    val compiledRuleSet: StateFlow<CompiledRuleSet> = _compiledRuleSet.asStateFlow()

    lateinit var pausedPackagesFlow: StateFlow<Set<String>>
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
                    edgeTriggerWidthDp = prefs[GestureConfig.KEY_EDGE_TRIGGER_WIDTH] ?: 20f,
                    bottomTriggerHeightDp = prefs[GestureConfig.KEY_BOTTOM_TRIGGER_HEIGHT] ?: 40f,
                    holdTimeMs = prefs[GestureConfig.KEY_HOLD_TIME] ?: 280L,
                    feedbackColorArgb = prefs[GestureConfig.KEY_FEEDBACK_COLOR]
                        ?: 0xFF3D5AFE.toInt(),
                    feedbackOpacity = prefs[GestureConfig.KEY_FEEDBACK_OPACITY] ?: 0.57f,
                )
            }
            .stateIn(appScope, SharingStarted.Eagerly, GestureConfig())

        pausedPackagesFlow = settingsDataStore.data
            .map { prefs -> prefs[KEY_PAUSED_PACKAGES] ?: emptySet() }
            .stateIn(appScope, SharingStarted.Eagerly, emptySet())

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

    /**
     * 同步加载Kurallar — 在 onServiceConnected 中调用，确保 Service 启动时Kurallar立即可用。
     * 学习 STB 策略：不依赖异步 flow，在 Service 绑定时同步完成初始化。
     */
    fun ensureRulesLoadedSync() {
        if (_compiledRuleSet.value !== CompiledRuleSet.EMPTY) return
        // AccessibilityService connects on the main thread. Blocking for
        // DataStore here can deadlock initialization and produce an ANR.
        // Safe defaults are available immediately; onCreate replaces them
        // asynchronously with persisted rules.
        _compiledRuleSet.value = Presets.DEFAULT.compile()
    }

    suspend fun updateHoldTime(milliseconds: Long) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_HOLD_TIME] = milliseconds.coerceIn(150L, 700L)
        }
    }

    suspend fun setPackagePaused(packageName: String, paused: Boolean) {
        if (packageName == this.packageName) return
        settingsDataStore.edit { prefs ->
            val current = prefs[KEY_PAUSED_PACKAGES].orEmpty()
            prefs[KEY_PAUSED_PACKAGES] =
                if (paused) current + packageName else current - packageName
        }
    }

    suspend fun updateFeedbackColor(argb: Int) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_FEEDBACK_COLOR] = argb
        }
    }

    suspend fun updateFeedbackOpacity(opacity: Float) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_FEEDBACK_OPACITY] = opacity.coerceIn(0.1f, 1f)
        }
    }

    companion object {
        private val KEY_RULES_JSON = stringPreferencesKey("gesture_rules_json")
        private val KEY_PAUSED_PACKAGES = stringSetPreferencesKey("paused_packages")
        private lateinit var instance: AkisGestureApp
        fun getInstance(): AkisGestureApp = instance
    }
}
