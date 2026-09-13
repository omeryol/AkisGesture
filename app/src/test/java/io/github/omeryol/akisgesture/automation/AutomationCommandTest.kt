package io.github.omeryol.akisgesture.automation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomationCommandTest {

    @Test
    fun `canonical and legacy action names resolve to their commands`() {
        assertEquals(AutomationCommand.Command.START, AutomationCommand.fromAction(AutomationCommand.ACTION_START))
        assertEquals(AutomationCommand.Command.STOP, AutomationCommand.fromAction(AutomationCommand.ACTION_STOP))
        assertEquals(AutomationCommand.Command.TOGGLE, AutomationCommand.fromAction(AutomationCommand.ACTION_TOGGLE))
        assertEquals(AutomationCommand.Command.START, AutomationCommand.fromAction(AutomationCommand.LEGACY_ACTION_START))
        assertEquals(AutomationCommand.Command.STOP, AutomationCommand.fromAction(AutomationCommand.LEGACY_ACTION_STOP))
        assertEquals(AutomationCommand.Command.TOGGLE, AutomationCommand.fromAction(AutomationCommand.LEGACY_ACTION_TOGGLE))
    }

    @Test
    fun `unknown or malformed actions are rejected`() {
        val rejected = listOf(
            null,
            "",
            "   ",
            "START",
            "io.github.omeryol.akisgesture.action.START ",
            "io.github.omeryol.akisgesture.action.STARTX",
            "io.github.omeryol.akisgesture.action.DELETE_ALL",
            "com.openswipe.action.START; rm -rf /",
            "com.openswipe.action.START\nio.github.omeryol.akisgesture.action.STOP",
            "io.github.omeryol.akisgesture.action.START\u0000",
            "\${io.github.omeryol.akisgesture.action.START}",
        )

        for (value in rejected) {
            assertNull("Expected rejection for '$value'", AutomationCommand.fromAction(value))
        }
    }

    @Test
    fun `plugin values accept only exact lowercase keywords`() {
        assertEquals(AutomationCommand.Command.START, AutomationCommand.fromPluginValue("start"))
        assertEquals(AutomationCommand.Command.STOP, AutomationCommand.fromPluginValue("stop"))
        assertEquals(AutomationCommand.Command.TOGGLE, AutomationCommand.fromPluginValue("toggle"))

        val rejected = listOf(
            null, "", " ", "START", "Start", "start;stop", "start\n", "restart", "enable", "true",
        )
        for (value in rejected) {
            assertNull("Expected rejection for '$value'", AutomationCommand.fromPluginValue(value))
        }
    }

    @Test
    fun `plugin value and action round trip for every command`() {
        for (command in AutomationCommand.Command.values()) {
            assertEquals(
                command,
                AutomationCommand.fromPluginValue(AutomationCommand.pluginValueOf(command)),
            )
            assertEquals(
                command,
                AutomationCommand.fromAction(AutomationCommand.actionOf(command)),
            )
        }
    }

    @Test
    fun `target state follows the command semantics`() {
        assertTrue(AutomationCommand.targetState(AutomationCommand.Command.START, currentlyEnabled = false))
        assertTrue(AutomationCommand.targetState(AutomationCommand.Command.START, currentlyEnabled = true))

        assertFalse(AutomationCommand.targetState(AutomationCommand.Command.STOP, currentlyEnabled = true))
        assertFalse(AutomationCommand.targetState(AutomationCommand.Command.STOP, currentlyEnabled = false))

        assertTrue(AutomationCommand.targetState(AutomationCommand.Command.TOGGLE, currentlyEnabled = false))
        assertFalse(AutomationCommand.targetState(AutomationCommand.Command.TOGGLE, currentlyEnabled = true))
    }

    @Test
    fun `declared actions cover canonical and legacy names only`() {
        assertEquals(
            setOf(
                AutomationCommand.ACTION_START,
                AutomationCommand.ACTION_STOP,
                AutomationCommand.ACTION_TOGGLE,
                AutomationCommand.LEGACY_ACTION_START,
                AutomationCommand.LEGACY_ACTION_STOP,
                AutomationCommand.LEGACY_ACTION_TOGGLE,
            ),
            AutomationCommand.declaredActions,
        )
    }

    @Test
    fun `plugin bundles accept only the command key`() {
        assertTrue(LocalePluginProtocol.isAcceptedBundle(setOf(LocalePluginProtocol.KEY_COMMAND)))
        assertTrue(
            LocalePluginProtocol.isAcceptedBundle(
                LocalePluginProtocol.acceptedKeysOf(LocalePluginProtocol.KEY_COMMAND, null),
            ),
        )

        assertFalse("Empty bundle must be rejected", LocalePluginProtocol.isAcceptedBundle(emptySet()))
        assertFalse(
            "Unknown keys must be rejected",
            LocalePluginProtocol.isAcceptedBundle(
                setOf(LocalePluginProtocol.KEY_COMMAND, "shell_command"),
            ),
        )
        assertFalse(
            "Oversized bundles must be rejected",
            LocalePluginProtocol.isAcceptedBundle(
                (0..LocalePluginProtocol.MAX_BUNDLE_KEYS).map { "key$it" }.toSet(),
            ),
        )
    }
}
