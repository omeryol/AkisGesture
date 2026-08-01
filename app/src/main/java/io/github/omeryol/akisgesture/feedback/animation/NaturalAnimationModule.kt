package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Canvas
import io.github.omeryol.akisgesture.overlay.Edge

data class AnimationFrame(
    val canvas: Canvas,
    val edge: Edge,
    val touch: Float,
    val stretch: Float,
    val progress: Float,
    val width: Float,
    val height: Float,
    val color: Int,
    val opacity: Float,
    val size: Float,
    val time: Double,
)

interface NaturalAnimationModule {
    fun draw(frame: AnimationFrame)
}
