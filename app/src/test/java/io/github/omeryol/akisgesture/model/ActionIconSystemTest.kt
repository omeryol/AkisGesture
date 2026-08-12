package io.github.omeryol.akisgesture.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ActionIconSystemTest {
    private val representatives: List<ActionNode> = ActionNode.allFixed() + listOf(
        ActionNode.LaunchApp("example.launch", "Launch"),
        ActionNode.AppShortcut("example.shortcut", "shortcut", "Shortcut"),
        ActionNode.SendKeyCode(66, "Enter"),
    )

    @Test
    fun everyKnownActionProducesASemanticKey() {
        assertEquals(35, representatives.size)
        representatives.forEach { action ->
            assertNotEquals("${action.id} resolved to UNKNOWN", ActionIconKey.UNKNOWN, action.toIconKey())
        }
    }

    @Test
    fun everySelectablePackCoversEveryKnownKey() {
        val knownKeys = ActionIconKey.entries.filter { it.isKnown }
        assertEquals(34, knownKeys.size)
        ActionIconPack.entries.forEach { pack ->
            knownKeys.forEach { key ->
                assertTrue("Missing $pack/$key", ActionVisualResolver.drawableFor(pack, key) != 0)
            }
        }
    }

    @Test
    fun knownKeysNeverUseUnknownFallback() {
        representatives.forEach { action -> assertTrue(action.toIconKey().isKnown) }
    }

    @Test
    fun assetResourcesAreUniqueWithinEachPack() {
        val knownKeys = ActionIconKey.entries.filter { it.isKnown }
        ActionIconPack.entries.forEach { pack ->
            val resources = knownKeys.associateWith { ActionVisualResolver.drawableFor(pack, it) }
            val duplicates = resources.entries.groupBy { it.value }.filterValues { it.size > 1 }
            assertTrue("Unexpected duplicate resources in $pack: $duplicates", duplicates.isEmpty())
        }
    }

    @Test
    fun identicalVectorAssetsHaveAnExplicitAllowList() {
        val drawableDirectory = listOf(
            File("src/main/res/drawable"),
            File("app/src/main/res/drawable"),
        ).firstOrNull(File::isDirectory) ?: error("Drawable directory not found")
        val duplicates = drawableDirectory.listFiles()
            .orEmpty()
            .filter { it.name.startsWith("action_") && it.extension == "xml" }
            .groupBy { it.readText() }
            .values
            .filter { it.size > 1 }
            .map { files -> files.map(File::getName).toSortedSet() }
            .toSet()
        val allowed = setOf(
            sortedSetOf("action_bootstrap_volume_panel.xml", "action_bootstrap_volume_up.xml"),
            sortedSetOf("action_eva_volume_panel.xml", "action_eva_volume_up.xml"),
            sortedSetOf("action_heroicons_assistant.xml", "action_heroicons_voice_assistant.xml"),
            sortedSetOf("action_iconoir_volume_panel.xml", "action_iconoir_volume_up.xml"),
            sortedSetOf("action_phosphor_volume_panel.xml", "action_phosphor_volume_up.xml"),
            sortedSetOf("action_tabler_assistant.xml", "action_tabler_voice_assistant.xml"),
        )
        assertEquals(allowed, duplicates)
    }

    @Test
    fun packIdsRoundTrip() {
        ActionIconPack.entries.forEach { pack -> assertEquals(pack, ActionIconPack.fromId(pack.id)) }
    }

    @Test
    fun legacyPackIdsMigrateToCanonicalPacks() {
        val expected = mapOf(
            "system_default" to ActionIconPack.PHOSPHOR,
            "emoji_modern" to ActionIconPack.PHOSPHOR,
            "minimal_line" to ActionIconPack.ICONOIR,
            "neon_cyber" to ActionIconPack.TABLER,
            "tech_symbol" to ActionIconPack.TABLER,
            "retro_classic" to ActionIconPack.EVA,
        )
        assertEquals(expected.keys, ActionIconPack.legacyIds)
        expected.forEach { (legacyId, pack) ->
            assertEquals(pack, ActionIconPack.fromId(legacyId))
            assertEquals(pack, ActionIconPack.migrationTarget(legacyId))
        }
        assertNotNull(ActionIconPack.fromId(null))
    }
}
