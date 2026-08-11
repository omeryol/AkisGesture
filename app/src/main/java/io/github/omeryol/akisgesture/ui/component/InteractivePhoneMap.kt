package io.github.omeryol.akisgesture.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.gesture.GestureConfig
import io.github.omeryol.akisgesture.model.ActionIconPack
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.GestureRule
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.model.toSymbol
import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.ui.util.localizedLabel
import io.github.omeryol.akisgesture.ui.theme.EdgeUi
import kotlinx.coroutines.delay

data class PhoneZone(
    val edge: Edge,
    val start: Float,
    val end: Float,
    val quickAction: ActionNode?,
    val holdAction: ActionNode?,
    val lUpAction: ActionNode? = null,
    val lDownAction: ActionNode? = null,
    val color: Color,
    val ruleIds: Set<String>,
)

/** Keeps the side sensors usable while their endpoints are dragged on the home map. */
internal object SideEdgeRangeEditor {
    const val minimumLength = 0.20f

    enum class Handle { START, END, MOVE }

    fun drag(original: Pair<Float, Float>, handle: Handle, delta: Float): Pair<Float, Float> {
        val (start, end) = original
        return when (handle) {
            Handle.START -> (start + delta).coerceIn(0f, end - minimumLength) to end
            Handle.END -> start to (end + delta).coerceIn(start + minimumLength, 1f)
            Handle.MOVE -> {
                val length = end - start
                val newStart = (start + delta).coerceIn(0f, 1f - length)
                newStart to (newStart + length)
            }
        }
    }
}

@Composable
fun InteractivePhoneMap(
    rules: List<GestureRule>,
    onSideRangeChange: (Edge, Float, Float) -> Unit,
    onSideRangePreview: (Edge, Float, Float) -> Unit,
    onEdgeClick: (Edge) -> Unit,
    modifier: Modifier = Modifier,
    iconPack: ActionIconPack = ActionIconPack.EMOJI_MODERN,
    config: GestureConfig? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val handleRadius = with(LocalDensity.current) { 14.dp.toPx() }
    val sensorTouchPadding = with(LocalDensity.current) { 12.dp.toPx() }
    val zones = buildPhoneZones(rules, scheme)
    var dragPreview by remember { mutableStateOf<Map<Edge, Pair<Float, Float>>>(emptyMap()) }
    var rangeFeedback by remember { mutableStateOf<Pair<Edge, Pair<Float, Float>>?>(null) }
    var dragging by remember { mutableStateOf(false) }

    LaunchedEffect(rangeFeedback, dragging) {
        if (rangeFeedback != null && !dragging) {
            delay(3_000)
            rangeFeedback = null
            dragPreview = emptyMap()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(config, zones) {
                        var selectedEdge: Edge? = null
                        var selectedHandle: SideEdgeRangeEditor.Handle? = null
                        var originalRange = 0f to 1f
                        var totalDelta = 0f

                        detectDragGestures(
                            onDragStart = { position ->
                                val screen = phoneScreenRect(size.width.toFloat(), size.height.toFloat())
                                val hit = sideHandleAt(position, screen, config, dragPreview, handleRadius, sensorTouchPadding)
                                selectedEdge = hit?.first
                                selectedHandle = hit?.second
                                originalRange = selectedEdge?.let { sideRange(it, config, dragPreview) } ?: (0f to 1f)
                                totalDelta = 0f
                                dragging = selectedEdge != null
                            },
                            onDrag = { change, dragAmount ->
                                val edge = selectedEdge ?: return@detectDragGestures
                                val handle = selectedHandle ?: return@detectDragGestures
                                totalDelta += dragAmount.y / size.height.toFloat()
                                val updated = SideEdgeRangeEditor.drag(originalRange, handle, totalDelta)
                                dragPreview = mapOf(edge to updated)
                                rangeFeedback = edge to updated
                                onSideRangePreview(edge, updated.first, updated.second)
                                change.consume()
                            },
                            onDragEnd = {
                                val edge = selectedEdge
                                val range = edge?.let { dragPreview[it] }
                                if (edge != null && range != null) {
                                    rangeFeedback = edge to range
                                    onSideRangeChange(edge, range.first, range.second)
                                }
                                selectedEdge = null
                                selectedHandle = null
                                dragging = false
                            },
                            onDragCancel = {
                                selectedEdge?.let { edge ->
                                    onSideRangePreview(edge, originalRange.first, originalRange.second)
                                }
                                selectedEdge = null
                                selectedHandle = null
                                dragging = false
                            },
                        )
                    },
            ) {
                val screen = phoneScreenRect(size.width, size.height)
                val body = Rect(screen.left - 8f, screen.top - 8f, screen.right + 8f, screen.bottom + 8f)

                // Static 3D phone: cast shadow, deep side rail and a raised display surface.
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.45f),
                    topLeft = Offset(body.left + 10f, body.top + 14f),
                    size = body.size,
                    cornerRadius = CornerRadius(42f),
                )
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF60677A), Color(0xFF171A24), Color(0xFF343846)),
                        start = body.topLeft,
                        end = body.bottomRight,
                    ),
                    topLeft = body.topLeft,
                    size = body.size,
                    cornerRadius = CornerRadius(42f),
                )
                drawRoundRect(
                    color = Color(0xFF090B12),
                    topLeft = screen.topLeft,
                    size = screen.size,
                    cornerRadius = CornerRadius(34f),
                )
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF172554), Color(0xFF0F172A), Color(0xFF020617)),
                        center = Offset(screen.center.x, screen.top + screen.height * 0.28f),
                        radius = screen.width * 1.45f,
                    ),
                    topLeft = Offset(screen.left + 3f, screen.top + 3f),
                    size = Size(screen.width - 6f, screen.height - 6f),
                    cornerRadius = CornerRadius(30f),
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.26f),
                    start = Offset(body.left + 4f, body.top + 30f),
                    end = Offset(body.left + 4f, body.bottom - 30f),
                    strokeWidth = 2f,
                )

                val screenClip = androidx.compose.ui.graphics.Path().apply {
                    addRoundRect(RoundRect(screen, CornerRadius(34f)))
                }
                clipPath(screenClip) {
                    drawCircle(
                        brush = Brush.radialGradient(listOf(scheme.primary.copy(alpha = 0.32f), Color.Transparent)),
                        radius = screen.width * 0.72f,
                        center = Offset(screen.right * 0.82f, screen.top + screen.height * 0.36f),
                    )
                    zones.forEach { zone ->
                        val zoneRect = phoneZoneRect(zone, screen, config, dragPreview)
                        drawRoundRect(
                            brush = Brush.horizontalGradient(listOf(zone.color.copy(alpha = 0.78f), zone.color.copy(alpha = 0.38f))),
                            topLeft = zoneRect.topLeft,
                            size = zoneRect.size,
                            cornerRadius = CornerRadius(10f),
                        )
                        drawRoundRect(
                            color = zone.color,
                            topLeft = zoneRect.topLeft,
                            size = zoneRect.size,
                            cornerRadius = CornerRadius(10f),
                            style = Stroke(2f),
                        )
                    }
                    drawRingPreviews(screen, config, iconPack, density, scheme)
                }

                // High-contrast endpoint arrows stay inside the coloured trigger range.
                listOf(Edge.LEFT, Edge.RIGHT).forEach { edge ->
                    val range = sideRange(edge, config, dragPreview)
                    val sensor = sideSensorRect(edge, screen, config, dragPreview)
                    val x = sensor.center.x
                    listOf(range.first, range.second).forEach { ratio ->
                        val y = if (ratio == range.first) sensor.top + 10f else sensor.bottom - 10f
                        val direction = if (ratio == range.first) -1f else 1f
                        val triangle = androidx.compose.ui.graphics.Path().apply {
                            moveTo(x - 9f, y - direction * 6f)
                            lineTo(x + 9f, y - direction * 6f)
                            lineTo(x, y + direction * 11f)
                            close()
                        }
                        drawPath(triangle, color = Color(0xFFF1EEE6))
                    }
                }

                val islandWidth = screen.width * 0.30f
                drawRoundRect(
                    color = Color(0xFF020204),
                    topLeft = Offset(screen.center.x - islandWidth / 2f, screen.top + 9f),
                    size = Size(islandWidth, 11f),
                    cornerRadius = CornerRadius(7f),
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.45f),
                    topLeft = Offset(screen.center.x - screen.width * 0.16f, screen.bottom - 13f),
                    size = Size(screen.width * 0.32f, 3f),
                    cornerRadius = CornerRadius(2f),
                )
            }
            rangeFeedback?.let { (activeEdge, _) ->
                val leftRange = sideRange(Edge.LEFT, config, dragPreview)
                val rightRange = sideRange(Edge.RIGHT, config, dragPreview)
                Text(
                    text = rangeComparisonLabel(context, leftRange, rightRange, activeEdge),
                    color = scheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp)
                        .clip(RoundedCornerShape(12.dp)).background(scheme.primaryContainer).padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            EdgeActionPanel(
                title = "← ${context.getString(io.github.omeryol.akisgesture.R.string.edge_left)}",
                entries = zones.filter { it.edge == Edge.LEFT }.flatMap { it.actionEntries() },
                iconPack = iconPack,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp),
            )
            EdgeActionPanel(
                title = "${context.getString(io.github.omeryol.akisgesture.R.string.edge_right)} →",
                entries = zones.filter { it.edge == Edge.RIGHT }.flatMap { it.actionEntries() },
                iconPack = iconPack,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp),
            )
        }

        Spacer(Modifier.height(8.dp))

        // Compact edge indicators; assignments are shown beside the phone.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EdgeUi.ordered.forEach { edge ->
                val title = when (edge) {
                    Edge.LEFT -> context.getString(io.github.omeryol.akisgesture.R.string.edge_left)
                    Edge.RIGHT -> context.getString(io.github.omeryol.akisgesture.R.string.edge_right)
                    Edge.BOTTOM -> context.getString(io.github.omeryol.akisgesture.R.string.edge_bottom)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onEdgeClick(edge) }
                        .clip(RoundedCornerShape(10.dp))
                        .background(scheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

private fun PhoneZone.actions(): List<ActionNode> = listOfNotNull(quickAction, holdAction, lUpAction, lDownAction)

@Composable
private fun EdgeActionPanel(title: String, entries: List<Pair<String, ActionNode>>, iconPack: ActionIconPack, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .width(72.dp)
            .padding(4.dp),
    ) {
        Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        if (entries.isEmpty()) {
            Text("—", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                entries.forEachIndexed { index, (kind, action) ->
                    if (index > 0) Text("─", color = scheme.outline, style = MaterialTheme.typography.labelSmall)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(action.toSymbol(iconPack), style = MaterialTheme.typography.titleSmall)
                        Text(kind, style = MaterialTheme.typography.labelSmall, color = scheme.primary)
                    }
                }
            }
        }
    }
}

private fun PhoneZone.actionEntries(): List<Pair<String, ActionNode>> = listOfNotNull(
    quickAction?.takeUnless { it is ActionNode.NoAction }?.let { "1" to it },
    holdAction?.takeUnless { it is ActionNode.NoAction }?.let { "2" to it },
    lUpAction?.takeUnless { it is ActionNode.NoAction }?.let { "L↑" to it },
    lDownAction?.takeUnless { it is ActionNode.NoAction }?.let { "L↓" to it },
)

private fun edgeLabel(context: android.content.Context, edge: Edge): String = context.getString(
    when (edge) {
        Edge.LEFT -> io.github.omeryol.akisgesture.R.string.edge_left
        Edge.RIGHT -> io.github.omeryol.akisgesture.R.string.edge_right
        Edge.BOTTOM -> io.github.omeryol.akisgesture.R.string.edge_bottom
    },
)

private fun rangeComparisonLabel(
    context: android.content.Context,
    left: Pair<Float, Float>,
    right: Pair<Float, Float>,
    activeEdge: Edge,
): String {
    fun values(range: Pair<Float, Float>): String =
        "${((range.second - range.first) * 100).toInt()}% / ${(range.first * 100).toInt()}%"
    val leftMarker = if (activeEdge == Edge.LEFT) "•" else ""
    val rightMarker = if (activeEdge == Edge.RIGHT) "•" else ""
    return "${edgeLabel(context, Edge.LEFT)} $leftMarker${values(left)}    ${edgeLabel(context, Edge.RIGHT)} $rightMarker${values(right)}"
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRingPreviews(
    screen: Rect,
    config: GestureConfig?,
    iconPack: ActionIconPack,
    density: Float,
    scheme: androidx.compose.material3.ColorScheme,
) {
    if (config == null) return

    val ringRadius = (config.ringSizeDp * density * 0.22f).coerceIn(13f, 24f)
    val ringSpacing = (config.ringGroupSpacingDp * density * 0.22f).coerceIn(ringRadius * 2.15f, ringRadius * 3.7f)
    val sideLead = ringSpacing * (1f - config.ringArc.coerceIn(0f, 1f))
    val inset = (config.ringGroupInsetDp * density * 0.20f).coerceIn(ringRadius + 6f, screen.width * 0.30f)
    val edgeColors = mapOf(
        Edge.LEFT to EdgeUi.color(Edge.LEFT),
        Edge.RIGHT to EdgeUi.color(Edge.RIGHT),
        Edge.BOTTOM to EdgeUi.color(Edge.BOTTOM),
    )

    EdgeUi.ordered.filter { config.ringMenuEnabledFor(it) }.forEach { edge ->
        val actions = config.ringActionsFor(edge).take(3).let { values ->
            values + List(3 - values.size) { ActionNode.NoAction }
        }
        val centers = when (edge) {
            Edge.LEFT -> listOf(
                Offset(screen.left + inset + sideLead, screen.center.y - ringSpacing),
                Offset(screen.left + inset + ringSpacing, screen.center.y),
                Offset(screen.left + inset + sideLead, screen.center.y + ringSpacing),
            )
            Edge.RIGHT -> listOf(
                Offset(screen.right - inset - sideLead, screen.center.y - ringSpacing),
                Offset(screen.right - inset - ringSpacing, screen.center.y),
                Offset(screen.right - inset - sideLead, screen.center.y + ringSpacing),
            )
            Edge.BOTTOM -> listOf(
                Offset(screen.center.x - ringSpacing, screen.bottom - inset - sideLead),
                Offset(screen.center.x, screen.bottom - inset - ringSpacing),
                Offset(screen.center.x + ringSpacing, screen.bottom - inset - sideLead),
            )
        }
        centers.forEachIndexed { index, center ->
            val color = edgeColors.getValue(edge)
            drawCircle(color = Color.Black.copy(alpha = 0.34f), radius = ringRadius + 3f, center = center)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.30f), color.copy(alpha = 0.22f), color.copy(alpha = 0.08f)),
                    center = center,
                    radius = ringRadius * 1.7f,
                ),
                radius = ringRadius,
                center = center,
            )
            drawCircle(color = color.copy(alpha = 0.88f), radius = ringRadius, center = center, style = Stroke(width = 2.2f))
            val action = actions[index]
            if (action !is ActionNode.NoAction) {
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = android.graphics.Color.WHITE
                    textSize = ringRadius * 0.95f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }
                drawContext.canvas.nativeCanvas.drawText(action.toSymbol(iconPack), center.x, center.y - (paint.ascent() + paint.descent()) / 2f, paint)
            }
        }
    }
}

private fun phoneScreenRect(width: Float, height: Float): Rect {
    val phoneHeight = height * 0.94f
    val phoneWidth = (phoneHeight * 0.52f).coerceAtMost(width * 0.76f)
    val left = (width - phoneWidth) / 2f
    val top = (height - phoneHeight) / 2f
    return Rect(left + 7f, top + 7f, left + phoneWidth - 7f, top + phoneHeight - 7f)
}

private fun sideRange(edge: Edge, config: GestureConfig?, preview: Map<Edge, Pair<Float, Float>>): Pair<Float, Float> =
    preview[edge] ?: config?.verticalRangeFor(edge) ?: (0f to 1f)

private fun sideHandleAt(
    position: Offset,
    screen: Rect,
    config: GestureConfig?,
    preview: Map<Edge, Pair<Float, Float>>,
    handleRadius: Float,
    sensorTouchPadding: Float,
): Pair<Edge, SideEdgeRangeEditor.Handle>? {
    return listOf(Edge.LEFT, Edge.RIGHT).firstNotNullOfOrNull { edge ->
        val range = sideRange(edge, config, preview)
        val x = if (edge == Edge.LEFT) screen.left + 10f else screen.right - 10f
        val start = Offset(x, screen.top + range.first * screen.height)
        val end = Offset(x, screen.top + range.second * screen.height)
        when {
            (position - start).getDistance() <= handleRadius * 1.5f -> edge to SideEdgeRangeEditor.Handle.START
            (position - end).getDistance() <= handleRadius * 1.5f -> edge to SideEdgeRangeEditor.Handle.END
            expanded(sideSensorRect(edge, screen, config, preview), sensorTouchPadding).contains(position) -> edge to SideEdgeRangeEditor.Handle.MOVE
            else -> null
        }
    }
}

private fun phoneZoneRect(
    zone: PhoneZone,
    screen: Rect,
    config: GestureConfig?,
    preview: Map<Edge, Pair<Float, Float>>,
): Rect {
    val thickness = when (zone.edge) {
        Edge.LEFT -> (config?.leftTriggerWidthDp ?: 24f) / 60f * (screen.width * 0.18f)
        Edge.RIGHT -> (config?.rightTriggerWidthDp ?: 24f) / 60f * (screen.width * 0.18f)
        Edge.BOTTOM -> (config?.bottomTriggerHeightDp ?: 24f) / 60f * (screen.height * 0.18f)
    }
    return when (zone.edge) {
        Edge.LEFT, Edge.RIGHT -> sideSensorRect(zone.edge, screen, config, preview)
        Edge.BOTTOM -> {
            val height = thickness.coerceIn(8f, screen.height * 0.25f)
            Rect(screen.left + zone.start * screen.width + 4f, screen.bottom - height - 2f, screen.left + zone.end * screen.width - 4f, screen.bottom - 2f)
        }
    }
}

private fun sideSensorRect(
    edge: Edge,
    screen: Rect,
    config: GestureConfig?,
    preview: Map<Edge, Pair<Float, Float>>,
): Rect {
    val width = when (edge) {
        Edge.LEFT -> (config?.leftTriggerWidthDp ?: 24f) / 60f * (screen.width * 0.18f)
        Edge.RIGHT -> (config?.rightTriggerWidthDp ?: 24f) / 60f * (screen.width * 0.18f)
        Edge.BOTTOM -> error("Bottom edge does not have a vertical range")
    }.coerceIn(8f, screen.width * 0.28f)
    val range = sideRange(edge, config, preview)
    val left = if (edge == Edge.LEFT) screen.left + 2f else screen.right - width - 2f
    return Rect(left, screen.top + range.first * screen.height + 4f, left + width, screen.top + range.second * screen.height - 4f)
}

private fun expanded(rect: Rect, amount: Float): Rect = Rect(
    rect.left - amount,
    rect.top - amount,
    rect.right + amount,
    rect.bottom + amount,
)

private fun buildPhoneZones(rules: List<GestureRule>, scheme: androidx.compose.material3.ColorScheme): List<PhoneZone> {
    val colors = listOf(Color(0xFF3D5AFE), Color(0xFF00E676), Color(0xFFFF9100), Color(0xFFFF1744), Color(0xFFD500F9), Color(0xFF00E5FF))
    return rules.filter { it.enabled }
        .groupBy { Triple(it.trigger.edge, it.trigger.section, it.triggerMode) }
        .values
        .mapIndexed { index, group ->
            val representative = group.first()
            PhoneZone(
                edge = representative.trigger.edge,
                start = representative.trigger.section.start,
                end = representative.trigger.section.end,
                quickAction = group.firstOrNull { it.trigger.gestureType == GestureType.QUICK_SWIPE }?.action,
                holdAction = group.firstOrNull { it.trigger.gestureType == GestureType.SWIPE_HOLD }?.action,
                lUpAction = group.firstOrNull { it.trigger.gestureType == GestureType.SWIPE_UP_L }?.action,
                lDownAction = group.firstOrNull { it.trigger.gestureType == GestureType.SWIPE_DOWN_L }?.action,
                color = colors[index % colors.size],
                ruleIds = group.map { it.id }.toSet(),
            )
        }
}
