package io.github.omeryol.akisgesture.util

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class GithubRelease(
    val version: String,
    val url: String,
    val notes: String,
    val downloadUrl: String?,
)

object GithubReleaseChecker {
    private const val LATEST_RELEASE_URL =
        "https://api.github.com/repos/omeryol/AkisGesture/releases/latest"

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
            GithubRelease(
                version = release.getString("tag_name").removePrefix("v"),
                url = release.getString("html_url"),
                notes = release.optString("body").trim(),
                downloadUrl = release.optJSONArray("assets")
                    ?.let { assets ->
                        (0 until assets.length())
                            .map { assets.getJSONObject(it) }
                            .firstOrNull { it.optString("name").endsWith(".apk", ignoreCase = true) }
                            ?.optString("browser_download_url")
                            ?.takeIf(String::isNotBlank)
                    },
            )
        } finally {
            connection.disconnect()
        }
    }

    fun isNewerVersion(current: String, latest: String): Boolean {
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
                latestPart > currentPart -> true
                latestPart < currentPart -> false
                else -> null
            }
        } ?: false
    }

    /**
     * Extracts concise release highlights and removes full README content (badges, installation, license headers).
     */
    fun extractCleanReleaseNotes(rawNotes: String, isTurkish: Boolean = true): String {
        if (rawNotes.isBlank()) return ""

        var cleaned = rawNotes

        // 1. Language section extraction if release follows standard release notes structure
        if (cleaned.contains("## 🇹🇷 Türkçe") || cleaned.contains("## 🇬🇧 English")) {
            cleaned = if (isTurkish && cleaned.contains("## 🇹🇷 Türkçe")) {
                cleaned.substringAfter("## 🇹🇷 Türkçe")
                    .substringBefore("---")
                    .substringBefore("## 🇬🇧 English")
                    .substringBefore("## 📦 İndirme")
                    .substringBefore("## 📦 Downloads")
            } else if (cleaned.contains("## 🇬🇧 English")) {
                cleaned.substringAfter("## 🇬🇧 English")
                    .substringBefore("---")
                    .substringBefore("## 📦 İndirme")
                    .substringBefore("## 📦 Downloads")
            } else {
                cleaned.substringAfter("## 🇹🇷 Türkçe")
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