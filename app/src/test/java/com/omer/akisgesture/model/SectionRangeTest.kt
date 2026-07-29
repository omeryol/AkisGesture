package com.omer.akisgesture.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SectionRangeTest {
    @Test
    fun customRangeRetainsPositionAndLength() {
        val range = SectionRange(0.2f, 0.7f)
        assertEquals(0.5f, range.length, 0.0001f)
        assertTrue(range.contains(0.45f))
        assertFalse(range.contains(0.8f))
    }

    @Test
    fun touchingRangesDoNotConflictButOverlappingRangesDo() {
        val first = SectionRange(0f, 0.4f)
        assertFalse(first.overlapsWith(SectionRange(0.4f, 0.8f)))
        assertTrue(first.overlapsWith(SectionRange(0.3f, 0.8f)))
    }
}
