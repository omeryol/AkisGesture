package io.github.omeryol.akisgesture.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

object VerifiedApkDownloader {
    private val SAFE_FILE_NAME_REGEX = Regex("^[a-zA-Z0-9._-]+$")

    fun sanitizeAssetName(rawAssetName: String?, version: String): String {
        val candidate = rawAssetName?.takeIf(String::isNotBlank) ?: "AkisGesture-v$version.apk"
        return if (SAFE_FILE_NAME_REGEX.matches(candidate) && !candidate.contains("..")) {
            candidate
        } else {
            "AkisGesture-v$version.apk"
        }
    }

    fun download(context: Context, release: GithubRelease): File {
        val url = release.downloadUrl ?: error("APK download URL is missing")
        val expectedHash = release.assetSha256 ?: error("APK SHA-256 digest is missing")
        val safeAssetName = sanitizeAssetName(release.assetName, release.version)

        val targetDir = File(context.cacheDir, "verified-updates").apply { mkdirs() }
        val target = File(targetDir, safeAssetName)
        val canonicalTarget = target.canonicalFile
        val canonicalDir = targetDir.canonicalFile
        check(canonicalTarget.parentFile == canonicalDir) { "Invalid APK file target path" }

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

            verifyApkSignature(context, target)

            return target
        } catch (e: Throwable) {
            if (target.exists()) target.delete()
            throw e
        } finally {
            connection.disconnect()
            if (temporary.exists()) temporary.delete()
        }
    }

    @Suppress("DEPRECATION")
    fun verifyApkSignature(context: Context, apkFile: File) {
        val pm = context.packageManager

        val currentSigners = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
            } else {
                null
            }
            val info = if (flags != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(context.packageName, flags)
            } else {
                pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            }
            info.signingInfo?.apkContentsSigners?.map { sha256Fingerprint(it.toByteArray()) }
                ?: info.signingInfo?.signingCertificateHistory?.map { sha256Fingerprint(it.toByteArray()) }
                ?: emptyList()
        } else {
            val info = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            info.signatures?.map { sha256Fingerprint(it.toByteArray()) } ?: emptyList()
        }

        if (currentSigners.isEmpty()) {
            return
        }

        val archiveSigners = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
            } else {
                null
            }
            val info = if (flags != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageArchiveInfo(apkFile.absolutePath, flags)
            } else {
                pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
            }
            info?.signingInfo?.apkContentsSigners?.map { sha256Fingerprint(it.toByteArray()) }
                ?: info?.signingInfo?.signingCertificateHistory?.map { sha256Fingerprint(it.toByteArray()) }
                ?: emptyList()
        } else {
            val info = pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_SIGNATURES)
            info?.signatures?.map { sha256Fingerprint(it.toByteArray()) } ?: emptyList()
        }

        check(archiveSigners.isNotEmpty()) { "Downloaded APK signing certificates could not be read" }

        val hasMatch = currentSigners.any { current -> archiveSigners.contains(current) }
        check(hasMatch) {
            "APK signing certificate mismatch: downloaded package is not signed with the trusted app key"
        }
    }

    private fun sha256Fingerprint(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data).joinToString("") { "%02X".format(it) }
    }
}
