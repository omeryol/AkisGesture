package com.omer.akisgesture.gesture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppPausePolicyTest {
    private val paused = setOf("com.example.bank", "com.example.game")

    @Test
    fun pausesOnlyForConfiguredForegroundPackage() {
        assertTrue(AppPausePolicy.shouldPause("com.example.bank", paused))
        assertFalse(AppPausePolicy.shouldPause("com.example.mail", paused))
    }

    @Test
    fun missingForegroundPackageNeverPausesGestures() {
        assertFalse(AppPausePolicy.shouldPause(null, paused))
        assertFalse(AppPausePolicy.shouldPause("", paused))
    }

    @Test
    fun leavingPausedAppResumesPolicy() {
        assertTrue(AppPausePolicy.shouldPause("com.example.game", paused))
        assertFalse(AppPausePolicy.shouldPause("com.example.launcher", paused))
    }
}
