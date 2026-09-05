package io.github.omeryol.akisgesture.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.R
import io.github.omeryol.akisgesture.model.SectionRange
import io.github.omeryol.akisgesture.overlay.Edge

/**
 * Standardized obsidian phone visual matching the app design system,
 * displaying the unified gesture illustration with an active section glow overlay.
 */
@Composable
fun EdgeZoneVisual(
    edge: Edge,
    section: SectionRange,
    modifier: Modifier = Modifier,
    zoneColor: Color = MaterialTheme.colorScheme.primary,
    width: Dp = 48.dp,
    height: Dp = 68.dp,
) {
    val illustrationRes = when (edge) {
        Edge.LEFT -> R.drawable.illus_edge_left_unified
        Edge.RIGHT -> R.drawable.illus_edge_right_unified
        Edge.BOTTOM -> R.drawable.illus_edge_bottom_unified
    }

    Box(
        modifier = modifier
            .size(width, height)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0B0F17))
            .border(1.dp, zoneColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        // Unified front-facing obsidian smartphone illustration
        Image(
            painter = painterResource(illustrationRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )

        // Active Section Indicator Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val pillThickness = 3.5.dp.toPx()
            val corner = CornerRadius(2.dp.toPx())

            when (edge) {
                Edge.LEFT -> {
                    val top = section.start * h
                    val barH = (section.end - section.start) * h
                    drawRoundRect(
                        color = zoneColor,
                        topLeft = Offset(0f, top),
                        size = Size(pillThickness, barH),
                        cornerRadius = corner,
                    )
                }
                Edge.RIGHT -> {
                    val top = section.start * h
                    val barH = (section.end - section.start) * h
                    drawRoundRect(
                        color = zoneColor,
                        topLeft = Offset(w - pillThickness, top),
                        size = Size(pillThickness, barH),
                        cornerRadius = corner,
                    )
                }
                Edge.BOTTOM -> {
                    val left = section.start * w
                    val barW = (section.end - section.start) * w
                    drawRoundRect(
                        color = zoneColor,
                        topLeft = Offset(left, h - pillThickness),
                        size = Size(barW, pillThickness),
                        cornerRadius = corner,
                    )
                }
            }
        }
    }
}
