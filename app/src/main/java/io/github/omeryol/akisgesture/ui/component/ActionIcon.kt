package io.github.omeryol.akisgesture.ui.component

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.ui.util.actionCategoryColor
import io.github.omeryol.akisgesture.ui.util.actionImageVector

@Composable
fun ActionIcon(
    action: ActionNode,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = actionCategoryColor(action),
) {
    val context = LocalContext.current
    val appIcon = if (action is ActionNode.LaunchApp) {
        remember(action.packageName) {
            runCatching {
                val drawable = context.packageManager.getApplicationIcon(action.packageName)
                val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 96
                val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 96
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                }.asImageBitmap()
            }.getOrNull()
        }
    } else {
        null
    }

    if (appIcon != null) {
        Image(
            bitmap = appIcon,
            contentDescription = contentDescription,
            modifier = modifier,
        )
    } else {
        Icon(
            imageVector = actionImageVector(action),
            contentDescription = contentDescription,
            tint = tint,
            modifier = modifier,
        )
    }
}
