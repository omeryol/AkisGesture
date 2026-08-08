package io.github.omeryol.akisgesture.util

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

object VerifiedApkDownloader {
    fun download(context: Context, release: GithubRelease): File {
        val url = release.downloadUrl ?: error("APK download URL is missing")
        val expectedHash = release.assetSha256 ?: error("APK SHA-256 digest is missing")
        val targetDir = File(context.cacheDir, "verified-updates").apply { mkdirs() }
        val target = File(targetDir, release.assetName ?: "AkisGesture-v${release.version}.apk")
        val temporary = File(targetDir, "${target.name}.part")
        runCatching { temporary.delete() }
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "AkisGesture-Android")
        }
        try {
            check(connection.responseCode in 200..299) { "APK HTTP ${connection.responseCode}" }
            val digest = MessageDigest.getInstance("SHA-256")
            connection.inputStream.use { input ->
                temporary.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        digest.update(buffer, 0, count)
                        output.write(buffer, 0, count)
                    }
                }
            }
            val actualHash = digest.digest().joinToString("") { "%02x".format(it) }
            check(actualHash.equals(expectedHash, ignoreCase = true)) { "APK SHA-256 verification failed" }
            if (target.exists()) target.delete()
            check(temporary.renameTo(target)) { "Verified APK could not be finalized" }
            return target
        } finally {
            connection.disconnect()
            if (temporary.exists()) temporary.delete()
        }
    }
}
