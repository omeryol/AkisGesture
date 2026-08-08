package io.github.omeryol.akisgesture.model

import org.junit.Assert.assertNull
import org.junit.Test

class ActionNodeCompatibilityTest {

    @Test
    fun legacyNavBarActionIsRejected() {
        assertNull(ActionNode.fromId("toggle_nav_bar"))
    }
}
