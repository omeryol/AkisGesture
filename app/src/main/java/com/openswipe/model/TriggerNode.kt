package com.omer.akisgesture.model

import com.omer.akisgesture.overlay.Edge

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

    val length: Float get() = end - start

    companion object {
        val ALL = SectionRange(0f, 1f)
        fun thirds(index: Int) = SectionRange(index / 3f, (index + 1) / 3f)
        fun halves(index: Int) = SectionRange(index / 2f, (index + 1) / 2f)
        fun nths(index: Int, n: Int) = SectionRange(index.toFloat() / n, (index + 1).toFloat() / n)

        /** Common presets used by UI pickers. */
        fun presets(edge: Edge): List<Pair<String, SectionRange>> {
            val isVertical = edge == Edge.LEFT || edge == Edge.RIGHT
            return listOf(
                "Tüm alan" to ALL,
                (if (isVertical) "Üst bölüm" else "Sol bölüm") to thirds(0),
                "Orta bölüm" to thirds(1),
                (if (isVertical) "Alt bölüm" else "Sağ bölüm") to thirds(2),
                (if (isVertical) "Üst yarısı" else "Sol yarısı") to halves(0),
                (if (isVertical) "Alt yarısı" else "Sağ yarısı") to halves(1),
            )
        }
    }
}

enum class GestureType {
    QUICK_SWIPE,
    SWIPE_HOLD,
}
