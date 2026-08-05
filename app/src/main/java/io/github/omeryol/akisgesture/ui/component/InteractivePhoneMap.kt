package io.github.omeryol.akisgesture.ui.component

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.GestureRule
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.ui.util.localizedLabel

import io.github.omeryol.akisgesture.model.ActionIconPack
import io.github.omeryol.akisgesture.model.toSymbol
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import io.github.omeryol.akisgesture.gesture.GestureConfig

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

@Composable
fun InteractivePhoneMap(
    rules: List<GestureRule>,
    onZoneClick: (PhoneZone) -> Unit,
    modifier: Modifier = Modifier,
    iconPack: ActionIconPack = ActionIconPack.EMOJI_MODERN,
    config: GestureConfig? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val wallpaperDrawable = remember {
        runCatching {
            android.app.WallpaperManager.getInstance(context).drawable
        }.getOrNull()
    }
    val wallpaperBitmap = remember(wallpaperDrawable) {
        wallpaperDrawable?.let { drawable ->
            runCatching {
                val width = 240
                val height = 480
                val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, width, height)
                drawable.draw(canvas)
                bitmap.asImageBitmap()
            }.getOrNull()
        }
    }
    val zones = buildPhoneZones(rules, scheme)
    val leftZones = zones.filter { it.edge == Edge.LEFT }
    val rightZones = zones.filter { it.edge == Edge.RIGHT }
    val bottomZones = zones.filter { it.edge == Edge.BOTTOM }

    val infiniteTransition = rememberInfiniteTransition(label = "radarScan")
    val radarScanY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "radarScanY",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(zones) {
                        detectTapGestures { tapOffset ->
                            val z = hitTestPhoneZone(
                                tapOffset.x, tapOffset.y,
                                size.width.toFloat(), size.height.toFloat(),
                                zones,
                                config,
                            )
                            z?.let { onZoneClick(it) }
                        }
                    },
            ) {
                val w = size.width
                val h = size.height

                // Prominent, large phone dimensions (55% enlarged)
                val phoneH = h * 0.96f
                val phoneW = (phoneH * 0.52f).coerceAtMost(w * 0.78f)
                val phoneLeft = (w - phoneW) / 2f
                val phoneTop = (h - phoneH) / 2f
                val outerCorner = CornerRadius(42f)
                val innerCorner = CornerRadius(34f)

                val phoneBody = Rect(phoneLeft, phoneTop, phoneLeft + phoneW, phoneTop + phoneH)

                // ── 1. Outer Radial Ambient Glow ──
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(scheme.primary.copy(alpha = 0.32f), scheme.primary.copy(alpha = 0.05f), Color.Transparent),
                        center = phoneBody.center,
                        radius = phoneW * 1.2f,
                    ),
                    topLeft = Offset(phoneBody.left - 24f, phoneBody.top - 16f),
                    size = Size(phoneW + 48f, phoneH + 32f),
                    cornerRadius = CornerRadius(50f),
                )

                // ── 2. Phone Frame & Bezel ──
                drawRoundRect(
                    color = Color(0xFF07080E),
                    topLeft = Offset(phoneBody.left - 4f, phoneBody.top - 4f),
                    size = Size(phoneW + 8f, phoneH + 8f),
                    cornerRadius = outerCorner,
                )

                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF35384B), Color(0xFF191A26), Color(0xFF262836)),
                        start = phoneBody.topLeft,
                        end = phoneBody.bottomRight,
                    ),
                    topLeft = phoneBody.topLeft,
                    size = phoneBody.size,
                    cornerRadius = outerCorner,
                )

                // ── 3. OLED Display Screen Surface ──
                val screenMargin = 7f
                val screenRect = Rect(
                    phoneBody.left + screenMargin,
                    phoneBody.top + screenMargin,
                    phoneBody.right - screenMargin,
                    phoneBody.bottom - screenMargin,
                )

                // Rich Cyberpunk Wallpaper Background
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E1B4B),
                            Color(0xFF0F172A),
                            Color(0xFF020617),
                        ),
                        center = Offset(screenRect.center.x, screenRect.top + screenRect.height * 0.35f),
                        radius = screenRect.width * 1.5f,
                    ),
                    topLeft = screenRect.topLeft,
                    size = screenRect.size,
                    cornerRadius = innerCorner,
                )

                // Subtle Grid Lines inside Screen
                for (i in 1 until 6) {
                    val y = screenRect.top + screenRect.height * i / 6f
                    drawLine(
                        color = scheme.outline.copy(alpha = 0.08f),
                        start = Offset(screenRect.left + 12f, y),
                        end = Offset(screenRect.right - 12f, y),
                        strokeWidth = 1f,
                    )
                }

                // Simulated Dock App Icons near bottom
                val dockY = screenRect.bottom - 32f
                val iconW = 11f
                val iconSpacing = 7f
                val totalDockW = (iconW * 4) + (iconSpacing * 3)
                val dockStart = screenRect.center.x - totalDockW / 2f

                val appColors = listOf(
                    Color(0xFF38BDF8),
                    Color(0xFF818CF8),
                    Color(0xFF34D399),
                    Color(0xFFF472B6),
                )
                appColors.forEachIndexed { index, color ->
                    val ix = dockStart + index * (iconW + iconSpacing)
                    drawRoundRect(
                        color = color.copy(alpha = 0.65f),
                        topLeft = Offset(ix, dockY),
                        size = Size(iconW, iconW),
                        cornerRadius = CornerRadius(3f),
                    )
                }

                // Glossy Outline Border
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            scheme.primary.copy(alpha = 0.65f),
                            scheme.outline.copy(alpha = 0.15f),
                            scheme.tertiary.copy(alpha = 0.45f),
                        ),
                        start = screenRect.topLeft,
                        end = screenRect.bottomRight,
                    ),
                    topLeft = screenRect.topLeft,
                    size = screenRect.size,
                    cornerRadius = innerCorner,
                    style = Stroke(2.5f),
                )

                // ── 4. Dynamic Island Notch & Home Bar ──
                val islandW = phoneW * 0.28f
                val islandH = 12f
                val islandTop = screenRect.top + 8f
                val islandLeft = screenRect.center.x - islandW / 2f

                drawRoundRect(
                    color = Color(0xFF020204),
                    topLeft = Offset(islandLeft, islandTop),
                    size = Size(islandW, islandH),
                    cornerRadius = CornerRadius(6f),
                )

                val homeBarW = phoneW * 0.35f
                val homeBarH = 4f
                val homeBarBottom = screenRect.bottom - 10f
                val homeBarLeft = screenRect.center.x - homeBarW / 2f

                drawRoundRect(
                    color = Color.White.copy(alpha = 0.40f),
                    topLeft = Offset(homeBarLeft, homeBarBottom),
                    size = Size(homeBarW, homeBarH),
                    cornerRadius = CornerRadius(2.5f),
                )

                // ── 5. Draw Gesture Zones Inside Phone Screen Mask ──
                val screenClip = androidx.compose.ui.graphics.Path().apply {
                    addRoundRect(RoundRect(screenRect, innerCorner))
                }
                clipPath(screenClip) {
                    if (wallpaperBitmap != null) {
                        drawImage(
                            image = wallpaperBitmap,
                            dstOffset = IntOffset(screenRect.left.toInt(), screenRect.top.toInt()),
                            dstSize = IntSize(screenRect.width.toInt(), screenRect.height.toInt()),
                            alpha = 0.38f,
                        )
                    }

                    // Live Radar Neon Scan Line
                    val scanY = screenRect.top + radarScanY * screenRect.height
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, scheme.primary.copy(alpha = 0.50f), Color.Transparent),
                            startY = scanY - 18f,
                            endY = scanY + 18f,
                        ),
                        start = Offset(screenRect.left, scanY),
                        end = Offset(screenRect.right, scanY),
                        strokeWidth = 3f,
                    )

                    zones.forEach { zone ->
                        val zr = phoneZoneRect(zone, screenRect, config)
                        val zoneColor = zone.color
                        val zoneCorner = CornerRadius(10f)

                        // Translucent Glow Fill
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    zoneColor.copy(alpha = 0.90f),
                                    zoneColor.copy(alpha = 0.50f),
                                ),
                            ),
                            topLeft = zr.topLeft,
                            size = zr.size,
                            cornerRadius = zoneCorner,
                        )

                        // Neon Border Outline
                        drawRoundRect(
                            color = zoneColor,
                            topLeft = zr.topLeft,
                            size = zr.size,
                            cornerRadius = zoneCorner,
                            style = Stroke(2.5f),
                        )

                        // Highlight Dot
                        val hasHold = zone.holdAction != null
                        drawCircle(
                            color = if (hasHold) Color.White else Color.White.copy(alpha = 0.60f),
                            radius = 3.5f,
                            center = Offset(zr.center.x, zr.center.y),
                        )
                    }
                }

                // ── 6. Direct On-Zone Action Overlay Badges (Zero Lines, Direct Overlay) ──
                zones.forEach { zone ->
                    val action = zone.quickAction ?: zone.holdAction
                    if (action != null && action !is ActionNode.NoAction) {
                        val zr = phoneZoneRect(zone, screenRect, config)
                        val labelText = "${action.toSymbol(iconPack)} ${actionSymbolShort(action)}"

                        val badgeX = when (zone.edge) {
                            Edge.LEFT -> zr.left + 40f
                            Edge.RIGHT -> zr.right - 40f
                            Edge.BOTTOM -> zr.center.x
                        }
                        val badgeY = when (zone.edge) {
                            Edge.LEFT -> zr.center.y
                            Edge.RIGHT -> zr.center.y
                            Edge.BOTTOM -> (zr.top - 14f).coerceAtLeast(screenRect.top + 20f)
                        }

                        drawCalloutPill(badgeX, badgeY, labelText, zone.color, this)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── 7. Bottom Edge Interactive Quick Chips ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val edgeItems = listOf(
                Triple("← Sol Kenar", Edge.LEFT, leftZones),
                Triple("→ Sağ Kenar", Edge.RIGHT, rightZones),
                Triple("↓ Alt Kenar", Edge.BOTTOM, bottomZones),
            )

            edgeItems.forEach { (title, edge, edgeZones) ->
                val primaryAction = edgeZones.firstOrNull { it.quickAction != null || it.holdAction != null }?.let {
                    it.quickAction ?: it.holdAction
                }
                val actionLabel = primaryAction?.localizedLabel(context) ?: "Ayarla"

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(scheme.surfaceVariant.copy(alpha = 0.40f))
                        .clickable {
                            val targetZone = edgeZones.firstOrNull() ?: PhoneZone(
                                edge = edge,
                                start = 0f,
                                end = 1f,
                                quickAction = null,
                                holdAction = null,
                                color = scheme.primary,
                                ruleIds = emptySet(),
                            )
                            onZoneClick(targetZone)
                        }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.primary,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

private fun drawCalloutPill(
    x: Float,
    y: Float,
    text: String,
    accentColor: Color,
    drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
) {
    val paintBg = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(235, 14, 16, 26)
        style = android.graphics.Paint.Style.FILL
        isAntiAlias = true
        setShadowLayer(6f, 0f, 2f, android.graphics.Color.argb(140, 0, 0, 0))
    }
    val paintText = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 19f
        textAlign = android.graphics.Paint.Align.CENTER
        isFakeBoldText = true
        isAntiAlias = true
    }

    val fontMetrics = paintText.fontMetrics
    val textWidth = paintText.measureText(text)
    val paddingH = 12f
    val paddingV = 7f

    val pillRect = android.graphics.RectF(
        x - textWidth / 2f - paddingH,
        y + fontMetrics.top - paddingV,
        x + textWidth / 2f + paddingH,
        y + fontMetrics.bottom + paddingV,
    )

    drawScope.drawContext.canvas.nativeCanvas.drawRoundRect(pillRect, 10f, 10f, paintBg)

    val paintBorder = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(190, accentColor.red.toColorInt(), accentColor.green.toColorInt(), accentColor.blue.toColorInt())
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }
    drawScope.drawContext.canvas.nativeCanvas.drawRoundRect(pillRect, 10f, 10f, paintBorder)

    drawScope.drawContext.canvas.nativeCanvas.drawText(text, x, y, paintText)
}

private fun Float.toColorInt(): Int = (this * 255f).coerceIn(0f, 255f).toInt()

private fun actionEmoji(action: ActionNode): String = when (action) {
    is ActionNode.Back -> "🔙"
    is ActionNode.Home -> "🏠"
    is ActionNode.Recents -> "📑"
    is ActionNode.SwitchLastApp -> "🔀"
    is ActionNode.SwitchNextApp -> "⏩"
    is ActionNode.LockScreen -> "🔒"
    is ActionNode.Screenshot -> "📸"
    is ActionNode.NotificationPanel -> "🔔"
    is ActionNode.QuickSettings -> "⚙️"
    is ActionNode.PowerMenu -> "⚡"
    is ActionNode.MediaPlayPause -> "▶️"
    is ActionNode.Assistant -> "🤖"
    is ActionNode.ToggleFlashlight -> "🔦"
    is ActionNode.ForceStopForeground -> "🛑"
    is ActionNode.LaunchApp -> "📱"
    else -> "⚡"
}

private fun actionSymbolShort(action: ActionNode): String = when (action) {
    is ActionNode.Back -> "Geri"
    is ActionNode.Home -> "Ana Sayfa"
    is ActionNode.Recents -> "Son"
    is ActionNode.SwitchLastApp -> "Son App"
    is ActionNode.SwitchNextApp -> "Sonraki App"
    is ActionNode.LockScreen -> "Kilit"
    is ActionNode.Screenshot -> "Ekran Gör."
    is ActionNode.NotificationPanel -> "Bildirimler"
    is ActionNode.QuickSettings -> "Ayarlar"
    is ActionNode.PowerMenu -> "Güç"
    is ActionNode.MediaPlayPause -> "Oynat"
    is ActionNode.Assistant -> "Asistan"
    is ActionNode.ToggleFlashlight -> "Fener"
    is ActionNode.ForceStopForeground -> "Kapat"
    is ActionNode.LaunchApp -> "Uygulama"
    else -> "Eylem"
}

private fun phoneZoneRect(zone: PhoneZone, screen: Rect, config: GestureConfig? = null): Rect {
    val leftThickness = config?.let { (it.leftTriggerWidthDp / 60f * (screen.width * 0.18f)).coerceIn(8f, screen.width * 0.28f) } ?: 16f
    val rightThickness = config?.let { (it.rightTriggerWidthDp / 60f * (screen.width * 0.18f)).coerceIn(8f, screen.width * 0.28f) } ?: 16f
    val bottomThickness = config?.let { (it.bottomTriggerHeightDp / 60f * (screen.height * 0.18f)).coerceIn(8f, screen.height * 0.25f) } ?: 16f

    val (vStartLeft, vEndLeft) = config?.verticalRangeFor(Edge.LEFT) ?: (zone.start to zone.end)
    val (vStartRight, vEndRight) = config?.verticalRangeFor(Edge.RIGHT) ?: (zone.start to zone.end)

    return when (zone.edge) {
        Edge.LEFT -> Rect(
            screen.left + 2f,
            screen.top + vStartLeft * screen.height + 4f,
            screen.left + 2f + leftThickness,
            screen.top + vEndLeft * screen.height - 4f,
        )
        Edge.RIGHT -> Rect(
            screen.right - 2f - rightThickness,
            screen.top + vStartRight * screen.height + 4f,
            screen.right - 2f,
            screen.top + vEndRight * screen.height - 4f,
        )
        Edge.BOTTOM -> Rect(
            screen.left + zone.start * screen.width + 4f,
            screen.bottom - 2f - bottomThickness,
            screen.left + zone.end * screen.width - 4f,
            screen.bottom - 2f,
        )
    }
}

private fun buildPhoneZones(rules: List<GestureRule>, scheme: androidx.compose.material3.ColorScheme): List<PhoneZone> {
    val colors = listOf(
        Color(0xFF3D5AFE), Color(0xFF00E676), Color(0xFFFF9100),
        Color(0xFFFF1744), Color(0xFFD500F9), Color(0xFF00E5FF),
    )
    return rules
        .filter { it.enabled }
        .groupBy { Triple(it.trigger.edge, it.trigger.section, it.triggerMode) }
        .values
        .mapIndexed { idx, group ->
            val rep = group.first()
            val quick = group.firstOrNull { it.trigger.gestureType == GestureType.QUICK_SWIPE }?.action
            val hold = group.firstOrNull { it.trigger.gestureType == GestureType.SWIPE_HOLD }?.action
            val lUp = group.firstOrNull { it.trigger.gestureType == GestureType.SWIPE_UP_L }?.action
            val lDown = group.firstOrNull { it.trigger.gestureType == GestureType.SWIPE_DOWN_L }?.action
            PhoneZone(
                edge = rep.trigger.edge,
                start = rep.trigger.section.start,
                end = rep.trigger.section.end,
                quickAction = quick,
                holdAction = hold,
                lUpAction = lUp,
                lDownAction = lDown,
                color = colors[idx % colors.size],
                ruleIds = group.map { it.id }.toSet(),
            )
        }
}

private fun hitTestPhoneZone(x: Float, y: Float, w: Float, h: Float, zones: List<PhoneZone>, config: GestureConfig? = null): PhoneZone? {
    val phoneH = h * 0.96f
    val phoneW = (phoneH * 0.52f).coerceAtMost(w * 0.78f)
    val phoneLeft = (w - phoneW) / 2f
    val phoneTop = (h - phoneH) / 2f
    val screenMargin = 7f
    val screen = Rect(
        phoneLeft + screenMargin,
        phoneTop + screenMargin,
        phoneLeft + phoneW - screenMargin,
        phoneTop + phoneH - screenMargin,
    )

    return zones.firstOrNull { zone ->
        val zr = phoneZoneRect(zone, screen, config)
        val hit = Rect(
            zr.left - 30f, zr.top - 20f,
            zr.right + 30f, zr.bottom + 20f,
        )
        x in hit.left..hit.right && y in hit.top..hit.bottom
    }
}
