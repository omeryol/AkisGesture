package io.github.omeryol.akisgesture.gesture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GestureCancelPolicyTest {
    @Test
    fun armedGestureCancelsWhenFingerReturnsToEdge() {
        assertTrue(
            GestureCancelPolicy.shouldCancel(
                wasArmed = true,
                inwardDisplacement = 7f,
                activationThreshold = 30f,
            ),
        )
    }

    @Test
    fun smallWobbleDoesNotCancelArmedGesture() {
        assertFalse(GestureCancelPolicy.shouldCancel(true, 22f, 30f))
    }

    @Test
    fun gestureThatWasNeverArmedCannotEnterCancelState() {
        assertFalse(GestureCancelPolicy.shouldCancel(false, 0f, 30f))
    }

    @Test
    fun configuredHysteresisControlsReturnDistance() {
        assertTrue(GestureCancelPolicy.shouldCancel(true, 14f, 30f, hysteresisRatio = 0.5f))
        assertFalse(GestureCancelPolicy.shouldCancel(true, 16f, 30f, hysteresisRatio = 0.5f))
    }
}
