package io.github.omeryol.akisgesture.gesture.model

import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.model.GestureType

sealed class GestureResult {
    /** Touch position along the edge in pixels (top-to-bottom for L/R, left-to-right for bottom). */
    abstract val touchAlongEdgePx: Float

    data class EdgeSwipe(
        val edge: Edge,
        val section: Int,
        val gestureType: GestureType = GestureType.QUICK_SWIPE,
        override val touchAlongEdgePx: Float = 0f,
    ) : GestureResult()

    data class VerticalSwipe(
        val edge: Edge,
        val section: Int,
        val direction: SwipeDirection,
        override val touchAlongEdgePx: Float = 0f,
    ) : GestureResult()

    data class BottomHorizontalSwipe(
        val direction: SwipeDirection,
        override val touchAlongEdgePx: Float = 0f,
    ) : GestureResult()

    data class Tap(
        val edge: Edge,
        val section: Int,
        override val touchAlongEdgePx: Float = 0f,
    ) : GestureResult()
}
