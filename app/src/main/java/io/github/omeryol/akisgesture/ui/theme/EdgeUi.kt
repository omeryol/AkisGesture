package io.github.omeryol.akisgesture.ui.theme

import androidx.compose.ui.graphics.Color
import io.github.omeryol.akisgesture.overlay.Edge

/** Shared reading order and colour identity for all edge controls. */
object EdgeUi {
    val ordered = listOf(Edge.LEFT, Edge.BOTTOM, Edge.RIGHT)
    fun color(edge: Edge): Color = when (edge) {
        Edge.LEFT -> Color(0xFF1E88E5)
        Edge.BOTTOM -> Color(0xFFFF8F00)
        Edge.RIGHT -> Color(0xFF8E24AA)
    }
}
