package com.omer.akisgesture.ui.component

import android.os.SystemClock
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.omer.akisgesture.gesture.GestureConfig
import com.omer.akisgesture.gesture.GestureThresholds
import com.omer.akisgesture.model.GestureRule
import com.omer.akisgesture.model.GestureType
import com.omer.akisgesture.model.SectionRange
import com.omer.akisgesture.overlay.Edge
import kotlin.math.abs

@Composable
fun GestureMapCard(
    rules: List<GestureRule>,
    config: GestureConfig,
    onZoneClick: (GestureRule) -> Unit,
    onZoneRangeChange: (Set<String>, SectionRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    var mode by remember { mutableStateOf(GestureMapMode.EDIT) }
    var rehearsalStatus by remember { mutableStateOf("Bir alanı kenardan içeri çek") }
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
                if (mode == GestureMapMode.EDIT) {
                    "Dokunarak düzenle · sürükleyerek taşı veya boyutlandır"
                } else {
                    rehearsalStatus
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = mode == GestureMapMode.EDIT,
                    onClick = { mode = GestureMapMode.EDIT },
                    label = { Text("Düzenle") },
                )
                FilterChip(
                    selected = mode == GestureMapMode.REHEARSE,
                    onClick = {
                        mode = GestureMapMode.REHEARSE
                        rehearsalStatus = "Bir alanı kenardan içeri çek"
                    },
                    label = { Text("Dene") },
                )
            }
            GestureMapCanvas(
                zones = zones,
                config = config,
                mode = mode,
                onZoneClick = { onZoneClick(it.representative) },
                onZoneRangeChange = { zone, range ->
                    onZoneRangeChange(zone.ruleIds, range)
                },
                onRehearsalStatus = { rehearsalStatus = it },
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
    config: GestureConfig,
    mode: GestureMapMode,
    onZoneClick: (GestureMapZone) -> Unit,
    onZoneRangeChange: (GestureMapZone, SectionRange) -> Unit,
    onRehearsalStatus: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val outline = MaterialTheme.colorScheme.outline
    val screen = MaterialTheme.colorScheme.surfaceContainerHighest
    var dragPreview by remember { mutableStateOf<DragPreview?>(null) }
    var gesturePreview by remember { mutableStateOf<GesturePreview?>(null) }

    Canvas(
        modifier = modifier.pointerInput(zones, mode, config) {
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
                if (mode == GestureMapMode.REHEARSE) {
                    val startedAt = SystemClock.uptimeMillis()
                    var latestQuick = false
                    var latestHold = false
                    var pressed: Boolean
                    onRehearsalStatus("İçeri doğru çek")
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        pressed = change.pressed
                        val rawInward = when (edge) {
                            Edge.LEFT -> change.position.x - down.position.x
                            Edge.RIGHT -> down.position.x - change.position.x
                            Edge.BOTTOM -> down.position.y - change.position.y
                        }.coerceAtLeast(0f)
                        val stretch = GestureThresholds.dampedDisplacement(
                            rawInward,
                            config.dampingFactor,
                        )
                        latestQuick = GestureThresholds.isQuickArmed(
                            stretch,
                            config.minSwipeThresholdPx,
                        )
                        latestHold = GestureThresholds.isHoldArmed(
                            dampedDisplacement = stretch,
                            threshold = config.minSwipeThresholdPx,
                            elapsedMs = SystemClock.uptimeMillis() - startedAt,
                            holdTimeMs = config.holdTimeMs,
                        )
                        gesturePreview = GesturePreview(
                            edge = edge,
                            touchAlongEdge = if (edge == Edge.BOTTOM) downPosition.x else downPosition.y,
                            stretch = stretch,
                            quickArmed = latestQuick,
                            holdArmed = latestHold,
                        )
                        onRehearsalStatus(
                            when {
                                latestHold -> "Bekletme hazır · bırakınca önizleme biter"
                                latestQuick -> "Hızlı çekme hazır · bekletirsen ikinci hareket"
                                else -> "Biraz daha çek"
                            },
                        )
                        change.consume()
                    } while (pressed)
                    gesturePreview = null
                    onRehearsalStatus(
                        when {
                            latestHold -> "Bekletmeli hareket algılandı"
                            latestQuick -> "Hızlı çekme algılandı"
                            else -> "Hareket eşiğe ulaşmadı"
                        },
                    )
                    return@awaitEachGesture
                }

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

        gesturePreview?.let { preview ->
            val touchX = phoneRect.left + preview.touchAlongEdge * phoneRect.width
            val touchY = preview.touchAlongEdge * phoneRect.height
            val stretch = preview.stretch.coerceIn(0f, phoneRect.width * 0.42f)
            val span = 46f + stretch * 0.35f
            val path = Path()
            val tip: Offset
            when (preview.edge) {
                Edge.LEFT -> {
                    tip = Offset(phoneRect.left + stretch, touchY)
                    path.moveTo(phoneRect.left, touchY - span)
                    path.cubicTo(tip.x, touchY, tip.x, touchY, phoneRect.left, touchY + span)
                }
                Edge.RIGHT -> {
                    tip = Offset(phoneRect.right - stretch, touchY)
                    path.moveTo(phoneRect.right, touchY - span)
                    path.cubicTo(tip.x, touchY, tip.x, touchY, phoneRect.right, touchY + span)
                }
                Edge.BOTTOM -> {
                    tip = Offset(touchX, phoneRect.bottom - stretch)
                    path.moveTo(touchX - span, phoneRect.bottom)
                    path.cubicTo(touchX, tip.y, touchX, tip.y, touchX + span, phoneRect.bottom)
                }
            }
            path.close()
            val feedbackColor = when {
                preview.holdArmed -> Color(0xFF8B5CF6)
                preview.quickArmed -> Color(0xFF5B8CFF)
                else -> Color(config.feedbackColorArgb)
            }
            drawPath(
                path = path,
                color = feedbackColor.copy(alpha = config.feedbackOpacity),
            )
            drawCircle(
                color = feedbackColor,
                radius = if (preview.holdArmed) 16f else 12f,
                center = tip,
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

private data class GesturePreview(
    val edge: Edge,
    val touchAlongEdge: Float,
    val stretch: Float,
    val quickArmed: Boolean,
    val holdArmed: Boolean,
)

private enum class GestureMapMode {
    EDIT,
    REHEARSE,
}
