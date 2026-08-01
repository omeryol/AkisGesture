package io.github.omeryol.akisgesture.gesture

import io.github.omeryol.akisgesture.gesture.model.SwipeDirection
import kotlin.math.abs

object BottomAppSwitchPolicy {
    fun direction(dx: Float, dy: Float, touchSlop: Float): SwipeDirection? {
        if (abs(dx) <= touchSlop || abs(dx) <= abs(dy)) return null
        return if (dx < 0f) SwipeDirection.LEFT else SwipeDirection.RIGHT
    }

    fun isArmed(dx: Float, threshold: Float): Boolean =
        abs(dx) >= threshold.coerceAtLeast(1f)
}
