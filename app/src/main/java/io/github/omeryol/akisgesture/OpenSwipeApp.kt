package io.github.omeryol.akisgesture

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.omeryol.akisgesture.gesture.GestureConfig
import io.github.omeryol.akisgesture.gesture.HoldFireMode
import io.github.omeryol.akisgesture.feedback.FeedbackAnimation
import io.github.omeryol.akisgesture.feedback.FeedbackIcon
import io.github.omeryol.akisgesture.model.ActionIconPack
import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.rule.CompiledRuleSet
import io.github.omeryol.akisgesture.rule.AppRuleProfilesSerializer
import io.github.omeryol.akisgesture.rule.GestureRuleGraph
import io.github.omeryol.akisgesture.rule.Presets
import io.github.omeryol.akisgesture.rule.RuleSerializer.toGestureRuleGraph
import io.github.omeryol.akisgesture.rule.RuleSerializer.toJson
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

    lateinit var ruleProfilesFlow: StateFlow<Map<String, GestureRuleGraph>>
        private set

    lateinit var compiledRuleProfilesFlow: StateFlow<Map<String, CompiledRuleSet>>
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        io.github.omeryol.akisgesture.util.GestureTracker.init(this)

        gestureConfigFlow = settingsDataStore.data
            .map { prefs ->
                GestureConfig(
                    masterEnabled = prefs[GestureConfig.KEY_MASTER_ENABLED] ?: true,
                    leftEnabled = prefs[GestureConfig.KEY_LEFT_ENABLED] ?: true,
                    rightEnabled = prefs[GestureConfig.KEY_RIGHT_ENABLED] ?: true,
                    bottomEnabled = prefs[GestureConfig.KEY_BOTTOM_ENABLED] ?: true,
                    leftTriggerWidthDp = prefs[GestureConfig.KEY_LEFT_TRIGGER_WIDTH] ?: 20f,
                    rightTriggerWidthDp = prefs[GestureConfig.KEY_RIGHT_TRIGGER_WIDTH] ?: 20f,
                    bottomTriggerHeightDp = prefs[GestureConfig.KEY_BOTTOM_TRIGGER_HEIGHT] ?: 40f,
                    holdTimeMs = prefs[GestureConfig.KEY_HOLD_TIME] ?: 280L,
                    holdFireMode = prefs[GestureConfig.KEY_HOLD_FIRE_MODE]
                        ?.let { runCatching { HoldFireMode.valueOf(it) }.getOrNull() }
                        ?: HoldFireMode.ON_RELEASE,
                    ringMenuEnabled = prefs[GestureConfig.KEY_RING_MENU_ENABLED] ?: true,
                    leftRingMenuEnabled = prefs[GestureConfig.KEY_LEFT_RING_MENU_ENABLED] ?: (prefs[GestureConfig.KEY_RING_MENU_ENABLED] ?: true),
                    rightRingMenuEnabled = prefs[GestureConfig.KEY_RIGHT_RING_MENU_ENABLED] ?: (prefs[GestureConfig.KEY_RING_MENU_ENABLED] ?: true),
                    bottomRingMenuEnabled = prefs[GestureConfig.KEY_BOTTOM_RING_MENU_ENABLED] ?: (prefs[GestureConfig.KEY_RING_MENU_ENABLED] ?: true),
                    leftRingActionIds = prefs[GestureConfig.KEY_LEFT_RING_ACTIONS].toRingActionIds(),
                    rightRingActionIds = prefs[GestureConfig.KEY_RIGHT_RING_ACTIONS].toRingActionIds(),
                    bottomRingActionIds = prefs[GestureConfig.KEY_BOTTOM_RING_ACTIONS].toRingActionIds(),
                    ringGroupInsetDp = prefs[GestureConfig.KEY_RING_GROUP_INSET_DP] ?: 100f,
                    ringGroupSpacingDp = prefs[GestureConfig.KEY_RING_GROUP_SPACING_DP] ?: 60f,
                    ringSizeDp = prefs[GestureConfig.KEY_RING_SIZE_DP] ?: 58f,
                    ringArc = prefs[GestureConfig.KEY_RING_ARC] ?: 0.92f,
                    leftDamping = prefs[GestureConfig.KEY_LEFT_DAMPING] ?: 2.0f,
                    rightDamping = prefs[GestureConfig.KEY_RIGHT_DAMPING] ?: 2.0f,
                    bottomDamping = prefs[GestureConfig.KEY_BOTTOM_DAMPING] ?: 2.0f,
                    leftSwipeThresholdDp = prefs[GestureConfig.KEY_LEFT_SWIPE_THRESHOLD_DP] ?: 14f,
                    rightSwipeThresholdDp = prefs[GestureConfig.KEY_RIGHT_SWIPE_THRESHOLD_DP] ?: 14f,
                    bottomSwipeThresholdDp = prefs[GestureConfig.KEY_BOTTOM_SWIPE_THRESHOLD_DP] ?: 14f,
                    leftVerticalStart = prefs[GestureConfig.KEY_LEFT_VERTICAL_START] ?: 0f,
                    leftVerticalEnd = prefs[GestureConfig.KEY_LEFT_VERTICAL_END] ?: 1f,
                    rightVerticalStart = prefs[GestureConfig.KEY_RIGHT_VERTICAL_START] ?: 0f,
                    rightVerticalEnd = prefs[GestureConfig.KEY_RIGHT_VERTICAL_END] ?: 1f,
                    hysteresisRatio = (prefs[GestureConfig.KEY_HYSTERESIS_RATIO] ?: 0.75f).let { ratio ->
                        if (ratio < 0.50f) 0.75f else ratio
                    },
                    lSwipeThresholdDp = prefs[GestureConfig.KEY_L_SWIPE_THRESHOLD_DP] ?: 30f,
                    feedbackColorArgb = prefs[GestureConfig.KEY_FEEDBACK_COLOR]
                        ?: 0xFF3D5AFE.toInt(),
                    secondaryColorArgb = prefs[GestureConfig.KEY_SECONDARY_COLOR]
                        ?: 0xFFFF9100.toInt(),
                    lSwipeColorArgb = prefs[GestureConfig.KEY_L_SWIPE_COLOR]
                        ?: 0xFF00E676.toInt(),
                    useAppAdaptiveColor = prefs[GestureConfig.KEY_USE_APP_ADAPTIVE_COLOR] ?: false,
                    feedbackOpacity = prefs[GestureConfig.KEY_FEEDBACK_OPACITY] ?: 0.57f,
                    feedbackAnimation = prefs[GestureConfig.KEY_FEEDBACK_ANIMATION]
                        ?.let(FeedbackAnimation::fromStoredName)
                        ?: FeedbackAnimation.OCEAN_WAVE,
                    quickFeedbackIcon = prefs[GestureConfig.KEY_QUICK_FEEDBACK_ICON]
                        ?.let { runCatching { FeedbackIcon.valueOf(it) }.getOrNull() }
                        ?: FeedbackIcon.CHEVRON,
                    holdFeedbackIcon = prefs[GestureConfig.KEY_HOLD_FEEDBACK_ICON]
                        ?.let { runCatching { FeedbackIcon.valueOf(it) }.getOrNull() }
                        ?: FeedbackIcon.STAR,
                    pauseOnLockScreen = prefs[GestureConfig.KEY_PAUSE_ON_LOCK_SCREEN] ?: true,
                    pauseWhenKeyboardVisible =
                        prefs[GestureConfig.KEY_PAUSE_WHEN_KEYBOARD_VISIBLE] ?: false,
                    pauseInLandscape = prefs[GestureConfig.KEY_PAUSE_IN_LANDSCAPE] ?: false,
                    pauseOnFullScreen = prefs[GestureConfig.KEY_PAUSE_ON_FULL_SCREEN] ?: true,
                    pauseOnPermissionScreen = prefs[GestureConfig.KEY_PAUSE_ON_PERMISSION_SCREEN] ?: true,
                    pauseOnCamera = prefs[GestureConfig.KEY_PAUSE_ON_CAMERA] ?: false,
                    pauseOnPhoneCall = prefs[GestureConfig.KEY_PAUSE_ON_PHONE_CALL] ?: false,
                    pauseOnLauncher = prefs[GestureConfig.KEY_PAUSE_ON_LAUNCHER] ?: false,
                    appPauseMode = prefs[GestureConfig.KEY_APP_PAUSE_MODE]
                        ?.let { runCatching { io.github.omeryol.akisgesture.gesture.AppPauseMode.valueOf(it) }.getOrNull() }
                        ?: io.github.omeryol.akisgesture.gesture.AppPauseMode.BLACKLIST,
                    hideFromRecents = prefs[GestureConfig.KEY_HIDE_FROM_RECENTS] ?: false,
                    automationAppsEnabled = prefs[GestureConfig.KEY_AUTOMATION_APPS_ENABLED] ?: false,


                    hapticIntensity = prefs[GestureConfig.KEY_HAPTIC_INTENSITY] ?: 1f,
                    hapticSoundEnabled = prefs[GestureConfig.KEY_HAPTIC_SOUND_ENABLED] ?: false,
                    hapticEnabled = prefs[GestureConfig.KEY_HAPTIC_ENABLED] ?: true,
                    showGestureIndicatorBar = prefs[GestureConfig.KEY_SHOW_GESTURE_INDICATOR_BAR] ?: false,
                    animationSpeed = prefs[GestureConfig.KEY_ANIMATION_SPEED] ?: 1f,
                    animationSize = prefs[GestureConfig.KEY_ANIMATION_SIZE] ?: 1f,
                    iconSize = prefs[GestureConfig.KEY_ICON_SIZE] ?: 1f,
                    showPhoneMap = prefs[GestureConfig.KEY_SHOW_PHONE_MAP] ?: true,
                    showSummaryChart = prefs[GestureConfig.KEY_SHOW_SUMMARY_CHART] ?: true,
                    showPresetsCard = prefs[GestureConfig.KEY_SHOW_PRESETS_CARD] ?: true,
                    actionIconPack = ActionIconPack.fromId(prefs[GestureConfig.KEY_ACTION_ICON_PACK]),
                    rootWatchdogEnabled = prefs[GestureConfig.KEY_ROOT_WATCHDOG_ENABLED] ?: false,
                    rootWatchdogIntervalMinutes = prefs[GestureConfig.KEY_ROOT_WATCHDOG_INTERVAL_MINUTES] ?: 15,
                )
            }
            .stateIn(appScope, SharingStarted.Eagerly, GestureConfig())

        pausedPackagesFlow = settingsDataStore.data
            .map { prefs -> prefs[KEY_PAUSED_PACKAGES] ?: emptySet() }
            .stateIn(appScope, SharingStarted.Eagerly, emptySet())

        ruleProfilesFlow = settingsDataStore.data
            .map { prefs ->
                prefs[KEY_RULE_PROFILES_JSON]
                    ?.let { json ->
                        runCatching { AppRuleProfilesSerializer.fromJson(json) }
                            .getOrDefault(emptyMap())
                    }
                    .orEmpty()
            }
            .stateIn(appScope, SharingStarted.Eagerly, emptyMap())

        compiledRuleProfilesFlow = ruleProfilesFlow
            .map { profiles -> profiles.mapValues { (_, graph) -> graph.compile() } }
            .stateIn(appScope, SharingStarted.Eagerly, emptyMap())

        // Load rules from DataStore on startup
        appScope.launch(Dispatchers.IO) {
            settingsDataStore.edit { prefs ->
                val stored = prefs[GestureConfig.KEY_FEEDBACK_ANIMATION] ?: return@edit
                val canonical = FeedbackAnimation.fromStoredName(stored) ?: return@edit
                if (stored != canonical.name) {
                    prefs[GestureConfig.KEY_FEEDBACK_ANIMATION] = canonical.name
                }
            }
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

    suspend fun applyProfileRules(packageName: String, graph: GestureRuleGraph) {
        if (packageName == this.packageName) return
        settingsDataStore.edit { prefs ->
            val current = prefs[KEY_RULE_PROFILES_JSON]
                ?.let { runCatching { AppRuleProfilesSerializer.fromJson(it) }.getOrNull() }
                .orEmpty()
            prefs[KEY_RULE_PROFILES_JSON] =
                AppRuleProfilesSerializer.toJson(current + (packageName to graph))
        }
    }

    suspend fun removeRuleProfile(packageName: String) {
        settingsDataStore.edit { prefs ->
            val current = prefs[KEY_RULE_PROFILES_JSON]
                ?.let { runCatching { AppRuleProfilesSerializer.fromJson(it) }.getOrNull() }
                .orEmpty()
            val updated = current - packageName
            if (updated.isEmpty()) {
                prefs.remove(KEY_RULE_PROFILES_JSON)
            } else {
                prefs[KEY_RULE_PROFILES_JSON] = AppRuleProfilesSerializer.toJson(updated)
            }
        }
    }

    suspend fun loadRuleProfile(packageName: String): GestureRuleGraph? {
        return ruleProfilesFlow.value[packageName]
            ?: settingsDataStore.data.first()[KEY_RULE_PROFILES_JSON]
                ?.let { runCatching { AppRuleProfilesSerializer.fromJson(it)[packageName] }.getOrNull() }
    }

    suspend fun updateEdgeTriggerWidth(dp: Float) {
        settingsDataStore.edit { prefs ->
            // Write to both per-edge keys (legacy function kept for ViewModel compatibility)
            prefs[GestureConfig.KEY_LEFT_TRIGGER_WIDTH] = dp
            prefs[GestureConfig.KEY_RIGHT_TRIGGER_WIDTH] = dp
        }
    }

    suspend fun updateEdgeTriggerSize(edge: io.github.omeryol.akisgesture.overlay.Edge, dp: Float) {
        settingsDataStore.edit { prefs ->
            when (edge) {
                io.github.omeryol.akisgesture.overlay.Edge.LEFT -> prefs[GestureConfig.KEY_LEFT_TRIGGER_WIDTH] = dp
                io.github.omeryol.akisgesture.overlay.Edge.RIGHT -> prefs[GestureConfig.KEY_RIGHT_TRIGGER_WIDTH] = dp
                io.github.omeryol.akisgesture.overlay.Edge.BOTTOM -> prefs[GestureConfig.KEY_BOTTOM_TRIGGER_HEIGHT] = dp
            }
        }
    }

    suspend fun updateEdgeEnabled(edge: io.github.omeryol.akisgesture.overlay.Edge, enabled: Boolean) {
        settingsDataStore.edit { prefs ->
            when (edge) {
                io.github.omeryol.akisgesture.overlay.Edge.LEFT -> prefs[GestureConfig.KEY_LEFT_ENABLED] = enabled
                io.github.omeryol.akisgesture.overlay.Edge.RIGHT -> prefs[GestureConfig.KEY_RIGHT_ENABLED] = enabled
                io.github.omeryol.akisgesture.overlay.Edge.BOTTOM -> prefs[GestureConfig.KEY_BOTTOM_ENABLED] = enabled
            }
        }
    }

    suspend fun updateBottomTriggerHeight(dp: Float) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_BOTTOM_TRIGGER_HEIGHT] = dp
        }
    }

    suspend fun updateHideFromRecents(hide: Boolean) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_HIDE_FROM_RECENTS] = hide
        }
    }


    suspend fun loadSavedRules(): GestureRuleGraph? {
        val prefs = settingsDataStore.data.first()
        val json = prefs[KEY_RULES_JSON] ?: return null
        return runCatching { json.toGestureRuleGraph() }.getOrNull()
    }

    /**
     * Kuralları senkron yükler — onServiceConnected içinde çağrılarak servis başladığında kuralların hazır olmasını sağlar.
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

    suspend fun updateFeedbackOpacity(opacity: Float) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_FEEDBACK_OPACITY] = opacity.coerceIn(0.1f, 1f)
        }
    }

    suspend fun updateFeedbackAnimation(animation: FeedbackAnimation) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_FEEDBACK_ANIMATION] = animation.name
        }
    }

    suspend fun updateActionIconPack(pack: ActionIconPack) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_ACTION_ICON_PACK] = pack.id
        }
    }

    suspend fun updateQuickFeedbackIcon(icon: FeedbackIcon) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_QUICK_FEEDBACK_ICON] = icon.name
        }
    }

    suspend fun updateFeedbackColor(colorArgb: Int) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_FEEDBACK_COLOR] = colorArgb
            prefs[GestureConfig.KEY_USE_APP_ADAPTIVE_COLOR] = false
        }
    }

    suspend fun updateSecondaryColor(colorArgb: Int) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_SECONDARY_COLOR] = colorArgb
        }
    }

    suspend fun updateLSwipeColor(colorArgb: Int) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_L_SWIPE_COLOR] = colorArgb
        }
    }

    suspend fun updateUseAppAdaptiveColor(enabled: Boolean) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_USE_APP_ADAPTIVE_COLOR] = enabled
        }
    }

    suspend fun updateHoldFeedbackIcon(icon: FeedbackIcon) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_HOLD_FEEDBACK_ICON] = icon.name
        }
    }

    suspend fun updatePauseOnLockScreen(enabled: Boolean) {
        settingsDataStore.edit { it[GestureConfig.KEY_PAUSE_ON_LOCK_SCREEN] = enabled }
    }

    suspend fun updatePauseWhenKeyboardVisible(enabled: Boolean) {
        settingsDataStore.edit { it[GestureConfig.KEY_PAUSE_WHEN_KEYBOARD_VISIBLE] = enabled }
    }

    suspend fun updatePauseInLandscape(enabled: Boolean) {
        settingsDataStore.edit { it[GestureConfig.KEY_PAUSE_IN_LANDSCAPE] = enabled }
    }

    suspend fun updatePauseOnFullScreen(enabled: Boolean) {
        settingsDataStore.edit { it[GestureConfig.KEY_PAUSE_ON_FULL_SCREEN] = enabled }
    }

    suspend fun updatePauseOnPermissionScreen(enabled: Boolean) {
        settingsDataStore.edit { it[GestureConfig.KEY_PAUSE_ON_PERMISSION_SCREEN] = enabled }
    }

    suspend fun updatePauseOnCamera(enabled: Boolean) {
        settingsDataStore.edit { it[GestureConfig.KEY_PAUSE_ON_CAMERA] = enabled }
    }

    suspend fun updatePauseOnPhoneCall(enabled: Boolean) {
        settingsDataStore.edit { it[GestureConfig.KEY_PAUSE_ON_PHONE_CALL] = enabled }
    }

    suspend fun updateRingMenuEnabled(enabled: Boolean) {
        settingsDataStore.edit { it[GestureConfig.KEY_RING_MENU_ENABLED] = enabled }
    }

    suspend fun updateRingMenuEnabled(edge: Edge, enabled: Boolean) {
        settingsDataStore.edit { prefs ->
            when (edge) {
                Edge.LEFT -> prefs[GestureConfig.KEY_LEFT_RING_MENU_ENABLED] = enabled
                Edge.RIGHT -> prefs[GestureConfig.KEY_RIGHT_RING_MENU_ENABLED] = enabled
                Edge.BOTTOM -> prefs[GestureConfig.KEY_BOTTOM_RING_MENU_ENABLED] = enabled
            }
        }
    }

    suspend fun updateRingActions(edge: Edge, actionIds: List<String>) {
        val stored = actionIds.filter { it.isNotBlank() && it != "no_action" }.distinct().take(3).joinToString("|")
        settingsDataStore.edit { prefs ->
            when (edge) {
                Edge.LEFT -> prefs[GestureConfig.KEY_LEFT_RING_ACTIONS] = stored
                Edge.RIGHT -> prefs[GestureConfig.KEY_RIGHT_RING_ACTIONS] = stored
                Edge.BOTTOM -> prefs[GestureConfig.KEY_BOTTOM_RING_ACTIONS] = stored
            }
        }
    }

    suspend fun updateRingGroupInsetDp(value: Float) {
        settingsDataStore.edit { it[GestureConfig.KEY_RING_GROUP_INSET_DP] = value.coerceIn(0f, 2000f) }
    }

    suspend fun updateRingGroupSpacingDp(value: Float) {
        settingsDataStore.edit { it[GestureConfig.KEY_RING_GROUP_SPACING_DP] = value.coerceIn(36f, 120f) }
    }

    suspend fun updateRingSizeDp(value: Float) {
        settingsDataStore.edit { it[GestureConfig.KEY_RING_SIZE_DP] = value.coerceIn(40f, 92f) }
    }

    suspend fun updateRingArc(value: Float) {
        settingsDataStore.edit { it[GestureConfig.KEY_RING_ARC] = value.coerceIn(0f, 1f) }
    }

    suspend fun updateAutomationAppsEnabled(enabled: Boolean) {
        settingsDataStore.edit { it[GestureConfig.KEY_AUTOMATION_APPS_ENABLED] = enabled }
    }

    suspend fun updatePauseOnLauncher(enabled: Boolean) {
        settingsDataStore.edit { it[GestureConfig.KEY_PAUSE_ON_LAUNCHER] = enabled }
    }

    suspend fun updateAppPauseMode(mode: io.github.omeryol.akisgesture.gesture.AppPauseMode) {
        settingsDataStore.edit { it[GestureConfig.KEY_APP_PAUSE_MODE] = mode.name }
    }


    suspend fun applyColorPalette(quickColor: Int, holdColor: Int, lSwipeColor: Int) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_FEEDBACK_COLOR] = quickColor
            prefs[GestureConfig.KEY_SECONDARY_COLOR] = holdColor
            prefs[GestureConfig.KEY_L_SWIPE_COLOR] = lSwipeColor
        }
    }

    suspend fun updateHapticIntensity(intensity: Float) {
        settingsDataStore.edit { it[GestureConfig.KEY_HAPTIC_INTENSITY] = intensity.coerceIn(0f, 1f) }
    }

    suspend fun updateHapticSoundEnabled(enabled: Boolean) {
        settingsDataStore.edit { it[GestureConfig.KEY_HAPTIC_SOUND_ENABLED] = enabled }
    }

    suspend fun updateAnimationSpeed(speed: Float) {
        settingsDataStore.edit { it[GestureConfig.KEY_ANIMATION_SPEED] = speed.coerceIn(0.5f, 2f) }
    }

    suspend fun updateAnimationSize(size: Float) {
        settingsDataStore.edit { it[GestureConfig.KEY_ANIMATION_SIZE] = size.coerceIn(0.5f, 2f) }
    }

    suspend fun updateIconSize(size: Float) {
        settingsDataStore.edit { it[GestureConfig.KEY_ICON_SIZE] = size.coerceIn(0.5f, 2f) }
    }

    suspend fun updateRootWatchdogEnabled(enabled: Boolean) {
        settingsDataStore.edit { it[GestureConfig.KEY_ROOT_WATCHDOG_ENABLED] = enabled }
    }

    suspend fun updateRootWatchdogInterval(minutes: Int) {
        settingsDataStore.edit { it[GestureConfig.KEY_ROOT_WATCHDOG_INTERVAL_MINUTES] = minutes.coerceIn(5, 120) }
    }

    suspend fun updateHapticEnabled(enabled: Boolean) {
        settingsDataStore.edit { it[GestureConfig.KEY_HAPTIC_ENABLED] = enabled }
    }

    suspend fun updateShowGestureIndicatorBar(show: Boolean) {
        settingsDataStore.edit { it[GestureConfig.KEY_SHOW_GESTURE_INDICATOR_BAR] = show }
    }

    // ── Per-edge sensitivity ──

    suspend fun updateEdgeDamping(edge: io.github.omeryol.akisgesture.overlay.Edge, value: Float) {
        settingsDataStore.edit { prefs ->
            when (edge) {
                io.github.omeryol.akisgesture.overlay.Edge.LEFT -> prefs[GestureConfig.KEY_LEFT_DAMPING] = value
                io.github.omeryol.akisgesture.overlay.Edge.RIGHT -> prefs[GestureConfig.KEY_RIGHT_DAMPING] = value
                io.github.omeryol.akisgesture.overlay.Edge.BOTTOM -> prefs[GestureConfig.KEY_BOTTOM_DAMPING] = value
            }
        }
    }

    suspend fun updateEdgeSwipeThreshold(edge: io.github.omeryol.akisgesture.overlay.Edge, dp: Float) {
        settingsDataStore.edit { prefs ->
            when (edge) {
                io.github.omeryol.akisgesture.overlay.Edge.LEFT -> prefs[GestureConfig.KEY_LEFT_SWIPE_THRESHOLD_DP] = dp
                io.github.omeryol.akisgesture.overlay.Edge.RIGHT -> prefs[GestureConfig.KEY_RIGHT_SWIPE_THRESHOLD_DP] = dp
                io.github.omeryol.akisgesture.overlay.Edge.BOTTOM -> prefs[GestureConfig.KEY_BOTTOM_SWIPE_THRESHOLD_DP] = dp
            }
        }
    }

    suspend fun updateLSwipeThreshold(dp: Float) {
        settingsDataStore.edit { prefs ->
            prefs[GestureConfig.KEY_L_SWIPE_THRESHOLD_DP] = dp.coerceIn(15f, 60f)
        }
    }

    suspend fun updateEdgeVerticalRange(
        edge: io.github.omeryol.akisgesture.overlay.Edge,
        start: Float,
        end: Float,
    ) {
        settingsDataStore.edit { prefs ->
            when (edge) {
                io.github.omeryol.akisgesture.overlay.Edge.LEFT -> {
                    prefs[GestureConfig.KEY_LEFT_VERTICAL_START] = start
                    prefs[GestureConfig.KEY_LEFT_VERTICAL_END] = end
                }
                io.github.omeryol.akisgesture.overlay.Edge.RIGHT -> {
                    prefs[GestureConfig.KEY_RIGHT_VERTICAL_START] = start
                    prefs[GestureConfig.KEY_RIGHT_VERTICAL_END] = end
                }
                io.github.omeryol.akisgesture.overlay.Edge.BOTTOM -> { /* no vertical range for bottom */ }
            }
        }
    }

    suspend fun updateHoldFireMode(mode: HoldFireMode) {
        settingsDataStore.edit { it[GestureConfig.KEY_HOLD_FIRE_MODE] = mode.name }
    }

    suspend fun updateShowPhoneMap(show: Boolean) {
        settingsDataStore.edit { it[GestureConfig.KEY_SHOW_PHONE_MAP] = show }
    }

    suspend fun updateShowSummaryChart(show: Boolean) {
        settingsDataStore.edit { it[GestureConfig.KEY_SHOW_SUMMARY_CHART] = show }
    }

    suspend fun updateShowPresetsCard(show: Boolean) {
        settingsDataStore.edit { it[GestureConfig.KEY_SHOW_PRESETS_CARD] = show }
    }

    companion object {
        private val KEY_RULES_JSON = stringPreferencesKey("gesture_rules_json")
        private val KEY_RULE_PROFILES_JSON = stringPreferencesKey("app_rule_profiles_json")
        private val KEY_PAUSED_PACKAGES = stringSetPreferencesKey("paused_packages")
        private lateinit var instance: AkisGestureApp
        fun getInstance(): AkisGestureApp = instance
    }
}

private fun String?.toRingActionIds(): List<String> = this
    ?.split('|')
    ?.filter { it.isNotBlank() }
    ?.take(3)
    .orEmpty()
