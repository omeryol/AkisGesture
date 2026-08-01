package io.github.omeryol.akisgesture.ui

import io.github.omeryol.akisgesture.model.SectionRange
import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.ui.component.GestureMapGeometry
import io.github.omeryol.akisgesture.ui.component.RangeDragHandle
import io.github.omeryol.akisgesture.ui.component.SectionRangeEditor
import org.junit.Assert.assertEquals
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

    @Test
    fun centerDragKeepsLengthAndClampsAtScreenEnd() {
        val moved = SectionRangeEditor.drag(
            original = SectionRange(0.60f, 0.90f),
            handle = RangeDragHandle.CENTER,
            delta = 0.40f,
        )

        assertEquals(0.70f, moved.start, 0.0001f)
        assertEquals(1.00f, moved.end, 0.0001f)
    }

    @Test
    fun edgeDragCannotCollapseZone() {
        val resized = SectionRangeEditor.drag(
            original = SectionRange(0.20f, 0.60f),
            handle = RangeDragHandle.START,
            delta = 0.39f,
        )

        assertEquals(0.48f, resized.start, 0.0001f)
        assertEquals(0.60f, resized.end, 0.0001f)
    }

    @Test
    fun visualContentCoordinateMapsBackToSectionCoordinate() {
        assertEquals(
            0.50f,
            GestureMapGeometry.toSectionPosition(0.50f),
            0.0001f,
        )
        assertEquals(
            0.25f,
            GestureMapGeometry.toSectionDelta(0.21f),
            0.0001f,
        )
    }
}
