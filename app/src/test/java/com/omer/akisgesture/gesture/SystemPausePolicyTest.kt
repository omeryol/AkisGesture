package com.omer.akisgesture.gesture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemPausePolicyTest {
    @Test
    fun pausesOnlyForEnabledContexts() {
        val config = GestureConfig(
            pauseOnLockScreen = true,
            pauseWhenKeyboardVisible = true,
            pauseInLandscape = false,
        )

        assertTrue(SystemPausePolicy.shouldPause(config, true, false, false))
        assertTrue(SystemPausePolicy.shouldPause(config, false, true, false))
        assertFalse(SystemPausePolicy.shouldPause(config, false, false, true))
        assertFalse(SystemPausePolicy.shouldPause(config, false, false, false))
    }
}
