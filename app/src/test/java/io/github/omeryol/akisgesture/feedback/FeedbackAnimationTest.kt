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
    fun `legacy stored names migrate to canonical styles`() {
        assertTrue(FeedbackAnimation.entries.count { it != FeedbackAnimation.NONE } == 15)
        assertTrue(FeedbackAnimation.fromStoredName("MATRIX_DISSOLVE") == FeedbackAnimation.INK_FLOW)
        assertTrue(FeedbackAnimation.fromStoredName("PRISM_SHATTER") == FeedbackAnimation.GLASS_RIPPLE)
        assertTrue(FeedbackAnimation.fromStoredName("ELECTRIC_STORM") == FeedbackAnimation.HYDRO_WIPE)
    }
}
