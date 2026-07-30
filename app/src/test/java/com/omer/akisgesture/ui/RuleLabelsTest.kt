package com.omer.akisgesture.ui

import com.omer.akisgesture.model.SectionRange
import com.omer.akisgesture.overlay.Edge
import com.omer.akisgesture.ui.util.sectionLabel
import org.junit.Assert.assertEquals
import org.junit.Test

class RuleLabelsTest {

    @Test
    fun `vertical thirds use plain position names`() {
        assertEquals("Üst bölüm", sectionLabel(SectionRange.thirds(0), Edge.LEFT))
        assertEquals("Orta bölüm", sectionLabel(SectionRange.thirds(1), Edge.RIGHT))
        assertEquals("Alt bölüm", sectionLabel(SectionRange.thirds(2), Edge.LEFT))
    }

    @Test
    fun `bottom thirds use horizontal position names`() {
        assertEquals("Sol bölüm", sectionLabel(SectionRange.thirds(0), Edge.BOTTOM))
        assertEquals("Orta bölüm", sectionLabel(SectionRange.thirds(1), Edge.BOTTOM))
        assertEquals("Sağ bölüm", sectionLabel(SectionRange.thirds(2), Edge.BOTTOM))
    }

    @Test
    fun `custom range is clearly identified`() {
        assertEquals(
            "Özel alan · %20–%80",
            sectionLabel(SectionRange(0.2f, 0.8f), Edge.LEFT),
        )
    }
}
