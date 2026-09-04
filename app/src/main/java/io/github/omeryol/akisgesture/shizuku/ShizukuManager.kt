package io.github.omeryol.akisgesture.shizuku

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object ShizukuManager {
    enum class Status {
        NOT_INSTALLED,
        RUNNING_UNAUTHORIZED,
        AVAILABLE,
    }

    private val permissionListeners = mutableListOf<(Boolean) -> Unit>()
    private val shizukuPermissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == REQUEST_CODE_SHIZUKU) {
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            permissionListeners.forEach { it(granted) }
        }
    }

    private const val REQUEST_CODE_SHIZUKU = 10101
    private var initialized = false

    fun init() {
        if (initialized) return
        initialized = true
        runCatching {
            Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)
        }
    }

    fun isRunning(): Boolean = runCatching {
        Shizuku.pingBinder()
    }.getOrDefault(false)

    fun hasPermission(): Boolean = runCatching {
        if (!isRunning()) return false
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)

    fun getStatus(): Status {
        if (!isRunning()) return Status.NOT_INSTALLED
        return if (hasPermission()) Status.AVAILABLE else Status.RUNNING_UNAUTHORIZED
    }

    fun requestPermission(onResult: ((Boolean) -> Unit)? = null) {
        init()
        if (onResult != null) {
            permissionListeners.add(onResult)
        }
        if (isRunning() && !hasPermission()) {
            runCatching {
                Shizuku.requestPermission(REQUEST_CODE_SHIZUKU)
            }
        }
    }

    fun executeShell(command: String): String? {
        if (!hasPermission()) return null
        return runCatching {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java,
            ).apply { isAccessible = true }

            val process = method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val finished = process.waitFor(5, TimeUnit.SECONDS)
            if (finished && process.exitValue() == 0) output.trim() else null
        }.getOrNull()
    }
}
