package com.openswipe.rule

import com.openswipe.model.GestureRule
import com.openswipe.model.SectionRange

object RuleValidator {

    data class Conflict(
        val ruleA: GestureRule,
        val ruleB: GestureRule,
        val overlapDescription: String
    )

    fun validate(rules: List<GestureRule>): List<Conflict> {
        val enabledRules = rules.filter { it.enabled }
        val conflicts = mutableListOf<Conflict>()
        for (i in enabledRules.indices) {
            for (j in i + 1 until enabledRules.size) {
                val a = enabledRules[i]
                val b = enabledRules[j]
                if (a.trigger.edge == b.trigger.edge
                    && a.trigger.gestureType == b.trigger.gestureType
                    && a.trigger.section.overlapsWith(b.trigger.section)
                ) {
                    val overlapStart = maxOf(a.trigger.section.start, b.trigger.section.start)
                    val overlapEnd = minOf(a.trigger.section.end, b.trigger.section.end)
                    conflicts.add(
                        Conflict(
                            a, b,
                            "${a.trigger.edge} ${a.trigger.gestureType} [${overlapStart}, ${overlapEnd}]"
                        )
                    )
                }
            }
        }
        return conflicts
    }
}
