package io.github.omeryol.akisgesture.diagnostics

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import io.github.omeryol.akisgesture.BuildConfig
import io.github.omeryol.akisgesture.action.ActionResult
import io.github.omeryol.akisgesture.root.RootResult
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream

/**
 * Diagnostic-only flight recorder. The diagnostic APK records by default and
 * mirrors every event to Logcat for direct inspection; nothing is written to
 * disk automatically.
 */
object RuntimeDiagnostics {
    private const val MAX_EVENTS = 300
    private val lock = Any()
    private val events = ArrayDeque<DiagnosticEvent>(MAX_EVENTS)

    @Volatile
    var isRecording: Boolean = true
        private set

    @Volatile
    var lastDisconnectSummary: String? = null
        private set

    private val historicalExits = mutableListOf<Map<String, String>>()

    init {
        recordLocked("session", "started_default")
    }

    fun startSession() {
        synchronized(lock) {
            events.clear()
            isRecording = true
            recordLocked("session", "started")
        }
    }

    fun stopSession() {
        synchronized(lock) {
            recordLocked("session", "stopped")
            isRecording = false
        }
    }

    fun clear() {
        synchronized(lock) {
            events.clear()
            lastDisconnectSummary = null
        }
    }

    fun eventCount(): Int = synchronized(lock) { events.size }

    fun serviceConnected() = record("service", "connected")
    fun engineStarted() = record("engine", "started")

    fun serviceDisconnected(
        reason: String,
        context: Context? = null,
        foregroundPackage: String? = null,
        uptimeMs: Long = 0L,
    ) {
        val details = mutableMapOf<String, String>()
        details["reason"] = reason
        if (uptimeMs > 0) details["uptime_ms"] = uptimeMs.toString()
        if (!foregroundPackage.isNullOrBlank()) details["foreground_package"] = foregroundPackage

        var readableSummary = "Sebep: $reason"

        if (context != null) {
            runCatching {
                val cr = context.contentResolver
                val services = Settings.Secure.getString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
                val pkg = context.packageName
                val settingStillEnabled = services.split(':').any { it.contains(pkg) }
                details["setting_present"] = settingStillEnabled.toString()

                val masterA11y = Settings.Secure.getInt(cr, Settings.Secure.ACCESSIBILITY_ENABLED, -1)
                details["master_a11y_enabled"] = masterA11y.toString()

                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                if (pm != null) {
                    details["is_interactive"] = pm.isInteractive.toString()
                    details["is_power_save_mode"] = pm.isPowerSaveMode.toString()
                    details["ignoring_battery_optimizations"] = pm.isIgnoringBatteryOptimizations(pkg).toString()
                }

                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                if (am != null) {
                    val memInfo = ActivityManager.MemoryInfo()
                    am.getMemoryInfo(memInfo)
                    details["low_memory"] = memInfo.lowMemory.toString()
                    details["avail_ram_mb"] = (memInfo.availMem / (1024 * 1024)).toString()
                }

                readableSummary = when {
                    reason == "unbind" && settingStillEnabled -> "onUnbind (Ayar açık • Sistem bağlantıyı kesti)"
                    reason == "unbind" && !settingStillEnabled -> "onUnbind (Ayar silindi • Kullanıcı veya sistem kapattı)"
                    reason == "destroy" -> "onDestroy (Sistem servisi yok etti)"
                    else -> "$reason (Ayar: ${if (settingStillEnabled) "açık" else "kapalı"})"
                }
            }
        }

        lastDisconnectSummary = readableSummary
        record("service", "disconnected", details)
    }

    fun serviceInterrupted() {
        record("service", "interrupted")
    }

    fun healthCheckEvaluated(trigger: String, decision: String, details: Map<String, String> = emptyMap()) {
        record("health_check", decision, buildMap {
            put("trigger", trigger)
            putAll(details)
        })
    }

    fun recordHistoricalExitReasons(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        runCatching {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return
            val exits = am.getHistoricalProcessExitReasons(context.packageName, 0, 5)
            if (exits.isEmpty()) return
            synchronized(lock) {
                historicalExits.clear()
                for (exit in exits) {
                    val reasonDesc = when (exit.reason) {
                        ApplicationExitInfo.REASON_LOW_MEMORY -> "LMK (Düşük Bellek)"
                        ApplicationExitInfo.REASON_SIGNALED -> "Killed by Signal (SIGKILL/MIUI)"
                        ApplicationExitInfo.REASON_CRASH -> "Uygulama Çökmesi"
                        ApplicationExitInfo.REASON_CRASH_NATIVE -> "Yerel Çökme (Native)"
                        ApplicationExitInfo.REASON_ANR -> "ANR (Yanıt Vermiyor)"
                        ApplicationExitInfo.REASON_USER_REQUESTED -> "Kullanıcı Zorla Durdurdu"
                        ApplicationExitInfo.REASON_USER_STOPPED -> "Kullanıcı Durdurdu"
                        ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> "Aşırı Kaynak / CPU Kullanımı"
                        ApplicationExitInfo.REASON_PERMISSION_CHANGE -> "İzin Değişikliği"
                        ApplicationExitInfo.REASON_INITIALIZATION_FAILURE -> "Başlatma Hatası"
                        ApplicationExitInfo.REASON_EXIT_SELF -> "Normal Çıkış"
                        16 -> "Paket Güncellendi"
                        17 -> "Paket Durumu Değişti"
                        else -> "Kod: ${exit.reason}"
                    }
                    val map = buildMap {
                        put("reason", reasonDesc)
                        put("raw_reason", exit.reason.toString())
                        put("status", exit.status.toString())
                        put("importance", exit.importance.toString())
                        put("pss_mb", (exit.pss / 1024).toString())
                        put("rss_mb", (exit.rss / 1024).toString())
                        put("timestamp_epoch_ms", exit.timestamp.toString())
                        exit.description?.let { put("description", it) }
                    }
                    historicalExits.add(map)
                    recordLocked("process_exit", "historical", map)
                }
                val firstAbnormal = historicalExits.firstOrNull {
                    it["raw_reason"] != ApplicationExitInfo.REASON_EXIT_SELF.toString()
                }
                if (firstAbnormal != null && lastDisconnectSummary == null) {
                    lastDisconnectSummary = "Önceki Süreç: ${firstAbnormal["reason"]}"
                }
            }
        }
    }

    fun getLastDisconnectSummary(context: Context): String? {
        return lastDisconnectSummary ?: historicalExits.firstOrNull {
            it["raw_reason"] != "1"
        }?.get("reason")?.let { "Önceki Süreç: $it" }
    }

    fun gestureMatched(edge: String, gesture: String, actionId: String?) = record(
        category = "gesture",
        name = "matched",
        details = buildMap {
            put("edge", edge)
            put("gesture", gesture)
            actionId?.let { put("action", it) }
        },
    )

    fun gestureSignal(edge: String, signal: String) = record(
        category = "gesture",
        name = signal,
        details = mapOf("edge" to edge),
    )
    fun lActionLookup(edge: String, touchPx: Float, ratio: Float, matched: Boolean) = record(
        "l_lookup",
        "initial",
        mapOf(
            "edge" to edge,
            "touch_px" to "%.1f".format(java.util.Locale.US, touchPx),
            "ratio" to "%.4f".format(java.util.Locale.US, ratio),
            "matched" to matched.toString(),
        ),
    )
    fun lTrace(edge: String, phase: String, details: Map<String, String>) = record("l_trace", phase, buildMap {
        put("edge", edge)
        putAll(details)
    })
    fun feedbackSymbol(edge: String, gesture: String, symbol: String, colorMode: String? = null) = record(
        "feedback", "symbol", buildMap {
            put("edge", edge)
            put("gesture", gesture)
            put("symbol", symbol)
            colorMode?.let { put("color_mode", it) }
        },
    )

    fun ringOpened(edge: String) = record("ring", "opened", mapOf("edge" to edge))
    fun ringTiming(edge: String, phase: String, elapsedMs: Long) = record(
        "ring", "timing", mapOf("edge" to edge, "phase" to phase, "elapsed_ms" to elapsedMs.toString()),
    )
    fun ringAnimation(edge: String, phase: String, slot: Int? = null) = record(
        "ring", "animation_$phase", buildMap {
            put("edge", edge)
            slot?.let { put("slot", it.toString()) }
        },
    )
    fun ringSelected(edge: String, index: Int) = record("ring", "selected", mapOf("edge" to edge, "slot" to index.toString()))
    fun ringAction(edge: String, index: Int, actionId: String) = record(
        "ring",
        "action",
        mapOf("edge" to edge, "slot" to index.toString(), "action" to actionId),
    )
    fun ringDismissed(edge: String) = record("ring", "dismissed", mapOf("edge" to edge))
    fun ringHitProbe(edge: String, index: Int, x: Float, y: Float, touch: Float) = record(
        "ring", "hit_probe", mapOf(
            "edge" to edge,
            "hit" to index.toString(),
            "x" to "%.1f".format(java.util.Locale.US, x),
            "y" to "%.1f".format(java.util.Locale.US, y),
            "touch" to "%.1f".format(java.util.Locale.US, touch),
        ),
    )

    fun actionFinished(actionId: String, result: ActionResult) = record(
        category = "action",
        name = "finished",
        details = buildMap {
            put("action", actionId)
            when (result) {
                ActionResult.Success -> put("result", "success")
                is ActionResult.Failed -> {
                    put("result", "failed")
                    put("reason", result.reason.take(160).replace('\n', ' '))
                }
                is ActionResult.RequiresMinApi -> {
                    put("result", "requires_min_api")
                    put("api", result.api.toString())
                }
            }
        },
    )

    fun repairFinished(action: String, result: RootResult) = record(
        category = "accessibility_repair",
        name = "finished",
        details = buildMap {
            put("action", action)
            when (result) {
                RootResult.Success -> put("result", "success")
                is RootResult.Failure -> {
                    put("result", "failed")
                    put("reason", result.reason.take(160).replace('\n', ' '))
                }
            }
        },
    )

    fun export(context: Context, output: OutputStream) {
        val snapshot = synchronized(lock) { events.toList() }
        val exitsSnapshot = synchronized(lock) { historicalExits.toList() }
        val lastDisconnect = lastDisconnectSummary
        val report = JSONObject().apply {
            put("format", "akis-gesture-diagnostic")
            put("formatVersion", 1)
            put("exportedAtEpochMs", System.currentTimeMillis())
            put("appVersion", BuildConfig.VERSION_NAME)
            put("device", JSONObject().apply {
                put("manufacturer", Build.MANUFACTURER)
                put("model", Build.MODEL)
                put("sdkInt", Build.VERSION.SDK_INT)
            })
            lastDisconnect?.let { put("lastDisconnectSummary", it) }
            put("historicalProcessExits", JSONArray().apply {
                exitsSnapshot.forEach { put(JSONObject(it)) }
            })
            put("events", JSONArray().apply {
                snapshot.forEach { event -> put(event.toJson()) }
            })
        }
        output.bufferedWriter().use { it.write(report.toString(2)) }
    }

    private fun record(category: String, name: String, details: Map<String, String> = emptyMap()) {
        synchronized(lock) {
            if (!isRecording) return
            recordLocked(category, name, details)
        }
    }

    private fun recordLocked(category: String, name: String, details: Map<String, String> = emptyMap()) {
        while (events.size >= MAX_EVENTS) events.removeFirst()
        events.addLast(
            DiagnosticEvent(
                elapsedRealtimeMs = SystemClock.elapsedRealtime(),
                category = category,
                name = name,
                details = details,
            ),
        )
        val detailsText = details.entries.joinToString(", ") { (key, value) -> "$key=$value" }
        Log.i(LOG_TAG, "${category}/${name}${if (detailsText.isBlank()) "" else " | $detailsText"}")
    }

    private data class DiagnosticEvent(
        val elapsedRealtimeMs: Long,
        val category: String,
        val name: String,
        val details: Map<String, String>,
    ) {
        fun toJson(): JSONObject = JSONObject().apply {
            put("elapsedRealtimeMs", elapsedRealtimeMs)
            put("category", category)
            put("name", name)
            put("details", JSONObject(details))
        }
    }

    private const val LOG_TAG = "AkisGestureDiag"
}
