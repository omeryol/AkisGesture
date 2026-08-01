package io.github.omeryol.akisgesture.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.model.SectionRange
import io.github.omeryol.akisgesture.overlay.Edge

/**
 * Yarım telefon görseli — kenara göre sol/sağ/alt kısmı gösterir,
 * üzerinde etkin bölgeyi renkli overlay ile işaretler.
 */
@Composable
fun EdgeZoneVisual(
    edge: Edge,
    section: SectionRange,
    modifier: Modifier = Modifier,
    zoneColor: Color = MaterialTheme.colorScheme.primary,
) {
    val outline = MaterialTheme.colorScheme.outline
    val screen = MaterialTheme.colorScheme.surfaceVariant
    Box(modifier = modifier) {
        Canvas(Modifier.size(56.dp, 80.dp)) {
            val w = size.width
            val h = size.height
            val corner = CornerRadius(14f)

            when (edge) {
                Edge.LEFT -> {
                    // Left half phone
                    val phone = Rect(0f, 8f, w * 0.75f, h - 8f)
                    drawRoundRect(screen, phone.topLeft, phone.size, corner)
                    drawRoundRect(outline, phone.topLeft, phone.size, corner, style = Stroke(2f))
                    // Zone highlight on left edge
                    val zoneTop = phone.top + section.start * phone.height
                    val zoneH = (section.end - section.start) * phone.height
                    val zoneRect = Rect(phone.left, zoneTop, phone.left + 16f, zoneTop + zoneH)
                    drawRoundRect(zoneColor, zoneRect.topLeft, zoneRect.size, CornerRadius(6f))
                }
                Edge.RIGHT -> {
                    // Right half phone
                    val phone = Rect(w * 0.25f, 8f, w, h - 8f)
                    drawRoundRect(screen, phone.topLeft, phone.size, corner)
                    drawRoundRect(outline, phone.topLeft, phone.size, corner, style = Stroke(2f))
                    // Zone highlight on right edge
                    val zoneTop = phone.top + section.start * phone.height
                    val zoneH = (section.end - section.start) * phone.height
                    val zoneRect = Rect(phone.right - 16f, zoneTop, phone.right, zoneTop + zoneH)
                    drawRoundRect(zoneColor, zoneRect.topLeft, zoneRect.size, CornerRadius(6f))
                }
                Edge.BOTTOM -> {
                    // Bottom portion phone
                    val phone = Rect(16f, h * 0.2f, w - 16f, h)
                    drawRoundRect(screen, phone.topLeft, phone.size, corner)
                    drawRoundRect(outline, phone.topLeft, phone.size, corner, style = Stroke(2f))
                    // Zone highlight on bottom edge
                    val zoneLeft = phone.left + section.start * phone.width
                    val zoneW = (section.end - section.start) * phone.width
                    val zoneRect = Rect(zoneLeft, phone.bottom - 12f, zoneLeft + zoneW, phone.bottom)
                    drawRoundRect(zoneColor, zoneRect.topLeft, zoneRect.size, CornerRadius(6f))
                }
            }
        }
    }
}
