package io.github.omeryol.akisgesture.ui

import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.ui.util.filterActions
import org.junit.Assert.assertEquals
import org.junit.Test

class ActionSearchTest {

    private val actions = listOf(
        ActionNode.QuickSettings,
        ActionNode.ToggleFlashlight,
        ActionNode.LaunchApp("com.whatsapp", "WhatsApp"),
    )

    @Test
    fun `search matches Turkish action labels`() {
        assertEquals(
            listOf(ActionNode.QuickSettings),
            filterActions(actions, "hızlı"),
        )
    }

    @Test
    fun `search can use localized action labels`() {
        assertEquals(
            listOf(ActionNode.QuickSettings),
            filterActions(actions, "quick") { action ->
                if (action == ActionNode.QuickSettings) "Quick settings" else action.label
            },
        )
    }

    @Test
    fun `search matches application name or package`() {
        val app = actions.last()
        assertEquals(listOf(app), filterActions(actions, "whatsapp"))
        assertEquals(listOf(app), filterActions(actions, "com.whats"))
    }

    @Test
    fun `blank search keeps supplied ordering`() {
        assertEquals(actions, filterActions(actions, "  "))
    }
}
