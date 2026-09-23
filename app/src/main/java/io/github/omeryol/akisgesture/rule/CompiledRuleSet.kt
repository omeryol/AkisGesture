package io.github.omeryol.akisgesture.rule

import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.model.TriggerMode
import io.github.omeryol.akisgesture.overlay.Edge

class CompiledRuleSet(
    private val table: Map<Edge, Map<GestureType, List<CompiledSection>>>,
) {
    /**
     * Core matching method. Runtime hot path.
     *
     * @param edge which edge
     * @param gestureType what gesture
     * @param sectionRatio touch position ratio along the edge [0.0, 1.0]
     * @return matched ActionNode, or null if no match
     */
    fun match(edge: Edge, gestureType: GestureType, sectionRatio: Float): ActionNode? =
        matchSection(edge, gestureType, sectionRatio)?.action

    /**
     * Trigger mode (TOUCH/SWIPE) for the QUICK_SWIPE rule covering [sectionRatio] on [edge],
     * or TOUCH if no such rule exists there. Resolved per touch position rather than
     * aggregated per edge, so a TOUCH-mode section is never silently overridden by a
     * SWIPE-mode rule configured in a different section of the same edge.
     */
    fun quickSwipeTriggerModeFor(edge: Edge, sectionRatio: Float): TriggerMode =
        matchSection(edge, GestureType.QUICK_SWIPE, sectionRatio)?.triggerMode ?: TriggerMode.TOUCH

    private fun matchSection(edge: Edge, gestureType: GestureType, sectionRatio: Float): CompiledSection? {
        val sections = table[edge]?.get(gestureType) ?: return null
        for (section in sections) {
            if (sectionRatio >= section.start && sectionRatio <= section.end) return section
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
    val action: ActionNode,
    val triggerMode: TriggerMode = TriggerMode.TOUCH,
)
