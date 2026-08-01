package io.github.omeryol.akisgesture.feedback

import org.junit.Assert.assertTrue
import org.junit.Test

class FeedbackAnimationTest {
    @Test
    fun everyStyleHasBoundedPhysicalCoefficients() {
        FeedbackAnimation.entries.forEach { style ->
            assertTrue(style.viscosity in 0f..1f)
            assertTrue(style.surfaceTension in 0f..1f)
            assertTrue(style.damping in 0f..1f)
        }
    }

    @Test
    fun legacyStylesRemainAvailableForSavedPreferences() {
        val names = FeedbackAnimation.entries.map { it.name }.toSet()
        assertTrue("OCEAN_WAVE" in names)
        assertTrue("MATRIX_DISSOLVE" in names)
        assertTrue("PRISM_SHATTER" in names)
    }
}
