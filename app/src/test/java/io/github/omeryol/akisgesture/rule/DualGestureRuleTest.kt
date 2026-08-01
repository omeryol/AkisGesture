package io.github.omeryol.akisgesture.rule

import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.GestureRule
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.model.SectionRange
import io.github.omeryol.akisgesture.model.TriggerNode
import io.github.omeryol.akisgesture.overlay.Edge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DualGestureRuleTest {

    private val quick = GestureRule(
        id = "quick",
        trigger = TriggerNode(
            Edge.LEFT,
            SectionRange.ALL,
            GestureType.QUICK_SWIPE,
        ),
        action = ActionNode.Back,
    )
    private val hold = GestureRule(
        id = "hold",
        trigger = TriggerNode(
            Edge.LEFT,
            SectionRange.ALL,
            GestureType.SWIPE_HOLD,
        ),
        action = ActionNode.ForceStopForeground,
    )

    @Test
    fun sameAreaCanHaveQuickAndHoldActions() {
        assertTrue(RuleValidator.validate(listOf(quick, hold)).isEmpty())

        val compiled = GestureRuleGraph(listOf(quick, hold)).compile()
        assertEquals(
            ActionNode.Back,
            compiled.match(Edge.LEFT, GestureType.QUICK_SWIPE, 0.5f),
        )
        assertEquals(
            ActionNode.ForceStopForeground,
            compiled.match(Edge.LEFT, GestureType.SWIPE_HOLD, 0.5f),
        )
    }

    @Test
    fun overlappingRulesOfSameGestureStillConflict() {
        val duplicate = quick.copy(id = "duplicate", action = ActionNode.Home)
        assertEquals(1, RuleValidator.validate(listOf(quick, duplicate)).size)
    }
}
