package io.github.omeryol.akisgesture.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityControlComponentValidationTest {

    @Test
    fun `valid Android ComponentNames are accepted`() {
        val valid = listOf(
            "io.github.omeryol.akisgesture/.service.GestureAccessibilityService",
            "com.android.talkback/com.google.android.marvin.talkback.TalkBackService",
            "com.example.app/.MyService",
            "org.auto.tools/org.auto.tools.AccService_1",
        )

        for (component in valid) {
            assertTrue("Expected '$component' to be valid", AccessibilityControl.isValidComponentName(component))
        }
    }

    @Test
    fun `shell injection and invalid formats are strictly rejected`() {
        val invalid = listOf(
            "io.github.omeryol.akisgesture/.Service; rm -rf /",
            "com.foo/com.foo.Bar && reboot",
            "com.foo/com.foo.Bar`id`",
            "com.foo/com.foo.Bar\$(id)",
            "com.foo/com.foo.Bar' || echo pwned",
            "com.foo/com.foo.Bar|sh",
            "invalid_without_slash",
            "com.foo/",
            "/com.foo.Bar",
            "",
            "   ",
            "com.foo/bar with space",
            "com.foo/bar\nnewline",
        )

        for (malicious in invalid) {
            assertFalse("Expected '$malicious' to be rejected", AccessibilityControl.isValidComponentName(malicious))
        }
    }
}
