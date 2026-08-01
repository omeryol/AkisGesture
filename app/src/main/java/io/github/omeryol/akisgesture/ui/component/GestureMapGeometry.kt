package io.github.omeryol.akisgesture.ui.component

import io.github.omeryol.akisgesture.model.SectionRange
import io.github.omeryol.akisgesture.overlay.Edge

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

    fun rect(
        edge: Edge,
        section: SectionRange,
        config: io.github.omeryol.akisgesture.gesture.GestureConfig? = null,
    ): NormalizedRect {
        if (config == null) {
            val start = CONTENT_START + section.start * CONTENT_LENGTH
            val end = CONTENT_START + section.end * CONTENT_LENGTH
            return when (edge) {
                Edge.LEFT -> NormalizedRect(0.02f, start, 0.10f, end)
                Edge.RIGHT -> NormalizedRect(0.90f, start, 0.98f, end)
                Edge.BOTTOM -> NormalizedRect(start, 0.90f, end, 0.98f)
            }
        }

        return when (edge) {
            Edge.LEFT -> {
                val thickness = (config.leftTriggerWidthDp / 120f).coerceIn(0.04f, 0.28f)
                val vStart = config.leftVerticalStart
                val vEnd = config.leftVerticalEnd
                val sStart = vStart + (vEnd - vStart) * section.start
                val sEnd = vStart + (vEnd - vStart) * section.end
                val top = CONTENT_START + sStart * CONTENT_LENGTH
                val bottom = CONTENT_START + sEnd * CONTENT_LENGTH
                NormalizedRect(
                    left = 0.02f,
                    top = top,
                    right = (0.02f + thickness).coerceAtMost(0.40f),
                    bottom = bottom,
                )
            }
            Edge.RIGHT -> {
                val thickness = (config.rightTriggerWidthDp / 120f).coerceIn(0.04f, 0.28f)
                val vStart = config.rightVerticalStart
                val vEnd = config.rightVerticalEnd
                val sStart = vStart + (vEnd - vStart) * section.start
                val sEnd = vStart + (vEnd - vStart) * section.end
                val top = CONTENT_START + sStart * CONTENT_LENGTH
                val bottom = CONTENT_START + sEnd * CONTENT_LENGTH
                NormalizedRect(
                    left = (0.98f - thickness).coerceAtLeast(0.60f),
                    top = top,
                    right = 0.98f,
                    bottom = bottom,
                )
            }
            Edge.BOTTOM -> {
                val thickness = (config.bottomTriggerHeightDp / 120f).coerceIn(0.04f, 0.28f)
                val sStart = section.start
                val sEnd = section.end
                val left = CONTENT_START + sStart * CONTENT_LENGTH
                val right = CONTENT_START + sEnd * CONTENT_LENGTH
                NormalizedRect(
                    left = left,
                    top = (0.98f - thickness).coerceAtLeast(0.60f),
                    right = right,
                    bottom = 0.98f,
                )
            }
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
