package io.github.omeryol.akisgesture.util

import org.json.JSONObject
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class GithubRelease(
    val version: String,
    val url: String,
    val notes: String,
    val downloadUrl: String?,
    val publishedAt: String?,
    val assetName: String?,
    val assetSha256: String?,
)

data class GithubReleaseHistoryItem(
    val version: String,
    val date: String,
    val changesTr: List<String>,
    val changesEn: List<String>,
)

class ReleaseValidationException(message: String) : IllegalStateException(message)

object GithubReleaseChecker {
    private const val LATEST_RELEASE_URL =
        "https://api.github.com/repos/omeryol/AkisGesture/releases/latest"
    private const val RELEASE_HISTORY_URL =
        "https://api.github.com/repos/omeryol/AkisGesture/releases?per_page=30"

    fun fetchLatestRelease(): GithubRelease {
        val connection = (URL(LATEST_RELEASE_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 8_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "AkisGesture-Android")
        }
        return try {
            check(connection.responseCode in 200..299) {
                "GitHub HTTP ${connection.responseCode}"
            }
            val release = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            if (release.optBoolean("draft") || release.optBoolean("prerelease")) {
                throw ReleaseValidationException("Latest release is not a stable release")
            }
            val version = release.getString("tag_name").removePrefix("v")
            val expectedAssetName = "AkisGesture-v$version.apk"
            val asset = release.optJSONArray("assets")
                ?.let { assets ->
                    (0 until assets.length())
                        .map { assets.getJSONObject(it) }
                        .firstOrNull { item ->
                            val name = item.optString("name")
                            val label = item.optString("label")
                            name == expectedAssetName || label == expectedAssetName ||
                            name.endsWith(".apk", ignoreCase = true) || label.endsWith(".apk", ignoreCase = true)
                        }
                }
                ?: throw ReleaseValidationException("Expected APK asset is missing")
            val downloadUrl = asset.optString("browser_download_url").takeIf(String::isNotBlank)
                ?: throw ReleaseValidationException("APK download URL is missing")
            val notesBody = release.optString("body").trim()
            val sha256 = asset.optString("digest")
                .takeIf { it.startsWith("sha256:", ignoreCase = true) }
                ?.substringAfter(':')
                ?: extractSha256FromText(notesBody)
                ?: throw ReleaseValidationException("APK SHA-256 digest is missing")
            if (asset.optLong("size") <= 0L) throw ReleaseValidationException("APK asset is empty")
            GithubRelease(
                version = version,
                url = release.getString("html_url"),
                notes = notesBody,
                downloadUrl = downloadUrl,
                publishedAt = release.optString("published_at").takeIf(String::isNotBlank),
                assetName = asset.optString("name"),
                assetSha256 = sha256,
            )
        } finally {
            connection.disconnect()
        }
    }

    fun fetchReleaseHistory(): List<GithubReleaseHistoryItem> {
        val connection = (URL(RELEASE_HISTORY_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 8_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "AkisGesture-Android")
        }
        return try {
            check(connection.responseCode in 200..299) { "GitHub HTTP ${connection.responseCode}" }
            val releases = JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
            (0 until releases.length()).mapNotNull { index ->
                val release = releases.getJSONObject(index)
                if (release.optBoolean("draft") || release.optBoolean("prerelease")) return@mapNotNull null
                val notes = release.optString("body").trim()
                val version = release.optString("tag_name").removePrefix("v").trim()
                if (version.isBlank()) return@mapNotNull null
                GithubReleaseHistoryItem(
                    version = version,
                    date = release.optString("published_at").take(10).ifBlank { "-" },
                    changesTr = releaseNotesToList(extractCleanReleaseNotes(notes, true)),
                    changesEn = releaseNotesToList(extractCleanReleaseNotes(notes, false)),
                )
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun releaseNotesToList(notes: String): List<String> = notes.lines()
        .map { it.trim().removePrefix("-").removePrefix("*").trim() }
        .filter { it.isNotBlank() && !it.startsWith("#") && !it.equals("Download", true) }
        .ifEmpty { listOf("Release notes are not available.") }

    fun compareVersions(current: String, latest: String): Int {
        val currentParts = current.split(Regex("[^0-9]+"))
            .filter(String::isNotEmpty)
            .map(String::toInt)
        val latestParts = latest.split(Regex("[^0-9]+"))
            .filter(String::isNotEmpty)
            .map(String::toInt)
        val size = maxOf(currentParts.size, latestParts.size)
        return (0 until size).firstNotNullOfOrNull { index ->
            val currentPart = currentParts.getOrElse(index) { 0 }
            val latestPart = latestParts.getOrElse(index) { 0 }
            when {
                latestPart > currentPart -> 1
                latestPart < currentPart -> -1
                else -> null
            }
        } ?: 0
    }

    fun isNewerVersion(current: String, latest: String): Boolean = compareVersions(current, latest) > 0

    fun extractSha256FromText(text: String): String? {
        if (text.isBlank()) return null
        val regex = Regex("""SHA-256[:`*\s]+([a-fA-F0-9]{64})""", RegexOption.IGNORE_CASE)
        return regex.find(text)?.groupValues?.get(1)?.uppercase()
    }

    /**
     * Extracts concise release highlights and removes full README content (badges, installation, license headers).
     */
    fun extractCleanReleaseNotes(rawNotes: String, isTurkish: Boolean = true): String {
        if (rawNotes.isBlank()) return ""

        var cleaned = rawNotes

        // 1. Language section extraction if release follows standard release notes structure
        val hasTurkishSection = cleaned.contains("## 🇹🇷 Türkçe") || cleaned.contains("## Türkçe")
        val hasEnglishSection = cleaned.contains("## 🇬🇧 English") || cleaned.contains("## English")
        if (hasTurkishSection || hasEnglishSection) {
            val turkishHeader = if (cleaned.contains("## 🇹🇷 Türkçe")) "## 🇹🇷 Türkçe" else "## Türkçe"
            val englishHeader = if (cleaned.contains("## 🇬🇧 English")) "## 🇬🇧 English" else "## English"
            cleaned = if (isTurkish && hasTurkishSection) {
                cleaned.substringAfter(turkishHeader)
                    .substringBefore("---")
                    .substringBefore(englishHeader)
                    .substringBefore("## 📦 İndirme")
                    .substringBefore("## 📦 Downloads")
            } else if (hasEnglishSection) {
                cleaned.substringAfter(englishHeader)
                    .substringBefore("---")
                    .substringBefore("## 📦 İndirme")
                    .substringBefore("## 📦 Downloads")
            } else {
                cleaned.substringAfter(turkishHeader)
                    .substringBefore("---")
                    .substringBefore("## 📦 İndirme")
                    .substringBefore("## 📦 Downloads")
            }
        }

        // 2. Filter out full README sections if full README was uploaded
        val lines = cleaned.lines()
        val filtered = mutableListOf<String>()
        var skippingSection = false

        for (line in lines) {
            val trimmed = line.trim()
            val lower = trimmed.lowercase()

            // Skip README badges, main headers, installation instructions, license, and download sections
            if (lower.startsWith("# akış gesture") || lower.startsWith("# akisgesture") ||
                trimmed.startsWith("![") ||
                lower.startsWith("## 🚀 kurulum") || lower.startsWith("## installation") ||
                lower.startsWith("## 🛠️ derleme") || lower.startsWith("## build") ||
                lower.startsWith("## 📄 lisans") || lower.startsWith("## license") ||
                lower.startsWith("## 🤝 katkıda bulunma") || lower.startsWith("## contributing") ||
                lower.startsWith("## 📦 indirme") || lower.startsWith("## downloads") ||
                lower.startsWith("### gereksinimler") || lower.startsWith("### requirements")
            ) {
                skippingSection = true
                continue
            }

            // Resume including lines if a feature/changelog bullet point or sub-header appears
            if (skippingSection) {
                if (trimmed.startsWith("## ") || trimmed.startsWith("### ") || trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                    if (!lower.contains("kurulum") && !lower.contains("lisans") && !lower.contains("indirme") && !lower.contains("installation") && !lower.contains("license")) {
                        skippingSection = false
                    }
                }
            }

            if (!skippingSection) {
                filtered.add(line)
            }
        }

        val result = filtered.joinToString("\n").trim()
            .replace(Regex("\n{3,}"), "\n\n")

        return result.ifBlank { rawNotes.trim() }
    }
}
