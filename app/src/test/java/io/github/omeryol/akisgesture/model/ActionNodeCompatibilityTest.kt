package io.github.omeryol.akisgesture.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ActionNodeCompatibilityTest {

    @Test
    fun unreliableNavBarActionIsNotOfferedForNewRules() {
        assertFalse(ActionNode.allFixed.contains(ActionNode.ToggleNavBar))
    }

    @Test
    fun legacyNavBarActionIdCanStillBeReadFromBackups() {
        assertEquals(ActionNode.ToggleNavBar, ActionNode.fromId("toggle_nav_bar"))
    }
}
