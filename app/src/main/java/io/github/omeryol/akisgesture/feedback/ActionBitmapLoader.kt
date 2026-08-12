package io.github.omeryol.akisgesture.feedback

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import io.github.omeryol.akisgesture.model.ActionIconPack
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.ActionVisual
import io.github.omeryol.akisgesture.model.ActionVisualResolver

object ActionBitmapLoader {
    fun load(
        context: Context,
        action: ActionNode,
        pack: ActionIconPack,
        sizePx: Int = 96,
        @ColorInt tint: Int? = null,
    ): Bitmap? {
        val visual = ActionVisualResolver.resolve(action, pack)
        val (drawable, tintDrawable) = when (visual) {
            is ActionVisual.ApplicationIcon -> {
                val installed = runCatching {
                    context.packageManager.getApplicationIcon(visual.packageName)
                }.getOrNull()
                (installed ?: AppCompatResources.getDrawable(context, visual.fallbackResourceId)) to (installed == null)
            }
            is ActionVisual.DrawableResource ->
                AppCompatResources.getDrawable(context, visual.resourceId) to true
            else -> null to false
        }
        drawable ?: return null
        val mutable = DrawableCompat.wrap(drawable.mutate())
        if (tintDrawable && tint != null) DrawableCompat.setTint(mutable, tint)
        val safeSize = sizePx.coerceAtLeast(1)
        return Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888).also { bitmap ->
            val canvas = Canvas(bitmap)
            val intrinsicWidth = mutable.intrinsicWidth.coerceAtLeast(1)
            val intrinsicHeight = mutable.intrinsicHeight.coerceAtLeast(1)
            val scale = minOf(safeSize.toFloat() / intrinsicWidth, safeSize.toFloat() / intrinsicHeight)
            val width = (intrinsicWidth * scale).toInt().coerceAtLeast(1)
            val height = (intrinsicHeight * scale).toInt().coerceAtLeast(1)
            val left = (safeSize - width) / 2
            val top = (safeSize - height) / 2
            mutable.setBounds(left, top, left + width, top + height)
            mutable.draw(canvas)
        }
    }
}
