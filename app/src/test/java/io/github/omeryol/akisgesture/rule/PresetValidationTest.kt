package io.github.omeryol.akisgesture.rule

import org.junit.Assert.assertTrue
import org.junit.Test

class PresetValidationTest {
    @Test
    fun everyPresetHasRulesAndNoConflicts() {
        Presets.ALL.forEach { (name, graph) ->
            assertTrue("$name boş olmamalı", graph.rules.isNotEmpty())
            assertTrue("$name çakışma içeriyor", RuleValidator.validate(graph.rules).isEmpty())
        }
    }

    @Test
    fun presetRangeCoversBeginnerGeneralAndAdvancedUse() {
        val names = Presets.ALL.map { it.first }
        assertTrue(names.any { it.startsWith("Başlangıç") })
        assertTrue(names.any { it.startsWith("Genel") })
        assertTrue(names.any { it.startsWith("İleri") })
        assertTrue(names.any { it.startsWith("Root") })
    }
}
