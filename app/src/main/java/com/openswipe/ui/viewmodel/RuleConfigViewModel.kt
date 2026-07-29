package com.omer.akisgesture.ui.viewmodel

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.omer.akisgesture.model.ActionNode
import com.omer.akisgesture.model.GestureRule
import com.omer.akisgesture.model.GestureType
import com.omer.akisgesture.model.SectionRange
import com.omer.akisgesture.model.TriggerMode
import com.omer.akisgesture.model.TriggerNode
import com.omer.akisgesture.overlay.Edge
import com.omer.akisgesture.rule.GestureRuleGraph
import com.omer.akisgesture.rule.Presets
import com.omer.akisgesture.rule.RuleValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.omer.akisgesture.AkisGestureApp
import com.omer.akisgesture.ui.util.edgeLabel
import com.omer.akisgesture.ui.util.gestureLabel
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
            val app = getApplication<Application>() as AkisGestureApp
            val savedGraph = app.loadSavedRules()
            if (savedGraph != null && savedGraph.rules.isNotEmpty()) {
                _rules.value = savedGraph.rules
                _appliedRules.value = savedGraph.rules
                _activePresetName.value = null
                revalidate()
            } else {
                loadPreset("Genel · Dengeli", Presets.DEFAULT)
                applyRules()
            }
        }
    }

    // ── Mutations ──

    fun addRule(trigger: TriggerNode, action: ActionNode, triggerMode: com.omer.akisgesture.model.TriggerMode = com.omer.akisgesture.model.TriggerMode.SWIPE) {
        val newRule = GestureRule(
            id = UUID.randomUUID().toString(),
            trigger = trigger,
            action = action,
            triggerMode = triggerMode,
        )
        _rules.value = _rules.value + newRule
        _activePresetName.value = null
        revalidate()
    }

    fun addGesturePair(
        edge: Edge,
        section: SectionRange,
        quickAction: ActionNode?,
        holdAction: ActionNode?,
        triggerMode: TriggerMode = TriggerMode.SWIPE,
    ) {
        val additions = buildList {
            quickAction?.let { action ->
                add(
                    GestureRule(
                        id = UUID.randomUUID().toString(),
                        trigger = TriggerNode(edge, section, GestureType.QUICK_SWIPE),
                        action = action,
                        triggerMode = triggerMode,
                    ),
                )
            }
            holdAction?.let { action ->
                add(
                    GestureRule(
                        id = UUID.randomUUID().toString(),
                        trigger = TriggerNode(edge, section, GestureType.SWIPE_HOLD),
                        action = action,
                        triggerMode = triggerMode,
                    ),
                )
            }
        }
        if (additions.isEmpty()) return
        _rules.value = _rules.value + additions
        _activePresetName.value = null
        revalidate()
    }

    fun removeRule(ruleId: String) {
        _rules.value = _rules.value.filter { it.id != ruleId }
        _activePresetName.value = null
        revalidate()
    }

    fun removeRules(ruleIds: Set<String>) {
        _rules.value = _rules.value.filterNot { it.id in ruleIds }
        _activePresetName.value = null
        revalidate()
    }

    fun setRulesEnabled(ruleIds: Set<String>, enabled: Boolean) {
        _rules.value = _rules.value.map { rule ->
            if (rule.id in ruleIds) rule.copy(enabled = enabled) else rule
        }
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

    fun updateRulesSection(ruleIds: Set<String>, section: SectionRange) {
        _rules.value = _rules.value.map { rule ->
            if (rule.id in ruleIds) {
                rule.copy(trigger = rule.trigger.copy(section = section))
            } else {
                rule
            }
        }
        _activePresetName.value = null
        revalidate()
    }

    fun updateRuleTriggerMode(ruleId: String, mode: com.omer.akisgesture.model.TriggerMode) {
        _rules.value = _rules.value.map { rule ->
            if (rule.id == ruleId) rule.copy(triggerMode = mode) else rule
        }
        _activePresetName.value = null
        revalidate()
    }

    fun applyRules() {
        if (_conflicts.value.isNotEmpty()) return
        val graph = GestureRuleGraph(rules = _rules.value)
        _appliedRules.value = _rules.value.toList()
        viewModelScope.launch {
            (getApplication<Application>() as AkisGestureApp).applyRules(graph)
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
                message = "${edgeLabel(c.ruleA.trigger.edge)} ${gestureLabel(c.ruleA.trigger.gestureType)} alanı çakışıyor",
            )
        }
    }

    companion object {
        fun isActionAvailable(action: ActionNode): Boolean {
            return Build.VERSION.SDK_INT >= action.minApi
        }

        val presets: List<Pair<String, GestureRuleGraph>> = Presets.ALL
    }
}
