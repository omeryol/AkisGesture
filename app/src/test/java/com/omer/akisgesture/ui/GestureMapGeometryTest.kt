package com.omer.akisgesture.ui

import com.omer.akisgesture.model.SectionRange
import com.omer.akisgesture.overlay.Edge
import com.omer.akisgesture.ui.component.GestureMapGeometry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GestureMapGeometryTest {
    @Test
    fun leftThirdMapsToUpperLeftEdge() {
        val rect = GestureMapGeometry.rect(Edge.LEFT, SectionRange.thirds(0))

        assertTrue(rect.contains(0.08f, 0.12f))
        assertFalse(rect.contains(0.50f, 0.12f))
        assertFalse(rect.contains(0.08f, 0.70f))
    }

    @Test
    fun bottomRightThirdMapsToBottomRightEdge() {
        val rect = GestureMapGeometry.rect(Edge.BOTTOM, SectionRange.thirds(2))

        assertTrue(rect.contains(0.80f, 0.90f))
        assertFalse(rect.contains(0.20f, 0.90f))
        assertFalse(rect.contains(0.80f, 0.50f))
    }
}
