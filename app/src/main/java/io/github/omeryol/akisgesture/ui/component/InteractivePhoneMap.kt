package io.github.omeryol.akisgesture.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.GestureRule
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.overlay.Edge

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
) {
    val scheme = MaterialTheme.colorScheme
    val zones = buildPhoneZones(rules, scheme)

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(zones) {
                    detectTapGestures { tapOffset ->
                        val z = hitTestPhoneZone(
                            tapOffset.x, tapOffset.y,
                            size.width.toFloat(), size.height.toFloat(),
                            zones,
                        )
                        z?.let { onZoneClick(it) }
                    }
                },
        ) {
            val w = size.width
            val h = size.height

            val phoneH = h * 0.88f
            val phoneW = phoneH * 0.49f
            val phoneLeft = (w - phoneW) / 2f
            val phoneTop = (h - phoneH) / 2f
            val outerCorner = CornerRadius(46f)
            val innerCorner = CornerRadius(40f)

            val phoneBody = Rect(phoneLeft, phoneTop, phoneLeft + phoneW, phoneTop + phoneH)

            // ── 1. Outer Glow and Drop Shadow ──
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(scheme.primary.copy(alpha = 0.20f), Color.Transparent),
                    center = phoneBody.center,
                    radius = phoneW * 0.9f
                ),
                topLeft = Offset(phoneBody.left - 26f, phoneBody.top - 18f),
                size = Size(phoneW + 52f, phoneH + 36f),
                cornerRadius = CornerRadius(56f)
            )

            // ── 2. Phone Outer Frame Chassis ──
            drawRoundRect(
                color = Color(0xFF0D0E14),
                topLeft = Offset(phoneBody.left - 4f, phoneBody.top - 4f),
                size = Size(phoneW + 8f, phoneH + 8f),
                cornerRadius = outerCorner
            )

            // Metallic Bezel
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF383B4F), Color(0xFF1C1D28), Color(0xFF282A37)),
                    start = phoneBody.topLeft,
                    end = phoneBody.bottomRight
                ),
                topLeft = phoneBody.topLeft,
                size = phoneBody.size,
                cornerRadius = outerCorner
            )

            // ── 3. Screen Bezel & Display Surface ──
            val screenMargin = 8f
            val screenRect = Rect(
                phoneBody.left + screenMargin,
                phoneBody.top + screenMargin,
                phoneBody.right - screenMargin,
                phoneBody.bottom - screenMargin
            )

            // Screen Dark OLED Gradient Fill
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF090A0E), Color(0xFF131522), Color(0xFF07080C))
                ),
                topLeft = screenRect.topLeft,
                size = screenRect.size,
                cornerRadius = innerCorner
            )

            // Screen Glossy Border Outline
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        scheme.primary.copy(alpha = 0.55f),
                        scheme.outline.copy(alpha = 0.25f),
                        scheme.tertiary.copy(alpha = 0.45f)
                    ),
                    start = screenRect.topLeft,
                    end = screenRect.bottomRight
                ),
                topLeft = screenRect.topLeft,
                size = screenRect.size,
                cornerRadius = innerCorner,
                style = Stroke(3f)
            )

            // ── 4. Notch / Dynamic Island ──
            val islandW = phoneW * 0.28f
            val islandH = 14f
            val islandTop = screenRect.top + 10f
            val islandLeft = screenRect.center.x - islandW / 2f

            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(islandLeft, islandTop),
                size = Size(islandW, islandH),
                cornerRadius = CornerRadius(8f)
            )
            drawCircle(
                color = Color(0xFF1A1C2B),
                radius = 3.5f,
                center = Offset(islandLeft + islandW - 14f, islandTop + islandH / 2f)
            )

            // ── 5. Home Bar Indicator ──
            val homeBarW = phoneW * 0.32f
            val homeBarH = 4.5f
            val homeBarBottom = screenRect.bottom - 12f
            val homeBarLeft = screenRect.center.x - homeBarW / 2f

            drawRoundRect(
                color = Color.White.copy(alpha = 0.40f),
                topLeft = Offset(homeBarLeft, homeBarBottom),
                size = Size(homeBarW, homeBarH),
                cornerRadius = CornerRadius(3f)
            )

            // ── 6. Draw Glowing Glass Gesture Zones aligned perfectly inside screen ──
            zones.forEach { zone ->
                val zr = phoneZoneRect(zone, screenRect)
                val zoneColor = zone.color
                val zoneCorner = CornerRadius(10f)

                // Translucent fill
                drawRoundRect(
                    color = zoneColor.copy(alpha = 0.75f),
                    topLeft = zr.topLeft,
                    size = zr.size,
                    cornerRadius = zoneCorner
                )
                // Vibrant border
                drawRoundRect(
                    color = zoneColor.copy(alpha = 0.95f),
                    topLeft = zr.topLeft,
                    size = zr.size,
                    cornerRadius = zoneCorner,
                    style = Stroke(2.5f)
                )
                // Glossy inner highlight
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.25f),
                    topLeft = Offset(zr.left + 2f, zr.top + 2f),
                    size = Size((zr.width - 4f).coerceAtLeast(1f), (zr.height * 0.35f).coerceAtLeast(1f)),
                    cornerRadius = CornerRadius(6f)
                )
            }

            // ── 7. Draw Action Symbols ──
            zones.forEach { zone ->
                val zr = phoneZoneRect(zone, screenRect)
                val action = zone.quickAction ?: zone.holdAction
                if (action != null && action !is ActionNode.NoAction) {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 20f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                        isAntiAlias = true
                        setShadowLayer(4f, 0f, 1f, android.graphics.Color.argb(140, 0, 0, 0))
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        actionSymbolShort(action),
                        zr.center.x,
                        zr.center.y + 7f,
                        paint,
                    )
                }
            }
        }
    }
}

private fun actionSymbolShort(action: ActionNode): String = when (action) {
    is ActionNode.Back -> "←"
    is ActionNode.Home -> "⌂"
    is ActionNode.Recents -> "⊞"
    is ActionNode.SwitchLastApp -> "↶"
    is ActionNode.SwitchNextApp -> "↷"
    is ActionNode.LockScreen -> "🔒"
    is ActionNode.Screenshot -> "📷"
    is ActionNode.NotificationPanel -> "🔔"
    is ActionNode.QuickSettings -> "⚙"
    is ActionNode.PowerMenu -> "⏻"
    is ActionNode.MediaPlayPause -> "▶"
    is ActionNode.Assistant -> "🤖"
    is ActionNode.ToggleFlashlight -> "🔦"
    is ActionNode.ForceStopForeground -> "✕"
    is ActionNode.LaunchApp -> "📂"
    else -> "●"
}

private fun phoneZoneRect(zone: PhoneZone, screen: Rect): Rect = when (zone.edge) {
    Edge.LEFT -> Rect(
        screen.left + 2f,
        screen.top + zone.start * screen.height + 4f,
        screen.left + 16f,
        screen.top + zone.end * screen.height - 4f,
    )
    Edge.RIGHT -> Rect(
        screen.right - 16f,
        screen.top + zone.start * screen.height + 4f,
        screen.right - 2f,
        screen.top + zone.end * screen.height - 4f,
    )
    Edge.BOTTOM -> Rect(
        screen.left + zone.start * screen.width + 4f,
        screen.bottom - 16f,
        screen.left + zone.end * screen.width - 4f,
        screen.bottom - 2f,
    )
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

private fun hitTestPhoneZone(x: Float, y: Float, w: Float, h: Float, zones: List<PhoneZone>): PhoneZone? {
    val phoneH = h * 0.88f
    val phoneW = phoneH * 0.49f
    val phoneLeft = (w - phoneW) / 2f
    val phoneTop = (h - phoneH) / 2f
    val screenMargin = 8f
    val screen = Rect(
        phoneLeft + screenMargin,
        phoneTop + screenMargin,
        phoneLeft + phoneW - screenMargin,
        phoneTop + phoneH - screenMargin,
    )

    return zones.firstOrNull { zone ->
        val zr = phoneZoneRect(zone, screen)
        val hit = Rect(
            zr.left - 24f, zr.top - 16f,
            zr.right + 24f, zr.bottom + 16f,
        )
        x in hit.left..hit.right && y in hit.top..hit.bottom
    }
}
