package com.omer.akisgesture.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.omer.akisgesture.model.GestureRule
import com.omer.akisgesture.model.GestureType

@Composable
fun GestureMapCard(
    rules: List<GestureRule>,
    onZoneClick: (GestureRule) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeRules = rules.filter { it.enabled }
    val zones = activeRules
        .groupBy { Triple(it.trigger.edge, it.trigger.section, it.triggerMode) }
        .values
        .mapNotNull { group ->
            group.firstOrNull()?.let { representative ->
                GestureMapZone(
                    representative = representative,
                    hasQuick = group.any { it.trigger.gestureType == GestureType.QUICK_SWIPE },
                    hasHold = group.any { it.trigger.gestureType == GestureType.SWIPE_HOLD },
                )
            }
        }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text("Hareket alanların", style = MaterialTheme.typography.titleMedium)
            Text(
                "Düzenlemek için renkli bir kenara dokun",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            GestureMapCanvas(
                zones = zones,
                onZoneClick = { onZoneClick(it.representative) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(top = 8.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MapLegend(color = Color(0xFF5B8CFF), text = "Hızlı")
                MapLegend(color = Color(0xFFFFB74D), text = "Beklet")
                MapLegend(color = Color(0xFF8B5CF6), text = "İkisi")
            }
        }
    }
}

@Composable
private fun MapLegend(color: Color, text: String) {
    Text(
        text = "● $text",
        color = color,
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
private fun GestureMapCanvas(
    zones: List<GestureMapZone>,
    onZoneClick: (GestureMapZone) -> Unit,
    modifier: Modifier = Modifier,
) {
    val outline = MaterialTheme.colorScheme.outline
    val screen = MaterialTheme.colorScheme.surfaceContainerHighest
    Canvas(
        modifier = modifier.pointerInput(zones) {
            detectTapGestures { tap ->
                val normalizedX = ((tap.x / size.width) - 0.27f) / 0.46f
                val normalizedY = tap.y / size.height
                zones
                    .asReversed()
                    .firstOrNull {
                        GestureMapGeometry.rect(
                            it.representative.trigger.edge,
                            it.representative.trigger.section,
                        ).contains(normalizedX, normalizedY)
                    }
                    ?.let(onZoneClick)
            }
        },
    ) {
        val phoneLeft = size.width * 0.27f
        val phoneRight = size.width * 0.73f
        val phoneRect = Rect(phoneLeft, 0f, phoneRight, size.height)
        drawRoundRect(
            color = screen,
            topLeft = phoneRect.topLeft,
            size = phoneRect.size,
            cornerRadius = CornerRadius(38f, 38f),
        )
        drawRoundRect(
            color = outline,
            topLeft = phoneRect.topLeft,
            size = phoneRect.size,
            cornerRadius = CornerRadius(38f, 38f),
            style = Stroke(width = 4f),
        )
        drawRoundRect(
            color = outline.copy(alpha = 0.5f),
            topLeft = Offset(size.width * 0.44f, size.height * 0.035f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.12f, 5f),
            cornerRadius = CornerRadius(4f),
        )

        zones.forEach { zone ->
            val normalized = GestureMapGeometry.rect(
                zone.representative.trigger.edge,
                zone.representative.trigger.section,
            )
            val mapped = mapToPhone(normalized, phoneRect)
            val color = when {
                zone.hasQuick && zone.hasHold -> Color(0xFF8B5CF6)
                zone.hasHold -> Color(0xFFFFB74D)
                else -> Color(0xFF5B8CFF)
            }
            drawRoundRect(
                color = color,
                topLeft = mapped.topLeft,
                size = mapped.size,
                cornerRadius = CornerRadius(12f, 12f),
            )
        }
    }
}

private fun mapToPhone(rect: NormalizedRect, phone: Rect): Rect =
    Rect(
        left = phone.left + rect.left * phone.width,
        top = phone.top + rect.top * phone.height,
        right = phone.left + rect.right * phone.width,
        bottom = phone.top + rect.bottom * phone.height,
    )

private data class GestureMapZone(
    val representative: GestureRule,
    val hasQuick: Boolean,
    val hasHold: Boolean,
)
