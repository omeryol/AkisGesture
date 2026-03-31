package com.openswipe.rule

import com.openswipe.model.GestureRule
import com.openswipe.model.GestureType
import com.openswipe.model.TriggerMode

data class GestureRuleGraph(
    val rules: List<GestureRule>
) {
    fun validate(): List<RuleValidator.Conflict> = RuleValidator.validate(rules)

    fun compile(): CompiledRuleSet {
        val table = mutableMapOf<com.openswipe.overlay.Edge, MutableMap<GestureType, MutableList<CompiledSection>>>()

        for (rule in rules) {
            if (!rule.enabled) continue
            val edge = rule.trigger.edge
            val gestureType = rule.trigger.gestureType

            table
                .getOrPut(edge) { mutableMapOf() }
                .getOrPut(gestureType) { mutableListOf() }
                .add(
                    CompiledSection(
                        start = rule.trigger.section.start,
                        end = rule.trigger.section.end,
                        action = rule.action
                    )
                )
        }

        // Sort each group by start for early-exit matching
        for ((_, byGesture) in table) {
            for ((_, sections) in byGesture) {
                sections.sortBy { it.start }
            }
        }

        // Aggregate trigger mode per edge: if ANY enabled rule uses SWIPE, the edge uses SWIPE
        val edgeTriggerModes = mutableMapOf<com.openswipe.overlay.Edge, TriggerMode>()
        for (rule in rules) {
            if (!rule.enabled) continue
            val edge = rule.trigger.edge
            val current = edgeTriggerModes[edge]
            if (current == null) {
                edgeTriggerModes[edge] = rule.triggerMode
            } else if (rule.triggerMode == TriggerMode.SWIPE) {
                edgeTriggerModes[edge] = TriggerMode.SWIPE
            }
        }

        return CompiledRuleSet(table, edgeTriggerModes)
    }
}
