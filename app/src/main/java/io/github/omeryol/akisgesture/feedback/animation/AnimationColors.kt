package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Color

internal fun alpha(color: Int, value: Int): Int = Color.argb(
    value.coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color),
)

internal fun lighten(color: Int, amount: Float): Int = blend(color, Color.WHITE, amount)

internal fun darken(color: Int, amount: Float): Int = blend(color, Color.BLACK, amount)

internal fun blend(from: Int, to: Int, amount: Float): Int {
    val t = amount.coerceIn(0f, 1f)
    return Color.rgb(
        (Color.red(from) + (Color.red(to) - Color.red(from)) * t).toInt(),
        (Color.green(from) + (Color.green(to) - Color.green(from)) * t).toInt(),
        (Color.blue(from) + (Color.blue(to) - Color.blue(from)) * t).toInt(),
    )
}
