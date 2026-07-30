package com.omer.akisgesture.ui

import com.omer.akisgesture.model.ActionNode
import com.omer.akisgesture.ui.util.filterActions
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
