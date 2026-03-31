package com.openswipe.ui.util

import com.openswipe.model.ActionNode
import com.openswipe.model.GestureType
import com.openswipe.model.SectionRange
import com.openswipe.overlay.Edge

fun edgeLabel(edge: Edge): String = when (edge) {
    Edge.LEFT -> "左侧"
    Edge.RIGHT -> "右侧"
    Edge.BOTTOM -> "底部"
}

fun gestureLabel(type: GestureType): String = when (type) {
    GestureType.SWIPE -> "滑动"
}

fun sectionLabel(section: SectionRange, edge: Edge): String {
    val isVertical = edge == Edge.LEFT || edge == Edge.RIGHT
    return when {
        section.start == 0f && section.end == 1f -> "全段"
        section.start == 0f && section.end == 1f / 3f -> if (isVertical) "上1/3" else "左1/3"
        section.start == 1f / 3f && section.end == 2f / 3f -> "中1/3"
        section.start == 2f / 3f && section.end == 1f -> if (isVertical) "下1/3" else "右1/3"
        section.start == 0f && section.end == 0.5f -> if (isVertical) "上半" else "左半"
        section.start == 0.5f && section.end == 1f -> if (isVertical) "下半" else "右半"
        else -> "${(section.start * 100).toInt()}%-${(section.end * 100).toInt()}%"
    }
}

fun edgeIcon(edge: Edge): String = when (edge) {
    Edge.LEFT -> "\u2190"   // ←
    Edge.RIGHT -> "\u2192"  // →
    Edge.BOTTOM -> "\u2193" // ↓
}

fun actionIcon(action: ActionNode): String = when (action) {
    is ActionNode.Back -> "\uD83D\uDD19"
    is ActionNode.Home -> "\uD83C\uDFE0"
    is ActionNode.Recents -> "\uD83D\uDDC2"
    is ActionNode.SwitchLastApp -> "\uD83D\uDD04"
    is ActionNode.LockScreen -> "\uD83D\uDD12"
    is ActionNode.Screenshot -> "\uD83D\uDCF7"
    is ActionNode.SplitScreen -> "\u2B1C"
    is ActionNode.PowerMenu -> "\u23FB"
    is ActionNode.NotificationPanel -> "\uD83D\uDD14"
    is ActionNode.QuickSettings -> "\u2699"
    is ActionNode.MediaPlayPause -> "\u23EF"
    is ActionNode.MediaNext -> "\u23ED"
    is ActionNode.MediaPrevious -> "\u23EE"
    is ActionNode.VolumeUp -> "\uD83D\uDD0A"
    is ActionNode.VolumeDown -> "\uD83D\uDD09"
    is ActionNode.ToggleFlashlight -> "\uD83D\uDD26"
    is ActionNode.NoAction -> "\u26D4"
    is ActionNode.LaunchApp -> "\uD83D\uDCF1"
    else -> "\u2753"
}
