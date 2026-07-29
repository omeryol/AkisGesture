package com.omer.akisgesture.ui.component

import com.omer.akisgesture.model.SectionRange
import com.omer.akisgesture.overlay.Edge

/**
 * Normalized geometry shared by the gesture-map renderer and its hit testing.
 * Keeping this independent from Compose prevents the drawing and touch areas
 * from drifting apart.
 */
data class NormalizedRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    fun contains(x: Float, y: Float): Boolean =
        x in left..right && y in top..bottom
}

object GestureMapGeometry {
    private const val CONTENT_START = 0.08f
    private const val CONTENT_LENGTH = 0.84f
    private const val EDGE_OUTER = 0.94f
    private const val EDGE_INNER = 0.86f

    fun rect(edge: Edge, section: SectionRange): NormalizedRect {
        val start = CONTENT_START + section.start * CONTENT_LENGTH
        val end = CONTENT_START + section.end * CONTENT_LENGTH
        return when (edge) {
            Edge.LEFT -> NormalizedRect(
                left = 1f - EDGE_OUTER,
                top = start,
                right = 1f - EDGE_INNER,
                bottom = end,
            )
            Edge.RIGHT -> NormalizedRect(
                left = EDGE_INNER,
                top = start,
                right = EDGE_OUTER,
                bottom = end,
            )
            Edge.BOTTOM -> NormalizedRect(
                left = start,
                top = EDGE_INNER,
                right = end,
                bottom = EDGE_OUTER,
            )
        }
    }

    fun toSectionPosition(contentPosition: Float): Float =
        ((contentPosition - CONTENT_START) / CONTENT_LENGTH).coerceIn(0f, 1f)

    fun toSectionDelta(contentDelta: Float): Float =
        contentDelta / CONTENT_LENGTH
}

enum class RangeDragHandle {
    START,
    CENTER,
    END,
}

object SectionRangeEditor {
    const val MIN_LENGTH = 0.12f

    fun handleFor(position: Float, range: SectionRange): RangeDragHandle {
        val relative = ((position - range.start) / range.length).coerceIn(0f, 1f)
        return when {
            relative <= 0.25f -> RangeDragHandle.START
            relative >= 0.75f -> RangeDragHandle.END
            else -> RangeDragHandle.CENTER
        }
    }

    fun drag(
        original: SectionRange,
        handle: RangeDragHandle,
        delta: Float,
    ): SectionRange = when (handle) {
        RangeDragHandle.START -> SectionRange(
            start = (original.start + delta).coerceIn(0f, original.end - MIN_LENGTH),
            end = original.end,
        )
        RangeDragHandle.END -> SectionRange(
            start = original.start,
            end = (original.end + delta).coerceIn(original.start + MIN_LENGTH, 1f),
        )
        RangeDragHandle.CENTER -> {
            val length = original.length
            val start = (original.start + delta).coerceIn(0f, 1f - length)
            SectionRange(start = start, end = start + length)
        }
    }
}
