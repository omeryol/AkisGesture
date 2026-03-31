package com.openswipe.rule

import com.openswipe.model.ActionNode
import com.openswipe.model.GestureType
import com.openswipe.overlay.Edge

class CompiledRuleSet(
    private val table: Map<Edge, Map<GestureType, List<CompiledSection>>>
) {
    /**
     * Core matching method. Runtime hot path.
     *
     * @param edge which edge
     * @param gestureType what gesture
     * @param sectionRatio touch position ratio along the edge [0.0, 1.0]
     * @return matched ActionNode, or null if no match
     */
    fun match(edge: Edge, gestureType: GestureType, sectionRatio: Float): ActionNode? {
        val sections = table[edge]?.get(gestureType) ?: return null
        for (section in sections) {
            if (sectionRatio < section.start) return null
            if (sectionRatio <= section.end) return section.action
        }
        return null
    }

    /** Whether any rules exist for the given edge (any gesture type). */
    fun hasRulesFor(edge: Edge): Boolean = table.containsKey(edge)

    /** Total number of compiled sections (actions) for a given edge across all gesture types. */
    fun ruleCountFor(edge: Edge): Int =
        table[edge]?.values?.sumOf { it.size } ?: 0

    /** Total number of compiled sections across all edges. */
    fun totalRuleCount(): Int =
        table.values.sumOf { gestures -> gestures.values.sumOf { it.size } }

    companion object {
        val EMPTY = CompiledRuleSet(emptyMap())
    }
}

data class CompiledSection(
    val start: Float,
    val end: Float,
    val action: ActionNode
)
