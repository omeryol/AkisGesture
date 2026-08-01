package io.github.omeryol.akisgesture.ui.component

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.R
import io.github.omeryol.akisgesture.gesture.GestureConfig
import io.github.omeryol.akisgesture.gesture.GestureThresholds
import io.github.omeryol.akisgesture.model.GestureRule
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.model.SectionRange
import io.github.omeryol.akisgesture.overlay.Edge
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
    var expanded by remember { mutableStateOf(false) }
    val rehearsalStart = stringResource(R.string.rehearsal_start)
    var rehearsalStatus by remember { mutableStateOf(rehearsalStart) }
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

    AkisGlassCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.gesture_areas)) },
                supportingContent = {
                    Text(if (expanded) stringResource(R.string.map_expanded_hint) else stringResource(R.string.active_area_count, zones.size))
                },
                trailingContent = {
                    Icon(
                        if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = stringResource(if (expanded) R.string.collapse else R.string.open_map),
                    )
                },
                modifier = Modifier.clickable { expanded = !expanded },
            )
            AnimatedVisibility(expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        if (mode == GestureMapMode.EDIT) {
                            stringResource(R.string.map_edit_hint)
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
                            label = { Text(stringResource(R.string.edit)) },
                        )
                        FilterChip(
                            selected = mode == GestureMapMode.REHEARSE,
                            onClick = {
                                mode = GestureMapMode.REHEARSE
                                rehearsalStatus = rehearsalStart
                            },
                            label = { Text(stringResource(R.string.try_action)) },
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
                        MapLegend(color = Color(0xFF5B8CFF), text = stringResource(R.string.quick_legend))
                        MapLegend(color = Color(0xFFFFB74D), text = stringResource(R.string.hold_legend))
                        MapLegend(color = Color(0xFF8B5CF6), text = stringResource(R.string.both_legend))
                    }
                }
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
    val context = LocalContext.current
    val outline = MaterialTheme.colorScheme.outline
    val screen = MaterialTheme.colorScheme.surfaceContainerHighest
    var dragPreview by remember { mutableStateOf<DragPreview?>(null) }
    var gesturePreview by remember { mutableStateOf<GesturePreview?>(null) }
    val density = LocalDensity.current

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
                    onRehearsalStatus(context.getString(R.string.rehearsal_pull))
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
                            config.dampingFor(edge),
                        )
                        val rehearsalThresholdPx = with(density) {
                            config.swipeThresholdDpFor(edge).dp.toPx()
                        }
                        latestQuick = GestureThresholds.isQuickArmed(
                            stretch,
                            rehearsalThresholdPx,
                        )
                        latestHold = GestureThresholds.isHoldArmed(
                            dampedDisplacement = stretch,
                            threshold = rehearsalThresholdPx,
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
                                latestHold -> context.getString(R.string.rehearsal_hold_ready)
                                latestQuick -> context.getString(R.string.rehearsal_quick_ready)
                                else -> context.getString(R.string.rehearsal_more)
                            },
                        )
                        change.consume()
                    } while (pressed)
                    gesturePreview = null
                    onRehearsalStatus(
                        when {
                            latestHold -> context.getString(R.string.rehearsal_hold_detected)
                            latestQuick -> context.getString(R.string.rehearsal_quick_detected)
                            else -> context.getString(R.string.rehearsal_not_reached)
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
