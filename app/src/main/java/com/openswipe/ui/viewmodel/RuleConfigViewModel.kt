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
import com.openswipe.rule.RuleValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.openswipe.OpenSwipeApp
import com.openswipe.ui.util.edgeLabel
import com.openswipe.ui.util.gestureLabel
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
        viewModelScope.launch {
            val app = getApplication<Application>() as OpenSwipeApp
            val savedGraph = app.loadSavedRules()
            if (savedGraph != null && savedGraph.rules.isNotEmpty()) {
                _rules.value = savedGraph.rules
                _appliedRules.value = savedGraph.rules
                _activePresetName.value = null
                revalidate()
            } else {
                loadPreset("默认", Presets.DEFAULT)
                applyRules()
            }
        }
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

    fun toggleRuleEnabled(ruleId: String) {
        _rules.value = _rules.value.map { rule ->
            if (rule.id == ruleId) rule.copy(enabled = !rule.enabled) else rule
        }
        _activePresetName.value = null
        revalidate()
    }

    fun getRuleById(ruleId: String): GestureRule? {
        return _rules.value.find { it.id == ruleId }
    }

    fun updateRuleTrigger(ruleId: String, newTrigger: TriggerNode) {
        _rules.value = _rules.value.map { rule ->
            if (rule.id == ruleId) rule.copy(trigger = newTrigger) else rule
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
        val validatorConflicts = RuleValidator.validate(_rules.value)
        _conflicts.value = validatorConflicts.map { c ->
            Conflict(
                ruleA = c.ruleA,
                ruleB = c.ruleB,
                message = "${edgeLabel(c.ruleA.trigger.edge)} ${gestureLabel(c.ruleA.trigger.gestureType)} 区段冲突",
            )
        }
    }

    companion object {
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
