package io.github.omeryol.akisgesture.gesture

import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.model.ActionNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GestureConfigTest {
    @Test
    fun perEdgeEnabledFlagsAreApplied() {
        val config = GestureConfig(leftEnabled = false, rightEnabled = true, bottomEnabled = false)
        assertFalse(config.isEnabled(Edge.LEFT))
        assertTrue(config.isEnabled(Edge.RIGHT))
        assertFalse(config.isEnabled(Edge.BOTTOM))
    }

    @Test
    fun ringMenuIsEnabledByDefaultButNeedsAnAssignedAction() {
        val config = GestureConfig()
        assertTrue(config.ringMenuEnabled)
        assertFalse(config.hasRingActionsFor(Edge.LEFT))
    }

    @Test
    fun ringMenuResolvesOnlyThreeValidActionsForItsOwnEdge() {
        val config = GestureConfig(
            leftRingActionIds = listOf("back", "no_action", "home", "recents", "screenshot"),
            rightRingActionIds = listOf("notification_panel"),
        )
        assertEquals(listOf(ActionNode.Back, ActionNode.Home, ActionNode.Recents), config.ringActionsFor(Edge.LEFT))
        assertEquals(listOf(ActionNode.NotificationPanel), config.ringActionsFor(Edge.RIGHT))
        assertFalse(config.hasRingActionsFor(Edge.BOTTOM))
    }
}
