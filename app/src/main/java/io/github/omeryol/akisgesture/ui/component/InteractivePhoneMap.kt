package io.github.omeryol.akisgesture.ui.component

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.BuildConfig
import io.github.omeryol.akisgesture.gesture.GestureConfig
import io.github.omeryol.akisgesture.model.ActionIconColorMode
import io.github.omeryol.akisgesture.model.ActionIconPack
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.GestureRule
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.feedback.ActionBitmapLoader
import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.ui.theme.EdgeUi
import kotlinx.coroutines.delay

private val MAP_HEIGHT = 400.dp

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
    iconPack: ActionIconPack = ActionIconPack.PHOSPHOR,
    config: GestureConfig? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val handleRadius = with(LocalDensity.current) { 14.dp.toPx() }
    val sensorTouchPadding = with(LocalDensity.current) { 12.dp.toPx() }
    val zones = buildPhoneZones(rules, scheme)
    val ringPreviewBitmaps = remember(config, iconPack, config?.actionIconColorMode) {
        val actionsToLoad = EdgeUi.ordered
            .flatMap { edge ->
                val list = mutableListOf<ActionNode>()
                list.addAll(config?.ringActionsFor(edge).orEmpty())
                if (config?.recentAppsEnabledFor(edge) == true) {
                    list.add(ActionNode.SwitchLastApp)
                }
                list
            }
            .distinctBy { it.id }
        actionsToLoad.associate { action ->
            val colorMode = config?.actionIconColorMode ?: ActionIconColorMode.FUNCTIONAL
            action.id to ActionBitmapLoader.load(
                context = context,
                action = action,
                pack = iconPack,
                sizePx = 96,
                tint = colorMode.resolveColorInt(action),
            )?.asImageBitmap()
        }
    }
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
                .height(MAP_HEIGHT),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MAP_HEIGHT)
                    .align(Alignment.TopCenter)
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
                val body = Rect(screen.left - 9f, screen.top - 9f, screen.right + 9f, screen.bottom + 9f)

                // 1. Soft Realistic Ambient Shadow
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.55f),
                    topLeft = Offset(body.left + 8f, body.top + 14f),
                    size = body.size,
                    cornerRadius = CornerRadius(44f),
                )
                // 2. Obsidian Titanium Hardware Rail (Frame)
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF383C4A), Color(0xFF14161F), Color(0xFF262936), Color(0xFF0F1118)),
                        start = body.topLeft,
                        end = body.bottomRight,
                    ),
                    topLeft = body.topLeft,
                    size = body.size,
                    cornerRadius = CornerRadius(44f),
                )
                // 3. Bezel Edge Specular Highlight
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.18f),
                    topLeft = body.topLeft,
                    size = body.size,
                    cornerRadius = CornerRadius(44f),
                    style = Stroke(1.5f),
                )
                // 4. OLED Display Border
                drawRoundRect(
                    color = Color(0xFF06070B),
                    topLeft = screen.topLeft,
                    size = screen.size,
                    cornerRadius = CornerRadius(36f),
                )
                // 5. Deep Space OLED Display Surface with Ambient Center Glow
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF090D18), Color(0xFF020408)),
                        center = Offset(screen.center.x, screen.top + screen.height * 0.35f),
                        radius = screen.width * 1.35f,
                    ),
                    topLeft = Offset(screen.left + 2f, screen.top + 2f),
                    size = Size(screen.width - 4f, screen.height - 4f),
                    cornerRadius = CornerRadius(34f),
                )

                val screenClip = androidx.compose.ui.graphics.Path().apply {
                    addRoundRect(RoundRect(screen, CornerRadius(34f)))
                }
                clipPath(screenClip) {
                    // Subtle Cybernetic Blueprint Grid & HUD Telemetry (matched to master artwork)
                    val gridStep = 28.dp.toPx()
                    var gx = screen.left + 18f
                    while (gx < screen.right - 18f) {
                        var gy = screen.top + 24f
                        while (gy < screen.bottom - 24f) {
                            // Faint Crosshairs at Grid Intersections
                            drawLine(
                                color = Color.White.copy(alpha = 0.08f),
                                start = Offset(gx - 3.5f, gy),
                                end = Offset(gx + 3.5f, gy),
                                strokeWidth = 1f,
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.08f),
                                start = Offset(gx, gy - 3.5f),
                                end = Offset(gx, gy + 3.5f),
                                strokeWidth = 1f,
                            )
                            gy += gridStep
                        }
                        gx += gridStep
                    }

                    // Futuristic HUD Telemetry Brackets (Center Screen - exactly matching master visual)
                    val hudY = screen.top + screen.height * 0.38f
                    val hudColor = Color(0xFF64FEFF).copy(alpha = 0.16f)
                    val hudW = screen.width * 0.68f
                    val hudLeft = screen.center.x - hudW / 2f
                    // Top Telemetry Bracket Line
                    drawLine(color = hudColor, start = Offset(hudLeft, hudY), end = Offset(hudLeft + hudW * 0.22f, hudY), strokeWidth = 1.2f)
                    drawLine(color = hudColor, start = Offset(hudLeft + hudW * 0.78f, hudY), end = Offset(hudLeft + hudW, hudY), strokeWidth = 1.2f)
                    // Stepped Center Channel
                    val stepY = hudY + 14f
                    drawLine(color = hudColor, start = Offset(hudLeft + hudW * 0.22f, hudY), end = Offset(hudLeft + hudW * 0.28f, stepY), strokeWidth = 1.2f)
                    drawLine(color = hudColor, start = Offset(hudLeft + hudW * 0.28f, stepY), end = Offset(hudLeft + hudW * 0.72f, stepY), strokeWidth = 1.2f)
                    drawLine(color = hudColor, start = Offset(hudLeft + hudW * 0.72f, stepY), end = Offset(hudLeft + hudW * 0.78f, hudY), strokeWidth = 1.2f)
                    // Secondary Lower Telemetry Line
                    val lowerY = screen.top + screen.height * 0.58f
                    val hudColor2 = Color(0xFFA573E2).copy(alpha = 0.14f)
                    drawLine(color = hudColor2, start = Offset(hudLeft + 8f, lowerY), end = Offset(hudLeft + hudW * 0.38f, lowerY), strokeWidth = 1.2f)
                    drawLine(color = hudColor2, start = Offset(hudLeft + hudW * 0.62f, lowerY), end = Offset(hudLeft + hudW - 8f, lowerY), strokeWidth = 1.2f)

                    // Side Recessed Sensor Tracks
                    listOf(Edge.LEFT, Edge.RIGHT).forEach { edge ->
                        val track = sideSensorRect(edge, screen, config, dragPreview)
                        drawRoundRect(
                            color = Color(0xFF0C101A),
                            topLeft = track.topLeft,
                            size = track.size,
                            cornerRadius = CornerRadius(8f),
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.06f),
                            topLeft = track.topLeft,
                            size = track.size,
                            cornerRadius = CornerRadius(8f),
                            style = Stroke(1f),
                        )
                    }

                    // Dynamic Segmented Neon Zones (Bölünmüş Kenarlar - Multi-Layer Radiant Bloom)
                    zones.forEach { zone ->
                        val rawRect = phoneZoneRect(zone, screen, config, dragPreview)
                        val isSide = zone.edge == Edge.LEFT || zone.edge == Edge.RIGHT
                        val zoneRect = if (isSide) {
                            Rect(rawRect.left, rawRect.top + 2.5f, rawRect.right, rawRect.bottom - 2.5f)
                        } else {
                            Rect(rawRect.left + 2.5f, rawRect.top, rawRect.right - 2.5f, rawRect.bottom)
                        }

                        if (zoneRect.height > 4f && zoneRect.width > 4f) {
                            // Layer 1: Wide Ambient Diffuse Bloom (glowing light bleed into glass and chassis)
                            val bloomX = if (isSide) 16f else 4f
                            val bloomY = if (isSide) 4f else 16f
                            drawRoundRect(
                                color = zone.color.copy(alpha = 0.22f),
                                topLeft = Offset(zoneRect.left - bloomX, zoneRect.top - bloomY),
                                size = Size(zoneRect.width + bloomX * 2f, zoneRect.height + bloomY * 2f),
                                cornerRadius = CornerRadius(16f),
                            )
                            // Layer 2: Focused Neon Aura Halo
                            val haloX = if (isSide) 7f else 2f
                            val haloY = if (isSide) 2f else 7f
                            drawRoundRect(
                                color = zone.color.copy(alpha = 0.48f),
                                topLeft = Offset(zoneRect.left - haloX, zoneRect.top - haloY),
                                size = Size(zoneRect.width + haloX * 2f, zoneRect.height + haloY * 2f),
                                cornerRadius = CornerRadius(12f),
                            )
                            // Layer 3: Luminous High-Contrast Neon Tube Body
                            val coreTone = when (zone.edge) {
                                Edge.LEFT -> Color(0xFF80F7FF)   // Cyan-White Core
                                Edge.RIGHT -> Color(0xFFF3E5F5)  // Violet-Lavender Core
                                Edge.BOTTOM -> Color(0xFFFFF9C4) // Amber-Gold Core
                            }
                            val brush = if (isSide) {
                                val gradientColors = when (zone.edge) {
                                    Edge.LEFT -> listOf(coreTone.copy(alpha = 0.95f), zone.color.copy(alpha = 0.85f), zone.color.copy(alpha = 0.35f))
                                    else -> listOf(zone.color.copy(alpha = 0.35f), zone.color.copy(alpha = 0.85f), coreTone.copy(alpha = 0.95f))
                                }
                                Brush.horizontalGradient(gradientColors, startX = zoneRect.left, endX = zoneRect.right)
                            } else {
                                Brush.verticalGradient(
                                    listOf(zone.color.copy(alpha = 0.35f), zone.color.copy(alpha = 0.85f), coreTone.copy(alpha = 0.95f)),
                                    startY = zoneRect.top,
                                    endY = zoneRect.bottom,
                                )
                            }
                            drawRoundRect(
                                brush = brush,
                                topLeft = zoneRect.topLeft,
                                size = zoneRect.size,
                                cornerRadius = CornerRadius(8f),
                            )
                            // Layer 4: Neon Outer Specular Rim
                            drawRoundRect(
                                color = zone.color,
                                topLeft = zoneRect.topLeft,
                                size = zoneRect.size,
                                cornerRadius = CornerRadius(8f),
                                style = Stroke(2f),
                            )
                            // Layer 5: High-Tech Specular White Light Streak (Beam)
                            if (isSide) {
                                val streakX = if (zone.edge == Edge.LEFT) zoneRect.left + 2f else zoneRect.right - 2f
                                drawLine(
                                    color = Color.White.copy(alpha = 0.90f),
                                    start = Offset(streakX, zoneRect.top + 6f),
                                    end = Offset(streakX, zoneRect.bottom - 6f),
                                    strokeWidth = 2.2f,
                                )
                            } else {
                                val streakY = zoneRect.bottom - 2f
                                drawLine(
                                    color = Color.White.copy(alpha = 0.90f),
                                    start = Offset(zoneRect.left + 6f, streakY),
                                    end = Offset(zoneRect.right - 6f, streakY),
                                    strokeWidth = 2.2f,
                                )
                            }
                        }
                    }

                    // Divider Notches Between Adjacent Sections
                    listOf(Edge.LEFT, Edge.RIGHT).forEach { edge ->
                        val edgeZones = zones.filter { it.edge == edge }.sortedBy { it.start }
                        if (edgeZones.size > 1) {
                            val sensor = sideSensorRect(edge, screen, config, dragPreview)
                            edgeZones.drop(1).forEach { nextZone ->
                                val dividerY = sensor.top + nextZone.start * sensor.height
                                drawLine(
                                    color = Color.White.copy(alpha = 0.90f),
                                    start = Offset(sensor.left - 2f, dividerY),
                                    end = Offset(sensor.right + 2f, dividerY),
                                    strokeWidth = 2.5f,
                                )
                            }
                        }
                    }
                    val bottomZones = zones.filter { it.edge == Edge.BOTTOM }.sortedBy { it.start }
                    if (bottomZones.size > 1) {
                        bottomZones.drop(1).forEach { nextZone ->
                            val rawRect = phoneZoneRect(nextZone, screen, config, dragPreview)
                            val dividerX = rawRect.left
                            drawLine(
                                color = Color.White.copy(alpha = 0.90f),
                                start = Offset(dividerX, rawRect.top - 2f),
                                end = Offset(dividerX, rawRect.bottom + 2f),
                                strokeWidth = 2.5f,
                            )
                        }
                    }

                    drawRingPreviews(screen, config, density, ringPreviewBitmaps)
                }

                // Glowing Tactile Range Handles
                listOf(Edge.LEFT, Edge.RIGHT).forEach { edge ->
                    val range = sideRange(edge, config, dragPreview)
                    val sensor = sideSensorRect(edge, screen, config, dragPreview)
                    val edgeColor = EdgeUi.color(edge)
                    val x = sensor.center.x
                    listOf(range.first, range.second).forEach { ratio ->
                        val y = if (ratio == range.first) sensor.top else sensor.bottom
                        // Outer Halo Glow
                        drawCircle(
                            color = edgeColor.copy(alpha = 0.40f),
                            radius = 12f,
                            center = Offset(x, y),
                        )
                        // Handle Base
                        drawCircle(
                            color = Color(0xFF0E131E),
                            radius = 8.5f,
                            center = Offset(x, y),
                        )
                        // Handle Accent Rim
                        drawCircle(
                            color = edgeColor,
                            radius = 8.5f,
                            center = Offset(x, y),
                            style = Stroke(2f),
                        )
                        // Crisp Center Specular Dot
                        drawCircle(
                            color = Color.White,
                            radius = 3.5f,
                            center = Offset(x, y),
                        )
                    }
                }

                // Dynamic Island with Camera Lens Reflection
                val islandWidth = screen.width * 0.28f
                drawRoundRect(
                    color = Color(0xFF030406),
                    topLeft = Offset(screen.center.x - islandWidth / 2f, screen.top + 8f),
                    size = Size(islandWidth, 12f),
                    cornerRadius = CornerRadius(8f),
                )
                drawCircle(
                    color = Color(0xFF1E293B),
                    radius = 3f,
                    center = Offset(screen.center.x + islandWidth * 0.28f, screen.top + 14f),
                )
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = 0.6f),
                    radius = 1.2f,
                    center = Offset(screen.center.x + islandWidth * 0.28f, screen.top + 14f),
                )

                // Speaker Slit
                drawRoundRect(
                    color = Color(0xFF151821),
                    topLeft = Offset(screen.center.x - 14f, screen.top + 4f),
                    size = Size(28f, 2.5f),
                    cornerRadius = CornerRadius(1.5f),
                )

                // Bottom Home Indicator Bar
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.50f),
                    topLeft = Offset(screen.center.x - screen.width * 0.16f, screen.bottom - 12f),
                    size = Size(screen.width * 0.32f, 3.5f),
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
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MAP_HEIGHT)
                    .align(Alignment.TopCenter),
            ) {
                val density = LocalDensity.current
                val screen = with(density) { phoneScreenRect(maxWidth.toPx(), MAP_HEIGHT.toPx()) }
                val screenLeft = with(density) { screen.left.toDp() }
                val screenRight = with(density) { screen.right.toDp() }
                val panelGap = 12.dp
                val panelWidth = with(density) {
                    (screen.left.toDp() - panelGap).coerceIn(76.dp, 104.dp)
                }
                EdgeActionPanelDirectional(
                    title = "${context.getString(io.github.omeryol.akisgesture.R.string.edge_left)} →",
                    edge = Edge.LEFT,
                    groups = sideActionGroups(zones, Edge.LEFT),
                    iconPack = iconPack,
                    modifier = Modifier
                        .width(panelWidth)
                        .align(Alignment.CenterStart)
                        .offset(x = screenLeft - panelWidth - panelGap),
                )
                EdgeActionPanelDirectional(
                    title = "← ${context.getString(io.github.omeryol.akisgesture.R.string.edge_right)}",
                    edge = Edge.RIGHT,
                    groups = sideActionGroups(zones, Edge.RIGHT),
                    iconPack = iconPack,
                    modifier = Modifier
                        .width(panelWidth)
                        .align(Alignment.CenterStart)
                        .offset(x = screenRight + panelGap),
                )
            }
        }

        EdgeActionColumn(
            title = "${context.getString(io.github.omeryol.akisgesture.R.string.edge_bottom)} ↑",
            edge = Edge.BOTTOM,
            groups = zones.filter { it.edge == Edge.BOTTOM }.map { it.actionEntries() },
            iconPack = iconPack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )

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
                        Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = EdgeUi.color(edge), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

private fun PhoneZone.actions(): List<ActionNode> = listOfNotNull(quickAction, holdAction, lUpAction, lDownAction)

@Composable
private fun EdgeActionPanelDirectional(
    title: String,
    edge: Edge,
    groups: List<List<Pair<String, ActionNode>>>,
    iconPack: ActionIconPack,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val visibleGroups = groups.filter { it.isNotEmpty() }
    val edgeColor = EdgeUi.color(edge)
    Column(modifier = modifier.height(MAP_HEIGHT).padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = edgeColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-8).dp),
        )
        if (visibleGroups.isEmpty()) {
            Text("-", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        } else {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                visibleGroups.forEachIndexed { index, entries ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(edgeColor.copy(alpha = 0.12f))
                            .padding(horizontal = 2.dp, vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                    if (visibleGroups.size > 1) {
                        Text(
                            text = context.getString(io.github.omeryol.akisgesture.R.string.map_section_title, index + 1),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = edgeColor,
                        )
                    }
                    SideDirectionalActions(entries, edge, iconPack, scheme)
                    }
                }
            }
        }
    }
}

@Composable
private fun SideDirectionalActions(
    entries: List<Pair<String, ActionNode>>,
    edge: Edge,
    iconPack: ActionIconPack,
    scheme: androidx.compose.material3.ColorScheme,
) {
    val numbered = entries.filter { it.first == "1" || it.first == "2" }
        .let { values -> if (edge == Edge.RIGHT) values.asReversed() else values }
    val lUp = entries.firstOrNull { it.first == "L↑" }
    val lDown = entries.firstOrNull { it.first == "L↓" }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        lUp?.let { (_, action) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ActionIcon(action, null, Modifier.size(16.dp), iconPack = iconPack)
                GestureVisualIcon(
                    direction = if (edge == Edge.LEFT) GestureVisualDirection.LEFT_EDGE_UP else GestureVisualDirection.RIGHT_EDGE_UP,
                    color = scheme.tertiary,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        if (numbered.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (numbered.size == 2) {
                    ActionBadge(numbered[0].first, numbered[0].second, iconPack, scheme)
                    Text(
                        text = if (edge == Edge.LEFT) "→" else "←",
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.tertiary,
                    )
                    ActionBadge(numbered[1].first, numbered[1].second, iconPack, scheme)
                } else {
                    numbered.forEach { (kind, action) -> ActionBadge(kind, action, iconPack, scheme) }
                }
            }
        }
        lDown?.let { (_, action) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GestureVisualIcon(
                    direction = if (edge == Edge.LEFT) GestureVisualDirection.LEFT_EDGE_DOWN else GestureVisualDirection.RIGHT_EDGE_DOWN,
                    color = scheme.tertiary,
                    modifier = Modifier.size(14.dp),
                )
                ActionIcon(action, null, Modifier.size(16.dp), iconPack = iconPack)
            }
        }
    }
}

@Composable
private fun EdgeActionColumn(
    title: String,
    edge: Edge,
    groups: List<List<Pair<String, ActionNode>>>,
    iconPack: ActionIconPack,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EdgeUi.color(edge))
        Spacer(Modifier.height(10.dp))
        val visibleGroups = groups.filter { it.isNotEmpty() }
        if (visibleGroups.isEmpty()) {
            Text("-", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                visibleGroups.forEachIndexed { index, entries ->
                    Column(
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (visibleGroups.size > 1) {
                            Text(
                                text = context.getString(io.github.omeryol.akisgesture.R.string.map_section_title, index + 1),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = EdgeUi.color(edge),
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        BottomDirectionalActions(entries, iconPack, scheme)
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomDirectionalActions(
    entries: List<Pair<String, ActionNode>>,
    iconPack: ActionIconPack,
    scheme: androidx.compose.material3.ColorScheme,
) {
    val left = entries.firstOrNull { it.first == "L\u2191" }
    val right = entries.firstOrNull { it.first == "L\u2193" }
    val numbered = entries.filter { it.first == "1" || it.first == "2" }.sortedByDescending { it.first }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            left?.let { (_, action) -> ActionBadge("", action, iconPack, scheme, GestureVisualDirection.BOTTOM_LEFT) }
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            numbered.forEach { (kind, action) -> ActionBadge(kind, action, iconPack, scheme) }
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            right?.let { (_, action) -> ActionBadge("", action, iconPack, scheme, GestureVisualDirection.BOTTOM_RIGHT) }
        }
    }
}

@Composable
private fun ActionBadge(
    kind: String,
    action: ActionNode,
    iconPack: ActionIconPack,
    scheme: androidx.compose.material3.ColorScheme,
    gestureVisual: GestureVisualDirection? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 2.dp, vertical = 1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (action) {
            ActionNode.SwitchLastApp -> Icon(
                imageVector = Icons.Filled.SwapHoriz,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(16.dp),
            )
            ActionNode.SwitchNextApp -> Icon(
                imageVector = Icons.Filled.SwapHoriz,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(16.dp),
            )
            else -> ActionIcon(action, null, Modifier.size(16.dp), iconPack = iconPack)
        }
        if (gestureVisual != null) {
            GestureVisualIcon(
                direction = gestureVisual,
                color = scheme.tertiary,
                modifier = Modifier.size(14.dp),
            )
        }
        if (kind.isNotEmpty()) {
            Text(kind, style = MaterialTheme.typography.labelSmall, color = scheme.primary, maxLines = 1)
        }
    }
}

@Composable
private fun SideLActionBadge(
    action: ActionNode,
    iconPack: ActionIconPack,
    scheme: androidx.compose.material3.ColorScheme,
    direction: GestureVisualDirection,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp),
    ) {
        GestureVisualIcon(
            direction = direction,
            color = scheme.tertiary,
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.Center),
        )
        ActionIcon(
            action,
            null,
            Modifier
                .size(16.dp)
                .align(
                    if (direction == GestureVisualDirection.LEFT_EDGE_UP ||
                        direction == GestureVisualDirection.LEFT_EDGE_DOWN) Alignment.CenterStart
                    else Alignment.CenterEnd,
                ),
            iconPack = iconPack,
        )
    }
}

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
                        ActionIcon(action, null, Modifier.size(18.dp), iconPack = iconPack)
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

private fun sideActionGroups(
    zones: List<PhoneZone>,
    edge: Edge,
): List<List<Pair<String, ActionNode>>> {
    val groups = zones
        .filter { it.edge == edge }
        .sortedBy { it.start }
        .map { it.actionEntries() }
        .take(3)
        .toMutableList()
    while (groups.size < 3) groups.add(emptyList())
    return groups
}

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
    density: Float,
    icons: Map<String, androidx.compose.ui.graphics.ImageBitmap?>,
) {
    if (config == null) return

    val edgeColors = mapOf(
        Edge.LEFT to EdgeUi.color(Edge.LEFT),
        Edge.RIGHT to EdgeUi.color(Edge.RIGHT),
        Edge.BOTTOM to EdgeUi.color(Edge.BOTTOM),
    )

    EdgeUi.ordered.filter { config.ringMenuEnabledFor(it) || config.recentAppsEnabledFor(it) }.forEach { edge ->
        val isRecent = config.recentAppsEnabledFor(edge)
        val arcValue = if (isRecent) config.recentAppsArc else config.ringArc
        val sizeValue = if (isRecent) config.recentAppsSizeDp else config.ringSizeDp
        val spacingValue = if (isRecent) config.recentAppsSpacingDp else config.ringGroupSpacingDp
        val insetValue = if (isRecent) config.recentAppsInsetDp else config.ringGroupInsetDp
        val count = if (isRecent) config.recentAppsCount.coerceIn(2, 6) else 3

        val ringRadius = (sizeValue * density * 0.22f).coerceIn(13f, 24f)
        val ringSpacing = (spacingValue * density * 0.22f).coerceIn(ringRadius * 2.15f, ringRadius * 3.7f)
        val inset = (insetValue * density * 0.20f).coerceIn(ringRadius + 6f, screen.width * 0.30f)
        val middleLead = ringSpacing * 1.45f
        val m = (count - 1) / 2f
        val maxDistFromCenter = if (m > 0f) m else 1f

        val actions = if (isRecent) {
            List(count) { ActionNode.SwitchLastApp }
        } else {
            val values = config.ringActionsFor(edge).take(3)
            values + List(3 - values.size) { ActionNode.NoAction }
        }
        val centers = (0 until count).map { i ->
            val u = if (m > 0f) kotlin.math.abs(i - m) / maxDistFromCenter else 0f
            val itemLead = middleLead * (1f - arcValue.coerceIn(0f, 1f) * (u * u))
            val delta = (i - m) * ringSpacing
            when (edge) {
                Edge.LEFT -> Offset(screen.left + inset + itemLead, screen.center.y + delta)
                Edge.RIGHT -> Offset(screen.right - inset - itemLead, screen.center.y + delta)
                Edge.BOTTOM -> Offset(screen.center.x + delta, screen.bottom - inset - itemLead)
            }
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
                icons[action.id]?.let { bitmap ->
                    val size = (ringRadius * 1.05f).toInt().coerceAtLeast(1)
                    drawImage(
                        image = bitmap,
                        dstOffset = IntOffset((center.x - size / 2f).toInt(), (center.y - size / 2f).toInt()),
                        dstSize = IntSize(size, size),
                    )
                }
            }
        }
    }
}

private fun phoneScreenRect(width: Float, height: Float): Rect {
    val phoneHeight = minOf(height * 0.94f, (width * 0.50f) / 0.52f)
    val phoneWidth = phoneHeight * 0.52f
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
        Edge.LEFT, Edge.RIGHT -> {
            val sensor = sideSensorRect(zone.edge, screen, config, preview)
            Rect(
                left = sensor.left,
                top = sensor.top + zone.start.coerceIn(0f, 1f) * sensor.height,
                right = sensor.right,
                bottom = sensor.top + zone.end.coerceIn(0f, 1f) * sensor.height,
            )
        }
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
    val zones = rules.filter { it.enabled }
        .groupBy { Triple(it.trigger.edge, it.trigger.section, it.triggerMode) }
        .values
        .map { group ->
            val representative = group.first()
            val edge = representative.trigger.edge
            PhoneZone(
                edge = edge,
                start = representative.trigger.section.start,
                end = representative.trigger.section.end,
                quickAction = group.firstOrNull { it.trigger.gestureType == GestureType.QUICK_SWIPE }?.action,
                holdAction = group.firstOrNull { it.trigger.gestureType == GestureType.SWIPE_HOLD }?.action,
                lUpAction = group.firstOrNull { it.trigger.gestureType == GestureType.SWIPE_UP_L }?.action,
                lDownAction = group.firstOrNull { it.trigger.gestureType == GestureType.SWIPE_DOWN_L }?.action,
                color = EdgeUi.color(edge),
                ruleIds = group.map { it.id }.toSet(),
            )
        }
    if (BuildConfig.BUILD_TYPE == "diagnostic") {
        Log.d(
            "AkisMapDiag",
            "rules=${rules.size} enabled=${rules.count { it.enabled }} zones=${zones.joinToString { "${it.edge}:${it.start}-${it.end}:${it.ruleIds.size}" }}",
        )
    }
    return zones
}
