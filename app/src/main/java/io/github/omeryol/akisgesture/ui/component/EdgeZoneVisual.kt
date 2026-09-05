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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.model.SectionRange
import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.ui.theme.EdgeUi

/**
 * Standardized, razor-sharp vector smartphone visual showing the exact active trigger section
 * with neon tube bloom. Uses authentic 19.5:9 smartphone proportions (aspect ratio 0.50).
 */
@Composable
fun EdgeZoneVisual(
    edge: Edge,
    section: SectionRange,
    modifier: Modifier = Modifier,
    zoneColor: Color = EdgeUi.color(edge),
    width: Dp = 34.dp,
    height: Dp = 68.dp,
) {
    Box(
        modifier = modifier.size(width, height),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val pad = 2.5f
            val phoneRect = Rect(pad, pad, w - pad, h - pad)

            // 1. Phone Outer Shadow / Chassis
            drawRoundRect(
                color = Color(0xFF030406),
                topLeft = phoneRect.topLeft,
                size = phoneRect.size,
                cornerRadius = CornerRadius(8f),
            )

            // 2. High-Tech Titanium Bezel Rim
            drawRoundRect(
                color = Color.White.copy(alpha = 0.12f),
                topLeft = phoneRect.topLeft,
                size = phoneRect.size,
                cornerRadius = CornerRadius(8f),
                style = Stroke(1f),
            )

            // 3. Screen Glass (OLED Deep Obsidian Black)
            val screenInset = 2.2f
            val screen = Rect(
                phoneRect.left + screenInset,
                phoneRect.top + screenInset,
                phoneRect.right - screenInset,
                phoneRect.bottom - screenInset,
            )
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0B0F17), Color(0xFF04060A)),
                    startY = screen.top,
                    endY = screen.bottom,
                ),
                topLeft = screen.topLeft,
                size = screen.size,
                cornerRadius = CornerRadius(6f),
            )

            // 4. Subtle Micro Island at Top
            val islandW = screen.width * 0.32f
            drawRoundRect(
                color = Color(0xFF010204),
                topLeft = Offset(screen.center.x - islandW / 2f, screen.top + 2.5f),
                size = Size(islandW, 2.5f),
                cornerRadius = CornerRadius(1.5f),
            )

            // 5. Inactive Edge Sensor Track (Faint outline of the whole edge)
            val trackColor = Color.White.copy(alpha = 0.05f)
            when (edge) {
                Edge.LEFT -> drawLine(color = trackColor, start = Offset(screen.left + 1f, screen.top + 6f), end = Offset(screen.left + 1f, screen.bottom - 6f), strokeWidth = 1f)
                Edge.RIGHT -> drawLine(color = trackColor, start = Offset(screen.right - 1f, screen.top + 6f), end = Offset(screen.right - 1f, screen.bottom - 6f), strokeWidth = 1f)
                Edge.BOTTOM -> drawLine(color = trackColor, start = Offset(screen.left + 6f, screen.bottom - 1f), end = Offset(screen.right - 6f, screen.bottom - 1f), strokeWidth = 1f)
            }

            // 6. Active Neon Trigger Section (Glows precisely where the section is defined)
            val coreTone = when (edge) {
                Edge.LEFT -> Color(0xFF80F7FF)
                Edge.RIGHT -> Color(0xFFF3E5F5)
                Edge.BOTTOM -> Color(0xFFFFF9C4)
            }

            when (edge) {
                Edge.LEFT -> {
                    val usableTop = screen.top + 4f
                    val usableH = screen.height - 8f
                    val startY = usableTop + section.start.coerceIn(0f, 1f) * usableH
                    val endY = usableTop + section.end.coerceIn(0f, 1f) * usableH
                    val x = screen.left + 1f

                    // Ambient Diffuse Bloom
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, zoneColor.copy(alpha = 0.50f), Color.Transparent),
                            startY = startY,
                            endY = endY,
                        ),
                        start = Offset(x, startY),
                        end = Offset(x, endY),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round,
                    )
                    // Neon Tube Body
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(zoneColor.copy(alpha = 0.40f), coreTone.copy(alpha = 0.95f), zoneColor.copy(alpha = 0.40f)),
                            startY = startY,
                            endY = endY,
                        ),
                        start = Offset(x, startY),
                        end = Offset(x, endY),
                        strokeWidth = 2.6f,
                        cap = StrokeCap.Round,
                    )
                    // Specular Core Beam
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.95f), Color.Transparent),
                            startY = startY,
                            endY = endY,
                        ),
                        start = Offset(x, startY + 1f),
                        end = Offset(x, endY - 1f),
                        strokeWidth = 1.2f,
                        cap = StrokeCap.Round,
                    )
                }
                Edge.RIGHT -> {
                    val usableTop = screen.top + 4f
                    val usableH = screen.height - 8f
                    val startY = usableTop + section.start.coerceIn(0f, 1f) * usableH
                    val endY = usableTop + section.end.coerceIn(0f, 1f) * usableH
                    val x = screen.right - 1f

                    // Ambient Diffuse Bloom
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, zoneColor.copy(alpha = 0.50f), Color.Transparent),
                            startY = startY,
                            endY = endY,
                        ),
                        start = Offset(x, startY),
                        end = Offset(x, endY),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round,
                    )
                    // Neon Tube Body
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(zoneColor.copy(alpha = 0.40f), coreTone.copy(alpha = 0.95f), zoneColor.copy(alpha = 0.40f)),
                            startY = startY,
                            endY = endY,
                        ),
                        start = Offset(x, startY),
                        end = Offset(x, endY),
                        strokeWidth = 2.6f,
                        cap = StrokeCap.Round,
                    )
                    // Specular Core Beam
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.95f), Color.Transparent),
                            startY = startY,
                            endY = endY,
                        ),
                        start = Offset(x, startY + 1f),
                        end = Offset(x, endY - 1f),
                        strokeWidth = 1.2f,
                        cap = StrokeCap.Round,
                    )
                }
                Edge.BOTTOM -> {
                    val usableLeft = screen.left + 4f
                    val usableW = screen.width - 8f
                    val startX = usableLeft + section.start.coerceIn(0f, 1f) * usableW
                    val endX = usableLeft + section.end.coerceIn(0f, 1f) * usableW
                    val y = screen.bottom - 1f

                    // Ambient Diffuse Bloom
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, zoneColor.copy(alpha = 0.50f), Color.Transparent),
                            startX = startX,
                            endX = endX,
                        ),
                        start = Offset(startX, y),
                        end = Offset(endX, y),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round,
                    )
                    // Neon Tube Body
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(zoneColor.copy(alpha = 0.40f), coreTone.copy(alpha = 0.95f), zoneColor.copy(alpha = 0.40f)),
                            startX = startX,
                            endX = endX,
                        ),
                        start = Offset(startX, y),
                        end = Offset(endX, y),
                        strokeWidth = 2.6f,
                        cap = StrokeCap.Round,
                    )
                    // Specular Core Beam
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.95f), Color.Transparent),
                            startX = startX,
                            endX = endX,
                        ),
                        start = Offset(startX + 1f, y),
                        end = Offset(endX - 1f, y),
                        strokeWidth = 1.2f,
                        cap = StrokeCap.Round,
                    )
                }
            }
        }
    }
}
