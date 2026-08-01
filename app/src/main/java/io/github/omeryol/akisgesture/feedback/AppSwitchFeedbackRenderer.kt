package io.github.omeryol.akisgesture.feedback

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import io.github.omeryol.akisgesture.gesture.model.SwipeDirection

class AppSwitchFeedbackRenderer {
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.WHITE
    }
    private val arrow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        color = Color.WHITE
    }
    private val bounds = RectF()

    fun draw(
        canvas: Canvas,
        direction: SwipeDirection,
        touchX: Float,
        progress: Float,
        armed: Boolean,
        color: Int,
        opacity: Float,
    ) {
        val width = if (armed) 126f else 106f
        val height = if (armed) 62f else 54f
        val centerX = touchX.coerceIn(width / 2f + 12f, canvas.width - width / 2f - 12f)
        val centerY = canvas.height - 76f
        bounds.set(
            centerX - width / 2f,
            centerY - height / 2f,
            centerX + width / 2f,
            centerY + height / 2f,
        )
        fill.color = color
        fill.alpha = ((155 + progress.coerceIn(0f, 1f) * 70f) * opacity)
            .toInt()
            .coerceIn(0, 255)
        canvas.drawRoundRect(bounds, height / 2f, height / 2f, fill)
        if (armed) canvas.drawRoundRect(bounds, height / 2f, height / 2f, stroke)

        arrow.textSize = if (armed) 46f else 38f
        arrow.alpha = (170 + progress.coerceIn(0f, 1f) * 85f).toInt()
        val symbol = if (direction == SwipeDirection.LEFT) "‹" else "›"
        val baseline = centerY - (arrow.ascent() + arrow.descent()) / 2f
        canvas.drawText(symbol, centerX, baseline, arrow)
    }
}
