package io.github.omeryol.akisgesture.ui.viewmodel

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.GestureRule
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.model.SectionRange
import io.github.omeryol.akisgesture.model.TriggerMode
import io.github.omeryol.akisgesture.model.TriggerNode
import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.rule.GestureRuleGraph
import io.github.omeryol.akisgesture.rule.Presets
import io.github.omeryol.akisgesture.rule.RuleValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import io.github.omeryol.akisgesture.AkisGestureApp
import io.github.omeryol.akisgesture.R
import io.github.omeryol.akisgesture.ui.util.edgeLabel
import io.github.omeryol.akisgesture.ui.util.gestureLabel
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface PendingActionTarget {
    data class EditRule(val ruleId: String) : PendingActionTarget
    data class AddGesture(
        val edge: Edge,
        val section: SectionRange,
        val gestureType: GestureType,
        val triggerMode: TriggerMode = TriggerMode.SWIPE,
    ) : PendingActionTarget
}

data class Conflict(
    val ruleA: GestureRule,
    val ruleB: GestureRule,
    val message: String,
)

class RuleConfigViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AkisGestureApp
    val gestureConfig = app.gestureConfigFlow

    var pendingTarget: PendingActionTarget? = null

    fun onActionSelected(action: ActionNode) {
        if (action is ActionNode.NoAction) return
        val target = pendingTarget ?: return
        when (target) {
            is PendingActionTarget.EditRule -> {
                updateRuleAction(target.ruleId, action)
            }
            is PendingActionTarget.AddGesture -> {
                addGesturePair(
                    edge = target.edge,
                    section = target.section,
                    quickAction = if (target.gestureType == GestureType.QUICK_SWIPE) action else null,
                    holdAction = if (target.gestureType == GestureType.SWIPE_HOLD) action else null,
                    lUpAction = if (target.gestureType == GestureType.SWIPE_UP_L) action else null,
                    lDownAction = if (target.gestureType == GestureType.SWIPE_DOWN_L) action else null,
                    triggerMode = target.triggerMode,
                )
            }
        }
        pendingTarget = null
    }

    fun setRingMenuEnabled(enabled: Boolean) {
        viewModelScope.launch { app.updateRingMenuEnabled(enabled) }
    }

    fun setRingActions(edge: Edge, actions: List<ActionNode>) {
        viewModelScope.launch { app.updateRingActions(edge, actions.map(ActionNode::id)) }
    }

    fun setRingGroupInsetDp(value: Float) {
        viewModelScope.launch { app.updateRingGroupInsetDp(value) }
    }

    fun setRingGroupSpacingDp(value: Float) {
        viewModelScope.launch { app.updateRingGroupSpacingDp(value) }
    }

    fun setRingSizeDp(value: Float) {
        viewModelScope.launch { app.updateRingSizeDp(value) }
    }

    fun setRingArc(value: Float) {
        viewModelScope.launch { app.updateRingArc(value) }
    }

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

    private val _activeProfilePackage = MutableStateFlow<String?>(null)
    val activeProfilePackage: StateFlow<String?> = _activeProfilePackage.asStateFlow()

    val profilePackages: StateFlow<Set<String>> = app.ruleProfilesFlow
        .map { it.keys }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    init {
        viewModelScope.launch {
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

    fun addRule(trigger: TriggerNode, action: ActionNode, triggerMode: TriggerMode = TriggerMode.SWIPE) {
        if (action is ActionNode.NoAction) return
        val current = _rules.value.toMutableList()
        val existingIndex = current.indexOfFirst {
            it.trigger.edge == trigger.edge && it.trigger.section == trigger.section && it.trigger.gestureType == trigger.gestureType && it.triggerMode == triggerMode
        }
        if (existingIndex >= 0) {
            current[existingIndex] = current[existingIndex].copy(action = action, enabled = true)
        } else {
            current.add(
                GestureRule(
                    id = UUID.randomUUID().toString(),
                    trigger = trigger,
                    action = action,
                    triggerMode = triggerMode,
                )
            )
        }
        _rules.value = current
        _activePresetName.value = null
        revalidate()
        applyRules()
    }

    fun addGesturePair(
        edge: Edge,
        section: SectionRange,
        quickAction: ActionNode?,
        holdAction: ActionNode?,
        lUpAction: ActionNode? = null,
        lDownAction: ActionNode? = null,
        triggerMode: TriggerMode = TriggerMode.SWIPE,
    ) {
        val current = _rules.value.toMutableList()

        fun setAction(type: GestureType, action: ActionNode?) {
            if (action == null || action is ActionNode.NoAction) return
            val existingIndex = current.indexOfFirst {
                it.trigger.edge == edge && it.trigger.section == section && it.trigger.gestureType == type && it.triggerMode == triggerMode
            }
            if (existingIndex >= 0) {
                current[existingIndex] = current[existingIndex].copy(action = action, enabled = true)
            } else {
                current.add(
                    GestureRule(
                        id = UUID.randomUUID().toString(),
                        trigger = TriggerNode(edge, section, type),
                        action = action,
                        triggerMode = triggerMode,
                    )
                )
            }
        }

        setAction(GestureType.QUICK_SWIPE, quickAction)
        setAction(GestureType.SWIPE_HOLD, holdAction)
        setAction(GestureType.SWIPE_UP_L, lUpAction)
        setAction(GestureType.SWIPE_DOWN_L, lDownAction)

        _rules.value = current
        _activePresetName.value = null
        revalidate()
        applyRules()
    }

    fun removeRule(ruleId: String) {
        _rules.value = _rules.value.filter { it.id != ruleId }
        _activePresetName.value = null
        revalidate()
        applyRules()
    }

    fun removeRules(ruleIds: Set<String>) {
        _rules.value = _rules.value.filterNot { it.id in ruleIds }
        _activePresetName.value = null
        revalidate()
        applyRules()
    }

    fun setRulesEnabled(ruleIds: Set<String>, enabled: Boolean) {
        _rules.value = _rules.value.map { rule ->
            if (rule.id in ruleIds) rule.copy(enabled = enabled) else rule
        }
        _activePresetName.value = null
        revalidate()
        applyRules()
    }

    fun updateRuleAction(ruleId: String, newAction: ActionNode) {
        if (newAction is ActionNode.NoAction) return
        _rules.value = _rules.value.map { rule ->
            if (rule.id == ruleId) rule.copy(action = newAction) else rule
        }
        _activePresetName.value = null
        revalidate()
        applyRules()
    }

    fun toggleRuleEnabled(ruleId: String) {
        _rules.value = _rules.value.map { rule ->
            if (rule.id == ruleId) rule.copy(enabled = !rule.enabled) else rule
        }
        _activePresetName.value = null
        revalidate()
        applyRules()
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
        applyRules()
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
        applyRules()
    }

    fun updateRuleTriggerMode(ruleId: String, mode: io.github.omeryol.akisgesture.model.TriggerMode) {
        _rules.value = _rules.value.map { rule ->
            if (rule.id == ruleId) rule.copy(triggerMode = mode) else rule
        }
        _activePresetName.value = null
        revalidate()
        applyRules()
    }

    fun applyRules() {
        val graph = GestureRuleGraph(rules = _rules.value)
        _appliedRules.value = _rules.value.toList()
        val profilePackage = _activeProfilePackage.value
        viewModelScope.launch {
            if (profilePackage == null) {
                app.applyRules(graph)
            } else {
                app.applyProfileRules(profilePackage, graph)
            }
        }
    }

    fun selectProfile(packageName: String?) {
        if (packageName == _activeProfilePackage.value) return
        viewModelScope.launch {
            val graph = if (packageName == null) {
                app.loadSavedRules() ?: Presets.DEFAULT
            } else {
                app.loadRuleProfile(packageName) ?: return@launch
            }
            _activeProfilePackage.value = packageName
            loadGraph(graph)
        }
    }

    fun createProfile(packageName: String) {
        if (packageName == app.packageName) return
        viewModelScope.launch {
            val existing = app.loadRuleProfile(packageName)
            val graph = existing ?: (app.loadSavedRules() ?: Presets.DEFAULT)
            if (existing == null) app.applyProfileRules(packageName, graph)
            _activeProfilePackage.value = packageName
            loadGraph(graph)
        }
    }

    fun removeProfile(packageName: String) {
        viewModelScope.launch {
            app.removeRuleProfile(packageName)
            if (_activeProfilePackage.value == packageName) {
                _activeProfilePackage.value = null
                loadGraph(app.loadSavedRules() ?: Presets.DEFAULT)
            }
        }
    }

    fun loadPreset(name: String, preset: GestureRuleGraph) {
        _rules.value = preset.rules
        _activePresetName.value = name
        revalidate()
        applyRules()
    }

    private fun loadGraph(graph: GestureRuleGraph) {
        _rules.value = graph.rules
        _appliedRules.value = graph.rules
        _activePresetName.value = null
        revalidate()
    }

    // ── Validation ──

    private fun revalidate() {
        val validatorConflicts = RuleValidator.validate(_rules.value)
        _conflicts.value = validatorConflicts.map { c ->
            Conflict(
                ruleA = c.ruleA,
                ruleB = c.ruleB,
                message = app.getString(
                    R.string.rule_conflict,
                    edgeLabel(app, c.ruleA.trigger.edge),
                    gestureLabel(app, c.ruleA.trigger.gestureType),
                ),
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
