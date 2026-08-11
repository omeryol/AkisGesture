package io.github.omeryol.akisgesture.diagnostics

import android.content.Context
import android.os.Build
import android.os.SystemClock
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
        synchronized(lock) { events.clear() }
    }

    fun eventCount(): Int = synchronized(lock) { events.size }

    fun serviceConnected() = record("service", "connected")
    fun engineStarted() = record("engine", "started")
    fun serviceDisconnected(reason: String) = record("service", "disconnected", mapOf("reason" to reason))

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
    fun feedbackSymbol(edge: String, gesture: String, symbol: String) = record(
        "feedback", "symbol", mapOf("edge" to edge, "gesture" to gesture, "symbol" to symbol),
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
