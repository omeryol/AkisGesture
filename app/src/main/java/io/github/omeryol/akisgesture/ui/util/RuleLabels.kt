package io.github.omeryol.akisgesture.ui.util

import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.model.SectionRange
import io.github.omeryol.akisgesture.overlay.Edge

fun edgeLabel(edge: Edge): String = when (edge) {
    Edge.LEFT -> "Sol kenar"
    Edge.RIGHT -> "Sağ kenar"
    Edge.BOTTOM -> "Alt kenar"
}

fun gestureLabel(type: GestureType): String = when (type) {
    GestureType.QUICK_SWIPE -> "Hızlı çekme"
    GestureType.SWIPE_HOLD -> "Çekip bekletme"
    GestureType.SWIPE_UP_L -> "L-Çekme (Yukarı)"
    GestureType.SWIPE_DOWN_L -> "L-Çekme (Aşağı)"
}

fun sectionLabel(section: SectionRange, edge: Edge): String {
    val isVertical = edge == Edge.LEFT || edge == Edge.RIGHT
    return when {
        section.start == 0f && section.end == 1f -> "Tüm alan"
        section.start == 0f && section.end == 1f / 3f ->
            if (isVertical) "Üst bölüm" else "Sol bölüm"
        section.start == 1f / 3f && section.end == 2f / 3f -> "Orta bölüm"
        section.start == 2f / 3f && section.end == 1f ->
            if (isVertical) "Alt bölüm" else "Sağ bölüm"
        section.start == 0f && section.end == 0.5f ->
            if (isVertical) "Üst yarısı" else "Sol yarısı"
        section.start == 0.5f && section.end == 1f ->
            if (isVertical) "Alt yarısı" else "Sağ yarısı"
        else -> "Özel alan · %${(section.start * 100).toInt()}–%${(section.end * 100).toInt()}"
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
    is ActionNode.SwitchNextApp -> "\uD83D\uDD04"
    is ActionNode.LockScreen -> "\uD83D\uDD12"
    is ActionNode.Screenshot -> "\uD83D\uDCF7"
    is ActionNode.SplitScreen -> "\u2B1C"
    is ActionNode.PowerMenu -> "\u23FB"
    is ActionNode.NotificationPanel -> "\uD83D\uDD14"
    is ActionNode.QuickSettings -> "\u2699"
    is ActionNode.InputMethodPicker -> "\u2328"
    is ActionNode.VolumePanel -> "\uD83C\uDF9A"
    is ActionNode.Assistant -> "\u2728"
    is ActionNode.MediaPlayPause -> "\u23EF"
    is ActionNode.MediaNext -> "\u23ED"
    is ActionNode.MediaPrevious -> "\u23EE"
    is ActionNode.VolumeUp -> "\uD83D\uDD0A"
    is ActionNode.VolumeDown -> "\uD83D\uDD09"
    is ActionNode.ToggleMute -> "\uD83D\uDD07"
    is ActionNode.ToggleFlashlight -> "\uD83D\uDD26"
    is ActionNode.NoAction -> "\u26D4"
    is ActionNode.LaunchApp -> "\uD83D\uDCF1"
    is ActionNode.ForceStopForeground -> "\u26D4"
    else -> "\u2753"
}

fun appLabel(context: android.content.Context, packageName: String): String {
    return try {
        val pm = context.packageManager
        val info = pm.getApplicationInfo(packageName, 0)
        pm.getApplicationLabel(info).toString()
    } catch (_: Exception) {
        packageName
    }
}
