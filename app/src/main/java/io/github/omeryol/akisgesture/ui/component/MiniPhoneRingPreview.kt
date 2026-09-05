package io.github.omeryol.akisgesture.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.ui.theme.EdgeUi

/**
 * Ultra-sharp vector mini-phone preview for Ring Menu and Recent Apps cards.
 * Never degrades in quality; renders edge-specific neon bloom and correct inward dock orientations.
 */
@Composable
fun MiniPhoneRingPreview(
    edge: Edge,
    isRecentApps: Boolean,
    modifier: Modifier = Modifier.size(width = 62.dp, height = 86.dp),
) {
    val edgeColor = EdgeUi.color(edge)
    val coreTone = when (edge) {
        Edge.LEFT -> Color(0xFF80F7FF)
        Edge.RIGHT -> Color(0xFFF3E5F5)
        Edge.BOTTOM -> Color(0xFFFFF9C4)
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val pad = 3.5f
            val phoneRect = Rect(pad, pad, w - pad, h - pad)

            // 1. Phone Outer Shadow / Chassis
            drawRoundRect(
                color = Color(0xFF030406),
                topLeft = phoneRect.topLeft,
                size = phoneRect.size,
                cornerRadius = CornerRadius(10f),
            )

            // 2. Bezel Rim (High-Tech Titanium Edge)
            drawRoundRect(
                color = Color.White.copy(alpha = 0.14f),
                topLeft = phoneRect.topLeft,
                size = phoneRect.size,
                cornerRadius = CornerRadius(10f),
                style = Stroke(1.2f),
            )

            // 3. Screen Glass (OLED Deep Black with subtle Cyber Gradient)
            val screenInset = 3.2f
            val screen = Rect(
                phoneRect.left + screenInset,
                phoneRect.top + screenInset,
                phoneRect.right - screenInset,
                phoneRect.bottom - screenInset,
            )
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0B0F17), Color(0xFF05070B)),
                    startY = screen.top,
                    endY = screen.bottom,
                ),
                topLeft = screen.topLeft,
                size = screen.size,
                cornerRadius = CornerRadius(8f),
            )

            // 4. Subtle Cybernetic HUD Crosshair in Screen Center
            val hudColor = Color.White.copy(alpha = 0.07f)
            val cx = screen.center.x
            val cy = screen.center.y
            drawLine(color = hudColor, start = Offset(cx - 8f, cy), end = Offset(cx + 8f, cy), strokeWidth = 1f)
            drawLine(color = hudColor, start = Offset(cx, cy - 8f), end = Offset(cx, cy + 8f), strokeWidth = 1f)

            // 5. Dynamic Island / Speaker Pill at Top
            val islandW = screen.width * 0.32f
            drawRoundRect(
                color = Color(0xFF020305),
                topLeft = Offset(cx - islandW / 2f, screen.top + 3f),
                size = Size(islandW, 3.5f),
                cornerRadius = CornerRadius(2f),
            )

            // 6. Neon Glowing Edge Tube (with center flare and end fading)
            when (edge) {
                Edge.LEFT -> {
                    val tubeX = screen.left + 1.2f
                    val topY = screen.top + screen.height * 0.18f
                    val botY = screen.bottom - screen.height * 0.18f
                    val tubeH = botY - topY

                    // Ambient Flare Leak
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(edgeColor.copy(alpha = 0.38f), Color.Transparent),
                            center = Offset(tubeX + 2f, (topY + botY) / 2f),
                            radius = tubeH * 0.45f,
                        ),
                        radius = tubeH * 0.45f,
                        center = Offset(tubeX + 2f, (topY + botY) / 2f),
                    )

                    // Tube Bloom Halo
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, edgeColor.copy(alpha = 0.70f), Color.Transparent),
                            startY = topY,
                            endY = botY,
                        ),
                        start = Offset(tubeX, topY),
                        end = Offset(tubeX, botY),
                        strokeWidth = 6.5f,
                        cap = StrokeCap.Round,
                    )

                    // Specular Core Beam
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.95f), Color.Transparent),
                            startY = topY,
                            endY = botY,
                        ),
                        start = Offset(tubeX, topY),
                        end = Offset(tubeX, botY),
                        strokeWidth = 1.8f,
                        cap = StrokeCap.Round,
                    )
                }
                Edge.RIGHT -> {
                    val tubeX = screen.right - 1.2f
                    val topY = screen.top + screen.height * 0.18f
                    val botY = screen.bottom - screen.height * 0.18f
                    val tubeH = botY - topY

                    // Ambient Flare Leak
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(edgeColor.copy(alpha = 0.38f), Color.Transparent),
                            center = Offset(tubeX - 2f, (topY + botY) / 2f),
                            radius = tubeH * 0.45f,
                        ),
                        radius = tubeH * 0.45f,
                        center = Offset(tubeX - 2f, (topY + botY) / 2f),
                    )

                    // Tube Bloom Halo
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, edgeColor.copy(alpha = 0.70f), Color.Transparent),
                            startY = topY,
                            endY = botY,
                        ),
                        start = Offset(tubeX, topY),
                        end = Offset(tubeX, botY),
                        strokeWidth = 6.5f,
                        cap = StrokeCap.Round,
                    )

                    // Specular Core Beam
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.95f), Color.Transparent),
                            startY = topY,
                            endY = botY,
                        ),
                        start = Offset(tubeX, topY),
                        end = Offset(tubeX, botY),
                        strokeWidth = 1.8f,
                        cap = StrokeCap.Round,
                    )
                }
                Edge.BOTTOM -> {
                    val tubeY = screen.bottom - 1.2f
                    val leftX = screen.left + screen.width * 0.18f
                    val rightX = screen.right - screen.width * 0.18f
                    val tubeW = rightX - leftX

                    // Ambient Flare Leak
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(edgeColor.copy(alpha = 0.38f), Color.Transparent),
                            center = Offset((leftX + rightX) / 2f, tubeY - 2f),
                            radius = tubeW * 0.45f,
                        ),
                        radius = tubeW * 0.45f,
                        center = Offset((leftX + rightX) / 2f, tubeY - 2f),
                    )

                    // Tube Bloom Halo
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, edgeColor.copy(alpha = 0.70f), Color.Transparent),
                            startX = leftX,
                            endX = rightX,
                        ),
                        start = Offset(leftX, tubeY),
                        end = Offset(rightX, tubeY),
                        strokeWidth = 6.5f,
                        cap = StrokeCap.Round,
                    )

                    // Specular Core Beam
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.95f), Color.Transparent),
                            startX = leftX,
                            endX = rightX,
                        ),
                        start = Offset(leftX, tubeY),
                        end = Offset(rightX, tubeY),
                        strokeWidth = 1.8f,
                        cap = StrokeCap.Round,
                    )
                }
            }

            // 7. Render 3 Dock Pods (Arching correctly inward from the edge)
            val podCount = 3
            val podRadius = 6.2f
            val spacing = podRadius * 2.8f
            val baseInset = podRadius + 4f
            val lead = 5.5f

            val podCenters = (0 until podCount).map { i ->
                val m = (podCount - 1) / 2f
                val u = kotlin.math.abs(i - m)
                val itemLead = lead * (1f - u * 0.45f)
                val delta = (i - m) * spacing

                when (edge) {
                    Edge.LEFT -> Offset(screen.left + baseInset + itemLead, cy + delta)
                    Edge.RIGHT -> Offset(screen.right - baseInset - itemLead, cy + delta)
                    Edge.BOTTOM -> Offset(cx + delta, screen.bottom - baseInset - itemLead)
                }
            }

            podCenters.forEach { center ->
                // Pod Outer Diffuse Glow
                drawCircle(
                    color = edgeColor.copy(alpha = 0.22f),
                    radius = podRadius + 3f,
                    center = center,
                )
                // Pod Dark Glass Background
                drawCircle(
                    color = Color(0xFF0C101A),
                    radius = podRadius,
                    center = center,
                )
                // Pod Specular Neon Rim
                drawCircle(
                    color = edgeColor.copy(alpha = 0.90f),
                    radius = podRadius,
                    center = center,
                    style = Stroke(1.4f),
                )

                // Pod Icon Content: Recent Apps (⇄ symbol) or Ring Menu (Luminous Cyber Dot/Diamond)
                if (isRecentApps) {
                    val arrowW = podRadius * 0.72f
                    val arrowH = podRadius * 0.42f
                    // Top right arrow (→)
                    drawLine(
                        color = Color.White.copy(alpha = 0.92f),
                        start = Offset(center.x - arrowW, center.y - arrowH),
                        end = Offset(center.x + arrowW, center.y - arrowH),
                        strokeWidth = 1.1f,
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.92f),
                        start = Offset(center.x + arrowW - 1.5f, center.y - arrowH - 1.5f),
                        end = Offset(center.x + arrowW, center.y - arrowH),
                        strokeWidth = 1.1f,
                    )
                    // Bottom left arrow (←)
                    drawLine(
                        color = Color.White.copy(alpha = 0.92f),
                        start = Offset(center.x + arrowW, center.y + arrowH),
                        end = Offset(center.x - arrowW, center.y + arrowH),
                        strokeWidth = 1.1f,
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.92f),
                        start = Offset(center.x - arrowW + 1.5f, center.y + arrowH + 1.5f),
                        end = Offset(center.x - arrowW, center.y + arrowH),
                        strokeWidth = 1.1f,
                    )
                } else {
                    // Ring Menu: Glowing Core Specular Pill / Dot
                    drawCircle(
                        color = Color.White.copy(alpha = 0.95f),
                        radius = 2.2f,
                        center = center,
                    )
                    drawCircle(
                        color = coreTone.copy(alpha = 0.60f),
                        radius = 3.6f,
                        center = center,
                        style = Stroke(0.9f),
                    )
                }
            }
        }
    }
}
