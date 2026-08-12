package io.github.omeryol.akisgesture.ui.component

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import io.github.omeryol.akisgesture.model.ActionIconColorMode
import io.github.omeryol.akisgesture.model.ActionIconPack
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.ActionVisual
import io.github.omeryol.akisgesture.model.ActionVisualResolver
import io.github.omeryol.akisgesture.ui.util.actionCategoryColor

val LocalActionIconColorMode = staticCompositionLocalOf { ActionIconColorMode.FUNCTIONAL }

@Composable
fun ActionIcon(
    action: ActionNode,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    iconPack: ActionIconPack = ActionIconPack.PHOSPHOR,
    colorMode: ActionIconColorMode? = null,
    tint: Color? = null,
) {
    val context = LocalContext.current
    val visual = remember(action, iconPack) { ActionVisualResolver.resolve(action, iconPack) }
    val resolvedColorMode = colorMode ?: LocalActionIconColorMode.current
    val resolvedTint = tint ?: when (resolvedColorMode) {
        ActionIconColorMode.MONOCHROME -> LocalContentColor.current
        ActionIconColorMode.THEME -> MaterialTheme.colorScheme.primary
        ActionIconColorMode.ACCENT -> MaterialTheme.colorScheme.secondary
        ActionIconColorMode.FUNCTIONAL -> actionCategoryColor(action)
        ActionIconColorMode.NEON -> Color(resolvedColorMode.resolveColorInt(action))
    }
    when (visual) {
        is ActionVisual.ApplicationIcon -> {
            val appIcon = remember(visual.packageName) {
                runCatching {
                    val drawable = context.packageManager.getApplicationIcon(visual.packageName)
                    val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 96
                    val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 96
                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
                        val canvas = Canvas(bitmap)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                    }.asImageBitmap()
                }.getOrNull()
            }
            if (appIcon != null) {
                Image(appIcon, contentDescription, modifier)
            } else {
                Icon(painterResource(visual.fallbackResourceId), contentDescription, modifier, resolvedTint)
            }
        }
        is ActionVisual.DrawableResource ->
            Icon(painterResource(visual.resourceId), contentDescription, modifier, resolvedTint)
        is ActionVisual.Vector ->
            Icon(visual.imageVector, contentDescription, modifier, resolvedTint)
        is ActionVisual.TextGlyph ->
            Text(visual.value, modifier = modifier, color = resolvedTint)
    }
}
