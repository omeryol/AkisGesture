package com.omer.akisgesture.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.omer.akisgesture.model.SectionRange
import com.omer.akisgesture.overlay.Edge
import kotlin.math.abs

@Composable
fun GestureMapCard(
    rules: List<GestureRule>,
    onZoneClick: (GestureRule) -> Unit,
    onZoneRangeChange: (Set<String>, SectionRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    val zones = rules
        .filter { it.enabled }
        .groupBy { Triple(it.trigger.edge, it.trigger.section, it.triggerMode) }
        .values
        .mapNotNull { group ->
            group.firstOrNull()?.let { representative ->
                GestureMapZone(
                    representative = representative,
                    ruleIds = group.map { it.id }.toSet(),
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
                "Dokunarak düzenle · sürükleyerek taşı veya boyutlandır",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            GestureMapCanvas(
                zones = zones,
                onZoneClick = { onZoneClick(it.representative) },
                onZoneRangeChange = { zone, range ->
                    onZoneRangeChange(zone.ruleIds, range)
                },
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
    onZoneRangeChange: (GestureMapZone, SectionRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    val outline = MaterialTheme.colorScheme.outline
    val screen = MaterialTheme.colorScheme.surfaceContainerHighest
    var dragPreview by remember { mutableStateOf<DragPreview?>(null) }

    Canvas(
        modifier = modifier.pointerInput(zones) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val downPosition = normalizedPosition(
                    down.position.x,
                    down.position.y,
                    size.width,
                    size.height,
                )
                val zone = zones.asReversed().firstOrNull {
                    hitRect(it).contains(downPosition.x, downPosition.y)
                } ?: return@awaitEachGesture
                val edge = zone.representative.trigger.edge
                val original = zone.representative.trigger.section
                val contentAxisPosition = if (edge == Edge.BOTTOM) {
                    downPosition.x
                } else {
                    downPosition.y
                }
                val sectionAxisPosition =
                    GestureMapGeometry.toSectionPosition(contentAxisPosition)
                val handle = SectionRangeEditor.handleFor(sectionAxisPosition, original)
                var dragging = false
                var latestRange = original
                var pressed: Boolean

                do {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    pressed = change.pressed
                    if (pressed) {
                        val rawDelta = if (edge == Edge.BOTTOM) {
                            change.position.x - down.position.x
                        } else {
                            change.position.y - down.position.y
                        }
                        val axisSize = if (edge == Edge.BOTTOM) {
                            size.width.toFloat() * 0.46f
                        } else {
                            size.height.toFloat()
                        }
                        if (abs(rawDelta) > viewConfiguration.touchSlop) dragging = true
                        if (dragging) {
                            latestRange = SectionRangeEditor.drag(
                                original = original,
                                handle = handle,
                                delta = GestureMapGeometry.toSectionDelta(rawDelta / axisSize),
                            )
                            dragPreview = DragPreview(zone, latestRange)
                            change.consume()
                        }
                    }
                } while (pressed)

                if (dragging) {
                    dragPreview = null
                    onZoneRangeChange(zone, latestRange)
                } else {
                    onZoneClick(zone)
                }
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
            val section = dragPreview
                ?.takeIf { it.zone.ruleIds == zone.ruleIds }
                ?.section
                ?: zone.representative.trigger.section
            val normalized = GestureMapGeometry.rect(
                zone.representative.trigger.edge,
                section,
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

private fun normalizedPosition(
    x: Float,
    y: Float,
    width: Int,
    height: Int,
): Offset = Offset(
    x = ((x / width) - 0.27f) / 0.46f,
    y = y / height,
)

private fun hitRect(zone: GestureMapZone): NormalizedRect {
    val rect = GestureMapGeometry.rect(
        zone.representative.trigger.edge,
        zone.representative.trigger.section,
    )
    val expansion = 0.05f
    return NormalizedRect(
        left = (rect.left - expansion).coerceAtLeast(0f),
        top = (rect.top - expansion).coerceAtLeast(0f),
        right = (rect.right + expansion).coerceAtMost(1f),
        bottom = (rect.bottom + expansion).coerceAtMost(1f),
    )
}

private data class GestureMapZone(
    val representative: GestureRule,
    val ruleIds: Set<String>,
    val hasQuick: Boolean,
    val hasHold: Boolean,
)

private data class DragPreview(
    val zone: GestureMapZone,
    val section: SectionRange,
)
