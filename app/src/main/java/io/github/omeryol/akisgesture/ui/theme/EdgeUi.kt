package io.github.omeryol.akisgesture.ui.theme

import androidx.compose.ui.graphics.Color
import io.github.omeryol.akisgesture.overlay.Edge

/** Shared reading order and colour identity for all edge controls. */
object EdgeUi {
    val ordered = listOf(Edge.LEFT, Edge.BOTTOM, Edge.RIGHT)
    fun color(edge: Edge): Color = when (edge) {
        Edge.LEFT -> Color(0xFF00E5FF)   // Vibrant Neon Cyber Cyan (matched to master visual)
        Edge.BOTTOM -> Color(0xFFFFB300) // Radiant Golden Amber (matched to master visual)
        Edge.RIGHT -> Color(0xFFD500F9)  // Electric Neon Purple (matched to master visual)
    }
}
