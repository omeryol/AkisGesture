package com.openswipe.gesture.model

class TouchState {
    var downX: Float = 0f
    var downY: Float = 0f
    var prevX: Float = 0f
    var prevY: Float = 0f
    var downTime: Long = 0L

    fun reset() {
        downX = 0f
        downY = 0f
        prevX = 0f
        prevY = 0f
        downTime = 0L
    }
}
