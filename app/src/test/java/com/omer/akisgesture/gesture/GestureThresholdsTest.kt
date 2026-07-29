package com.omer.akisgesture.gesture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GestureThresholdsTest {
    @Test
    fun dampingAndThresholdMatchDetectorCalibration() {
        val damped = GestureThresholds.dampedDisplacement(
            rawDisplacement = 60f,
            dampingFactor = 2f,
        )

        assertTrue(GestureThresholds.isQuickArmed(damped, threshold = 30f))
        assertFalse(GestureThresholds.isQuickArmed(damped, threshold = 31f))
    }

    @Test
    fun holdNeedsBothDistanceAndElapsedTime() {
        assertFalse(GestureThresholds.isHoldArmed(30f, 30f, 499L, 500L))
        assertTrue(GestureThresholds.isHoldArmed(30f, 30f, 500L, 500L))
        assertFalse(GestureThresholds.isHoldArmed(29f, 30f, 700L, 500L))
    }
}
