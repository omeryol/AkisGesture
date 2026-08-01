package io.github.omeryol.akisgesture.gesture

import io.github.omeryol.akisgesture.gesture.model.SwipeDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomAppSwitchPolicyTest {
    @Test
    fun `horizontal direction wins only after slop and over vertical movement`() {
        assertEquals(SwipeDirection.RIGHT, BottomAppSwitchPolicy.direction(45f, 8f, 12f))
        assertEquals(SwipeDirection.LEFT, BottomAppSwitchPolicy.direction(-45f, 8f, 12f))
        assertNull(BottomAppSwitchPolicy.direction(8f, 1f, 12f))
        assertNull(BottomAppSwitchPolicy.direction(30f, 42f, 12f))
    }

    @Test
    fun `arming uses absolute horizontal distance`() {
        assertFalse(BottomAppSwitchPolicy.isArmed(29f, 30f))
        assertTrue(BottomAppSwitchPolicy.isArmed(-30f, 30f))
    }
}
