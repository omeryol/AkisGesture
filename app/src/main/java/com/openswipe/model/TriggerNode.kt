package com.openswipe.model

import com.openswipe.overlay.Edge

data class TriggerNode(
    val edge: Edge,
    val section: SectionRange,
    val gestureType: GestureType
)

data class SectionRange(
    val start: Float = 0f,
    val end: Float = 1f
) {
    init {
        require(start in 0f..1f) { "start must be in [0, 1]" }
        require(end in 0f..1f) { "end must be in [0, 1]" }
        require(start < end) { "start must < end" }
    }

    fun contains(position: Float) = position in start..end

    fun overlapsWith(other: SectionRange): Boolean {
        return start < other.end && other.start < end
    }

    companion object {
        val ALL = SectionRange(0f, 1f)
        fun thirds(index: Int) = SectionRange(index / 3f, (index + 1) / 3f)
        fun halves(index: Int) = SectionRange(index / 2f, (index + 1) / 2f)
        fun nths(index: Int, n: Int) = SectionRange(index.toFloat() / n, (index + 1).toFloat() / n)

        /** Common presets used by UI pickers. */
        fun presets(edge: Edge): List<Pair<String, SectionRange>> {
            val isVertical = edge == Edge.LEFT || edge == Edge.RIGHT
            return listOf(
                "全段" to ALL,
                (if (isVertical) "上1/3" else "左1/3") to thirds(0),
                "中1/3" to thirds(1),
                (if (isVertical) "下1/3" else "右1/3") to thirds(2),
                (if (isVertical) "上半" else "左半") to halves(0),
                (if (isVertical) "下半" else "右半") to halves(1),
            )
        }
    }
}

enum class GestureType {
    SWIPE,
}
