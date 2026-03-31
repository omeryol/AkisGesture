package com.openswipe.gesture.model

import com.openswipe.overlay.Edge

sealed class GestureResult {
    /** Touch position along the edge in pixels (top-to-bottom for L/R, left-to-right for bottom). */
    abstract val touchAlongEdgePx: Float

    data class EdgeSwipe(
        val edge: Edge,
        val section: Int,
        override val touchAlongEdgePx: Float = 0f,
    ) : GestureResult()

    data class VerticalSwipe(
        val edge: Edge,
        val section: Int,
        val direction: SwipeDirection,
        override val touchAlongEdgePx: Float = 0f,
    ) : GestureResult()

    data class Tap(
        val edge: Edge,
        val section: Int,
        override val touchAlongEdgePx: Float = 0f,
    ) : GestureResult()
}
