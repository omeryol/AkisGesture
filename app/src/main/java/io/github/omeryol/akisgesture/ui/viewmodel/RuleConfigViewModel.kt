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
import io.github.omeryol.akisgesture.ui.util.edgeLabel
import io.github.omeryol.akisgesture.ui.util.gestureLabel
import kotlinx.coroutines.launch
import java.util.UUID

data class Conflict(
    val ruleA: GestureRule,
    val ruleB: GestureRule,
    val message: String,
)

class RuleConfigViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AkisGestureApp
    val gestureConfig = app.gestureConfigFlow

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

    fun addRule(trigger: TriggerNode, action: ActionNode, triggerMode: io.github.omeryol.akisgesture.model.TriggerMode = io.github.omeryol.akisgesture.model.TriggerMode.SWIPE) {
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
        lUpAction: ActionNode? = null,
        lDownAction: ActionNode? = null,
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
            lUpAction?.let { action ->
                add(
                    GestureRule(
                        id = UUID.randomUUID().toString(),
                        trigger = TriggerNode(edge, section, GestureType.SWIPE_UP_L),
                        action = action,
                        triggerMode = triggerMode,
                    ),
                )
            }
            lDownAction?.let { action ->
                add(
                    GestureRule(
                        id = UUID.randomUUID().toString(),
                        trigger = TriggerNode(edge, section, GestureType.SWIPE_DOWN_L),
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
        applyRules()
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
        applyRules()
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

    fun updateRuleTriggerMode(ruleId: String, mode: io.github.omeryol.akisgesture.model.TriggerMode) {
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
