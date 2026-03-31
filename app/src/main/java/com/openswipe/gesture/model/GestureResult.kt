package com.openswipe.gesture.model

import com.openswipe.overlay.Edge

sealed class GestureResult {
    data class EdgeSwipe(
        val edge: Edge,
        val section: Int,
        val isPrimary: Boolean,
    ) : GestureResult()

    data class VerticalSwipe(
        val edge: Edge,
        val section: Int,
        val direction: SwipeDirection,
    ) : GestureResult()

    data class Tap(
        val edge: Edge,
        val section: Int,
    ) : GestureResult()
}
