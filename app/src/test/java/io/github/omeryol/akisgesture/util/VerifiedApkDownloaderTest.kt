package io.github.omeryol.akisgesture.util

import org.junit.Assert.assertEquals
import org.junit.Test

class VerifiedApkDownloaderTest {

    @Test
    fun `sanitizeAssetName accepts standard apk name`() {
        val result = VerifiedApkDownloader.sanitizeAssetName("AkisGesture-v1.9.0.apk", "1.9.0")
        assertEquals("AkisGesture-v1.9.0.apk", result)
    }

    @Test
    fun `sanitizeAssetName rejects path traversal attempts and falls back to safe name`() {
        val traversalAttempts = listOf(
            "../../etc/passwd",
            "../bad.apk",
            "..\\bad.apk",
            "/absolute/path/bad.apk",
            "foo/bar.apk",
            "bad file with spaces.apk",
            "bad;command.apk",
            "name\u0000zero.apk",
            null,
            "",
            "   ",
        )

        for (attempt in traversalAttempts) {
            val result = VerifiedApkDownloader.sanitizeAssetName(attempt, "1.9.0")
            assertEquals("AkisGesture-v1.9.0.apk", result)
        }
    }
}
