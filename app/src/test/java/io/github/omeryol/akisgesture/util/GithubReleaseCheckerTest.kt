package io.github.omeryol.akisgesture.util

import org.junit.Assert.assertEquals
import org.junit.Test

class GithubReleaseCheckerTest {
    private val bilingualNotes = """
        ## 🇹🇷 Türkçe

        - Türkçe yenilik

        ---

        ## 🇬🇧 English

        - English highlight

        ---

        ## 📦 İndirme / Downloads
        - APK
    """.trimIndent()

    @Test
    fun `extracts Turkish section only`() {
        assertEquals("- Türkçe yenilik", GithubReleaseChecker.extractCleanReleaseNotes(bilingualNotes, isTurkish = true))
    }

    @Test
    fun `extracts English section only`() {
        assertEquals("- English highlight", GithubReleaseChecker.extractCleanReleaseNotes(bilingualNotes, isTurkish = false))
    }
}
