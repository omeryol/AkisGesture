package io.github.omeryol.akisgesture.ui.component

import org.junit.Assert.assertEquals
import org.junit.Test

class SideEdgeRangeEditorTest {
    @Test
    fun `start handle keeps the minimum active sensor length`() {
        val result = SideEdgeRangeEditor.drag(
            original = 0.20f to 0.50f,
            handle = SideEdgeRangeEditor.Handle.START,
            delta = 0.25f,
        )

        assertEquals(0.30f, result.first, 0.0001f)
        assertEquals(0.50f, result.second, 0.0001f)
    }

    @Test
    fun `end handle remains within the physical screen`() {
        val result = SideEdgeRangeEditor.drag(
            original = 0.55f to 0.85f,
            handle = SideEdgeRangeEditor.Handle.END,
            delta = 0.50f,
        )

        assertEquals(0.55f, result.first, 0.0001f)
        assertEquals(1f, result.second, 0.0001f)
    }

    @Test
    fun `moving a side preserves its length`() {
        val result = SideEdgeRangeEditor.drag(
            original = 0.20f to 0.60f,
            handle = SideEdgeRangeEditor.Handle.MOVE,
            delta = 0.30f,
        )

        assertEquals(0.50f, result.first, 0.0001f)
        assertEquals(0.90f, result.second, 0.0001f)
    }
}
