package io.github.omeryol.akisgesture.util

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class GithubRelease(
    val version: String,
    val url: String,
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
}