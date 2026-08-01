package io.github.omeryol.akisgesture.rule

import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.GestureRule
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.model.SectionRange
import io.github.omeryol.akisgesture.model.TriggerNode
import io.github.omeryol.akisgesture.overlay.Edge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class RuleProfileResolverTest {

    private val defaultRules = graphWith(ActionNode.Home).compile()
    private val browserRules = graphWith(ActionNode.Back).compile()

    @Test
    fun `default rules are used when app has no profile`() {
        assertSame(
            defaultRules,
            RuleProfileResolver.resolve(
                "com.example.other",
                defaultRules,
                mapOf("com.example.browser" to browserRules),
            ),
        )
    }

    @Test
    fun `foreground app profile overrides default rules`() {
        val resolved = RuleProfileResolver.resolve(
            "com.example.browser",
            defaultRules,
            mapOf("com.example.browser" to browserRules),
        )
        assertSame(browserRules, resolved)
        assertEquals(
            ActionNode.Back,
            resolved.match(Edge.LEFT, GestureType.QUICK_SWIPE, 0.5f),
        )
    }

    private fun graphWith(action: ActionNode) = GestureRuleGraph(
        listOf(
            GestureRule(
                id = action.id,
                trigger = TriggerNode(Edge.LEFT, SectionRange.ALL, GestureType.QUICK_SWIPE),
                action = action,
            ),
        ),
    )
}
