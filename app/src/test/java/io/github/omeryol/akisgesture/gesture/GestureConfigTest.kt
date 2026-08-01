package io.github.omeryol.akisgesture.gesture

import io.github.omeryol.akisgesture.overlay.Edge
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
}
