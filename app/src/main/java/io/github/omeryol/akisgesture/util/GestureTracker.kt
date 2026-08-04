package io.github.omeryol.akisgesture.util

import android.content.Context
import android.content.SharedPreferences
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.overlay.Edge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object GestureTracker {
    private const val PREF_NAME = "gesture_usage_stats"
    private const val KEY_TOTAL = "total_count"
    private const val KEY_EDGE_LEFT = "edge_left_count"
    private const val KEY_EDGE_RIGHT = "edge_right_count"
    private const val KEY_EDGE_BOTTOM = "edge_bottom_count"
    private const val KEY_TYPE_QUICK = "type_quick_count"
    private const val KEY_TYPE_HOLD = "type_hold_count"
    private const val KEY_TYPE_L_UP = "type_l_up_count"
    private const val KEY_TYPE_L_DOWN = "type_l_down_count"

    private var prefs: SharedPreferences? = null

    private val _totalExecutions = MutableStateFlow(0)
    val totalExecutions: StateFlow<Int> = _totalExecutions.asStateFlow()

    private val _edgeCounts = MutableStateFlow(mapOf(Edge.LEFT to 0, Edge.RIGHT to 0, Edge.BOTTOM to 0))
    val edgeCounts: StateFlow<Map<Edge, Int>> = _edgeCounts.asStateFlow()

    private val _typeCounts = MutableStateFlow(
        mapOf(
            GestureType.QUICK_SWIPE to 0,
            GestureType.SWIPE_HOLD to 0,
            GestureType.SWIPE_UP_L to 0,
            GestureType.SWIPE_DOWN_L to 0,
        ),
    )
    val typeCounts: StateFlow<Map<GestureType, Int>> = _typeCounts.asStateFlow()

    fun init(context: Context) {
        val p = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs = p
        loadFromPrefs(p)
    }

    private fun loadFromPrefs(p: SharedPreferences) {
        val total = p.getInt(KEY_TOTAL, 0)
        val left = p.getInt(KEY_EDGE_LEFT, 0)
        val right = p.getInt(KEY_EDGE_RIGHT, 0)
        val bottom = p.getInt(KEY_EDGE_BOTTOM, 0)

        val quick = p.getInt(KEY_TYPE_QUICK, 0)
        val hold = p.getInt(KEY_TYPE_HOLD, 0)
        val lUp = p.getInt(KEY_TYPE_L_UP, 0)
        val lDown = p.getInt(KEY_TYPE_L_DOWN, 0)

        _totalExecutions.value = total
        _edgeCounts.value = mapOf(Edge.LEFT to left, Edge.RIGHT to right, Edge.BOTTOM to bottom)
        _typeCounts.value = mapOf(
            GestureType.QUICK_SWIPE to quick,
            GestureType.SWIPE_HOLD to hold,
            GestureType.SWIPE_UP_L to lUp,
            GestureType.SWIPE_DOWN_L to lDown,
        )
    }

    fun recordGesture(context: Context, edge: Edge, gestureType: GestureType) {
        val p = prefs ?: context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).also { prefs = it }

        val newTotal = p.getInt(KEY_TOTAL, 0) + 1

        val edgeKey = when (edge) {
            Edge.LEFT -> KEY_EDGE_LEFT
            Edge.RIGHT -> KEY_EDGE_RIGHT
            Edge.BOTTOM -> KEY_EDGE_BOTTOM
        }
        val newEdgeCount = p.getInt(edgeKey, 0) + 1

        val typeKey = when (gestureType) {
            GestureType.QUICK_SWIPE -> KEY_TYPE_QUICK
            GestureType.SWIPE_HOLD -> KEY_TYPE_HOLD
            GestureType.SWIPE_UP_L -> KEY_TYPE_L_UP
            GestureType.SWIPE_DOWN_L -> KEY_TYPE_L_DOWN
            else -> KEY_TYPE_QUICK
        }
        val newTypeCount = p.getInt(typeKey, 0) + 1

        p.edit()
            .putInt(KEY_TOTAL, newTotal)
            .putInt(edgeKey, newEdgeCount)
            .putInt(typeKey, newTypeCount)
            .apply()

        _totalExecutions.value = newTotal
        _edgeCounts.value = _edgeCounts.value.toMutableMap().apply {
            put(edge, newEdgeCount)
        }
        _typeCounts.value = _typeCounts.value.toMutableMap().apply {
            put(gestureType, newTypeCount)
        }
    }
}
