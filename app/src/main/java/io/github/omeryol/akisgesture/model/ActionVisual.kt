package io.github.omeryol.akisgesture.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface ActionVisual {
    data class Vector(val imageVector: ImageVector) : ActionVisual
    data class DrawableResource(@DrawableRes val resourceId: Int) : ActionVisual
    data class TextGlyph(val value: String) : ActionVisual
    data class ApplicationIcon(
        val packageName: String,
        @DrawableRes val fallbackResourceId: Int,
    ) : ActionVisual
}
