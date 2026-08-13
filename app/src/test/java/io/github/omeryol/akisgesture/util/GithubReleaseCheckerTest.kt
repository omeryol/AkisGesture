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

    @Test
    fun `extracts SHA-256 hash from release body text`() {
        val body = """
            ## 📦 İndirme / Downloads
            - **İmzalı APK**: `AkisGesture-v1.7.0.apk`
            - **SHA-256**: `4556B73B633839FDE797FC8A775EA73B64738107B3E29F8818001CD45D65B0D6`
        """.trimIndent()
        assertEquals(
            "4556B73B633839FDE797FC8A775EA73B64738107B3E29F8818001CD45D65B0D6",
            GithubReleaseChecker.extractSha256FromText(body)
        )
    }
}
