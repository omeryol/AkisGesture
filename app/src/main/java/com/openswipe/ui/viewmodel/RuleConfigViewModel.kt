package com.openswipe.ui.viewmodel

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.openswipe.model.ActionNode
import com.openswipe.model.GestureRule
import com.openswipe.model.TriggerNode
import com.openswipe.rule.GestureRuleGraph
import com.openswipe.rule.Presets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.openswipe.OpenSwipeApp
import kotlinx.coroutines.launch
import java.util.UUID

data class Conflict(
    val ruleA: GestureRule,
    val ruleB: GestureRule,
    val message: String,
)

class RuleConfigViewModel(application: Application) : AndroidViewModel(application) {

    private val _rules = MutableStateFlow<List<GestureRule>>(emptyList())
    val rules: StateFlow<List<GestureRule>> = _rules.asStateFlow()

    private val _conflicts = MutableStateFlow<List<Conflict>>(emptyList())
    val conflicts: StateFlow<List<Conflict>> = _conflicts.asStateFlow()

    /** Snapshot of rules at last apply. Used to detect unapplied changes. */
    private val _appliedRules = MutableStateFlow<List<GestureRule>>(emptyList())

    val hasUnappliedChanges: StateFlow<Boolean> = combine(_rules, _appliedRules) { current, applied ->
        current != applied
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** Name of the currently loaded preset, or null if custom. */
    private val _activePresetName = MutableStateFlow<String?>(null)
    val activePresetName: StateFlow<String?> = _activePresetName.asStateFlow()

    init {
        // Load default preset
        loadPreset("iOS 风格", Presets.IOS_STYLE)
    }

    // ── Mutations ──

    fun addRule(trigger: TriggerNode, action: ActionNode) {
        val newRule = GestureRule(
            id = UUID.randomUUID().toString(),
            trigger = trigger,
            action = action,
        )
        _rules.value = _rules.value + newRule
        _activePresetName.value = null
        revalidate()
    }

    fun removeRule(ruleId: String) {
        _rules.value = _rules.value.filter { it.id != ruleId }
        _activePresetName.value = null
        revalidate()
    }

    fun updateRuleAction(ruleId: String, newAction: ActionNode) {
        _rules.value = _rules.value.map { rule ->
            if (rule.id == ruleId) rule.copy(action = newAction) else rule
        }
        _activePresetName.value = null
        revalidate()
    }

    fun applyRules() {
        if (_conflicts.value.isNotEmpty()) return
        val graph = GestureRuleGraph(rules = _rules.value)
        _appliedRules.value = _rules.value.toList()
        viewModelScope.launch {
            (getApplication<Application>() as OpenSwipeApp).applyRules(graph)
        }
    }

    fun loadPreset(name: String, preset: GestureRuleGraph) {
        _rules.value = preset.rules
        _activePresetName.value = name
        revalidate()
    }

    // ── Validation ──

    private fun revalidate() {
        val currentRules = _rules.value
        val found = mutableListOf<Conflict>()
        for (i in currentRules.indices) {
            for (j in i + 1 until currentRules.size) {
                val a = currentRules[i]
                val b = currentRules[j]
                if (a.trigger.edge == b.trigger.edge &&
                    a.trigger.gestureType == b.trigger.gestureType &&
                    a.trigger.section.overlapsWith(b.trigger.section)
                ) {
                    found.add(
                        Conflict(
                            ruleA = a,
                            ruleB = b,
                            message = "${edgeLabel(a.trigger.edge)} ${gestureLabel(a.trigger.gestureType)} 区段冲突",
                        )
                    )
                }
            }
        }
        _conflicts.value = found
    }

    companion object {
        /** All available system action nodes. */
        val allActions: List<ActionNode> = listOf(
            // System navigation
            ActionNode.Back,
            ActionNode.Home,
            ActionNode.Recents,
            ActionNode.SwitchLastApp,
            // System control
            ActionNode.LockScreen,
            ActionNode.Screenshot,
            ActionNode.SplitScreen,
            ActionNode.PowerMenu,
            // Panels
            ActionNode.NotificationPanel,
            ActionNode.QuickSettings,
            // Media
            ActionNode.MediaPlayPause,
            ActionNode.MediaNext,
            ActionNode.MediaPrevious,
            ActionNode.VolumeUp,
            ActionNode.VolumeDown,
            // Hardware
            ActionNode.ToggleFlashlight,
            // No-op
            ActionNode.NoAction,
        )

        fun isActionAvailable(action: ActionNode): Boolean {
            return Build.VERSION.SDK_INT >= action.minApi
        }

        val presets: List<Pair<String, GestureRuleGraph>> = listOf(
            "iOS 风格" to Presets.IOS_STYLE,
            "Android 经典" to Presets.ANDROID_CLASSIC,
            "媒体控制" to Presets.MEDIA_CONTROL,
        )
    }
}

// ── UI label helpers ──

fun edgeLabel(edge: com.openswipe.overlay.Edge): String = when (edge) {
    com.openswipe.overlay.Edge.LEFT -> "左侧"
    com.openswipe.overlay.Edge.RIGHT -> "右侧"
    com.openswipe.overlay.Edge.BOTTOM -> "底部"
}

fun gestureLabel(type: com.openswipe.model.GestureType): String = when (type) {
    com.openswipe.model.GestureType.SHORT_SWIPE -> "短滑"
    com.openswipe.model.GestureType.LONG_SWIPE -> "长滑"
}

fun sectionLabel(section: com.openswipe.model.SectionRange): String {
    return when {
        section.start == 0f && section.end == 1f -> "全段"
        section.start == 0f && section.end == 1f / 3f -> "左1/3"
        section.start == 1f / 3f && section.end == 2f / 3f -> "中1/3"
        section.start == 2f / 3f && section.end == 1f -> "右1/3"
        section.start == 0f && section.end == 0.5f -> "前半"
        section.start == 0.5f && section.end == 1f -> "后半"
        else -> "${(section.start * 100).toInt()}%-${(section.end * 100).toInt()}%"
    }
}

fun edgeIcon(edge: com.openswipe.overlay.Edge): String = when (edge) {
    com.openswipe.overlay.Edge.LEFT -> "\u2190"   // ←
    com.openswipe.overlay.Edge.RIGHT -> "\u2192"  // →
    com.openswipe.overlay.Edge.BOTTOM -> "\u2193" // ↓
}

fun actionIcon(action: ActionNode): String = when (action) {
    is ActionNode.Back -> "\uD83D\uDD19"
    is ActionNode.Home -> "\uD83C\uDFE0"
    is ActionNode.Recents -> "\uD83D\uDDC2"
    is ActionNode.SwitchLastApp -> "\uD83D\uDD04"
    is ActionNode.LockScreen -> "\uD83D\uDD12"
    is ActionNode.Screenshot -> "\uD83D\uDCF7"
    is ActionNode.SplitScreen -> "\u2B1C"
    is ActionNode.PowerMenu -> "\u23FB"
    is ActionNode.NotificationPanel -> "\uD83D\uDD14"
    is ActionNode.QuickSettings -> "\u2699"
    is ActionNode.MediaPlayPause -> "\u23EF"
    is ActionNode.MediaNext -> "\u23ED"
    is ActionNode.MediaPrevious -> "\u23EE"
    is ActionNode.VolumeUp -> "\uD83D\uDD0A"
    is ActionNode.VolumeDown -> "\uD83D\uDD09"
    is ActionNode.ToggleFlashlight -> "\uD83D\uDD26"
    is ActionNode.NoAction -> "\u26D4"
    is ActionNode.LaunchApp -> "\uD83D\uDCF1"
    else -> "\u2753"
}
