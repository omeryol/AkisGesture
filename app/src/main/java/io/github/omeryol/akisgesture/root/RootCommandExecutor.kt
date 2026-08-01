package io.github.omeryol.akisgesture.root

import android.content.Context
import android.content.Intent
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

sealed interface RootResult {
    data object Success : RootResult
    data class Failure(val reason: String) : RootResult
}

/**
 * Root komutlarının tek giriş noktası. Paket adı doğrulanmadan kabuğa veri
 * göndermez ve çalışma profillerini kendiliğinden hedeflemez.
 */
class RootCommandExecutor(private val context: Context) {
    private var switchTasks = emptyList<String>()
    private var switchIndex = 0
    private var switchDirection = 0
    private var expectedTaskPackage: String? = null
    private var lastSwitchAt = 0L

    fun forceStopPersonalProfile(packageName: String): RootResult {
        if (!PACKAGE_NAME.matches(packageName)) {
            return RootResult.Failure("Geçersiz paket adı")
        }
        if (packageName in protectedPackages()) {
            return RootResult.Failure("Bu uygulama güvenlik nedeniyle kapatılamaz")
        }
        val command =
            "task_ids=\$(dumpsys activity recents | " +
                "sed -n 's/.*#\\([0-9][0-9]*\\).*type=standard A=[0-9][0-9]*:$packageName}.*/\\1/p'); " +
                "am force-stop --user 0 $packageName; " +
                "for task_id in \$task_ids; do am stack remove \$task_id; done; " +
                "sleep 0.2; " +
                "if pidof $packageName >/dev/null; then " +
                "echo 'Uygulama işlemi kapanmadı'; exit 42; fi; " +
                "if dumpsys activity recents | grep -F '$packageName' >/dev/null; then " +
                "echo 'Uygulama son kullanılanlardan kaldırılamadı'; exit 43; fi"
        return execute(command)
    }

    fun checkAccess(): RootResult = execute("id -u")

    fun grantCameraPermission(): RootResult =
        execute("pm grant --user 0 ${context.packageName} android.permission.CAMERA")

    fun switchRecentTask(direction: Int): RootResult {
        if (direction == 0) return RootResult.Failure("Geçersiz geçiş yönü")
        val now = android.os.SystemClock.elapsedRealtime()
        val activities = executeForOutput("dumpsys activity activities")
            ?: return RootResult.Failure("Açık uygulama belirlenemedi")
        val currentPackage = Regex(
            """(?:topResumedActivity|ResumedActivity|mFocusedApp)[=:]\s*.*\su\d+\s+([^/\s]+)/""",
        ).find(activities)?.groupValues?.get(1)
            ?: return RootResult.Failure("Öndeki uygulama belirlenemedi")

        if (now - lastSwitchAt > 3_500L || switchTasks.isEmpty() ||
            (expectedTaskPackage != null && currentPackage != expectedTaskPackage)
        ) {
            val recents = executeForOutput("dumpsys activity recents")
                ?: return RootResult.Failure("Son uygulamalar okunamadı")
            val matcher = Regex(
                """Recent #\d+: Task\{[^#]*#(\d+) type=standard A=\d+:([^ }\r\n]+)""",
            )
            val ignored = setOf(
                context.packageName,
                "com.android.systemui",
                "com.miui.securitycenter",
                "com.miui.home",
            )
            val recentTasks = matcher.findAll(recents).mapNotNull { match ->
                val packageName = match.groupValues[2]
                if (packageName in ignored) null else packageName
            }.distinct().toList()
            switchTasks = listOf(currentPackage) + recentTasks.filterNot { it == currentPackage }
            switchIndex = 0
            switchDirection = direction
        }
        if (switchTasks.size < 2) return RootResult.Failure("Geçilecek başka uygulama yok")

        val delta = if (direction == switchDirection) 1 else -1
        val targetIndex = (switchIndex + delta).coerceIn(0, switchTasks.lastIndex)
        if (targetIndex == switchIndex) return RootResult.Failure("Bu yönde başka uygulama yok")
        val targetPackage = switchTasks[targetIndex]
        if (!PACKAGE_NAME.matches(targetPackage)) {
            return RootResult.Failure("Geçersiz uygulama paketi")
        }
        val result = execute(
            "monkey -p $targetPackage -c android.intent.category.LAUNCHER 1",
        )
        if (result is RootResult.Success) {
            switchIndex = targetIndex
            expectedTaskPackage = targetPackage
            lastSwitchAt = now
        }
        return result
    }

    fun toggleNavBar(): RootResult {
        // Read current state and toggle
        val current = executeForOutput("settings get global policy_control")
        val immersive = "immersive.navigation=*"
        return if (current?.contains("immersive.navigation") == true) {
            // Currently hidden, show it
            execute("settings put global policy_control null")
        } else {
            execute("settings put global policy_control $immersive")
        }
    }

    private fun executeForOutput(command: String): String? =
        runCommand(command)?.takeIf { it.exitCode == 0 }?.output

    fun execute(command: String): RootResult {
        val result = runCommand(command)
            ?: return RootResult.Failure("Root işlemi zaman aşımına uğradı veya başlatılamadı")
        return if (result.exitCode == 0) {
            RootResult.Success
        } else {
            RootResult.Failure(result.output.trim().ifEmpty { "Root işlemi başarısız" })
        }
    }

    private fun runCommand(command: String): CommandResult? = try {
        val process = ProcessBuilder("su", "-c", command)
            .redirectErrorStream(true)
            .start()
        val output = StringBuilder()
        val reader = thread(isDaemon = true, name = "akis-root-output") {
            process.inputStream.bufferedReader().use { stream ->
                stream.forEachLine { line -> output.appendLine(line) }
            }
        }
        if (!process.waitFor(ROOT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            reader.join(500L)
            null
        } else {
            reader.join(500L)
            CommandResult(process.exitValue(), output.toString())
        }
    } catch (_: Exception) {
        null
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
        private const val ROOT_TIMEOUT_SECONDS = 8L
        private val PACKAGE_NAME = Regex("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+$")
    }

    private data class CommandResult(val exitCode: Int, val output: String)
}
