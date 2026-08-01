package io.github.omeryol.akisgesture.service

import org.junit.Assert.assertEquals
import org.junit.Test

class AccessibilityHealthPolicyTest {

    @Test
    fun `user stop never triggers repair`() {
        assertEquals(
            AccessibilityHealthPolicy.Action.NONE,
            AccessibilityHealthPolicy.decide(
                desired = false,
                settingEnabled = false,
                serviceConnected = false,
                millisSinceLastRepair = Long.MAX_VALUE,
            ),
        )
    }

    @Test
    fun `missing setting is enabled`() {
        assertEquals(
            AccessibilityHealthPolicy.Action.ENABLE_SETTING,
            AccessibilityHealthPolicy.decide(
                desired = true,
                settingEnabled = false,
                serviceConnected = false,
                millisSinceLastRepair = Long.MAX_VALUE,
            ),
        )
    }

    @Test
    fun `enabled but disconnected service is rebound`() {
        assertEquals(
            AccessibilityHealthPolicy.Action.REBIND_SERVICE,
            AccessibilityHealthPolicy.decide(
                desired = true,
                settingEnabled = true,
                serviceConnected = false,
                millisSinceLastRepair = Long.MAX_VALUE,
            ),
        )
    }

    @Test
    fun `healthy service is left untouched`() {
        assertEquals(
            AccessibilityHealthPolicy.Action.NONE,
            AccessibilityHealthPolicy.decide(
                desired = true,
                settingEnabled = true,
                serviceConnected = true,
                millisSinceLastRepair = Long.MAX_VALUE,
            ),
        )
    }

    @Test
    fun `repair cooldown prevents repeated toggling`() {
        assertEquals(
            AccessibilityHealthPolicy.Action.NONE,
            AccessibilityHealthPolicy.decide(
                desired = true,
                settingEnabled = true,
                serviceConnected = false,
                millisSinceLastRepair = 5_000,
            ),
        )
    }
}
