package com.omer.akisgesture.root

import android.content.Context
import android.content.Intent

sealed interface RootResult {
    data object Success : RootResult
    data class Failure(val reason: String) : RootResult
}

/**
 * Root komutlarının tek giriş noktası. Paket adı doğrulanmadan kabuğa veri
 * göndermez ve çalışma profillerini kendiliğinden hedeflemez.
 */
class RootCommandExecutor(private val context: Context) {

    fun forceStopPersonalProfile(packageName: String): RootResult {
        if (!PACKAGE_NAME.matches(packageName)) {
            return RootResult.Failure("Geçersiz paket adı")
        }
        if (packageName in protectedPackages()) {
            return RootResult.Failure("Bu uygulama güvenlik nedeniyle kapatılamaz")
        }
        return execute("am force-stop --user 0 $packageName")
    }

    fun checkAccess(): RootResult = execute("id -u")

    private fun execute(command: String): RootResult = try {
        val process = ProcessBuilder("su", "-c", command)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
        val exitCode = process.waitFor()
        if (exitCode == 0) {
            RootResult.Success
        } else {
            RootResult.Failure(output.ifEmpty { "Root işlemi başarısız" })
        }
    } catch (error: Exception) {
        RootResult.Failure(error.message ?: "Root erişimi kullanılamadı")
    }

    private fun protectedPackages(): Set<String> {
        val homePackage = context.packageManager
            .resolveActivity(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
                0,
            )
            ?.activityInfo
            ?.packageName
        return buildSet {
            add(context.packageName)
            add("android")
            add("com.android.systemui")
            add("com.android.settings")
            add("com.android.phone")
            add("com.android.permissioncontroller")
            add("com.google.android.permissioncontroller")
            homePackage?.let(::add)
        }
    }

    companion object {
        private val PACKAGE_NAME = Regex("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+$")
    }
}
