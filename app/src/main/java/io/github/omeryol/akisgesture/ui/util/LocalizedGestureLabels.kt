package io.github.omeryol.akisgesture.ui.util

import android.content.Context
import io.github.omeryol.akisgesture.R
import io.github.omeryol.akisgesture.feedback.FeedbackAnimation
import io.github.omeryol.akisgesture.gesture.HoldFireMode
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.model.SectionRange
import io.github.omeryol.akisgesture.overlay.Edge

fun edgeLabel(context: Context, edge: Edge) = context.getString(when (edge) { Edge.LEFT -> R.string.edge_left; Edge.RIGHT -> R.string.edge_right; Edge.BOTTOM -> R.string.edge_bottom })
fun gestureLabel(context: Context, type: GestureType) = context.getString(when (type) { GestureType.QUICK_SWIPE -> R.string.gesture_quick; GestureType.SWIPE_HOLD -> R.string.gesture_hold; GestureType.SWIPE_UP_L -> R.string.gesture_l_up; GestureType.SWIPE_DOWN_L -> R.string.gesture_l_down })
fun sectionLabel(context: Context, section: SectionRange, edge: Edge): String {
    val vertical = edge != Edge.BOTTOM
    val id = when {
        section.start == 0f && section.end == 1f -> R.string.section_all
        section.start == 0f && section.end == 1f / 3f -> if (vertical) R.string.section_top else R.string.section_left
        section.start == 1f / 3f && section.end == 2f / 3f -> R.string.section_middle
        section.start == 2f / 3f && section.end == 1f -> if (vertical) R.string.section_bottom else R.string.section_right
        section.start == 0f && section.end == .5f -> if (vertical) R.string.section_top_half else R.string.section_left_half
        section.start == .5f && section.end == 1f -> if (vertical) R.string.section_bottom_half else R.string.section_right_half
        else -> return context.getString(R.string.section_custom, (section.start * 100).toInt(), (section.end * 100).toInt())
    }
    return context.getString(id)
}
fun HoldFireMode.localizedLabel(context: Context) = context.getString(if (this == HoldFireMode.ON_RELEASE) R.string.hold_on_release else R.string.hold_on_threshold)
fun FeedbackAnimation.localizedLabel(context: Context): String = context.getString(when (this) {
    FeedbackAnimation.OCEAN_WAVE -> R.string.animation_ocean; FeedbackAnimation.MERCURY_TEARDROP -> R.string.animation_droplet; FeedbackAnimation.PLASMA_FIRE -> R.string.animation_fire; FeedbackAnimation.ATMOSPHERIC_MIST -> R.string.animation_mist; FeedbackAnimation.ELECTRIC_STORM -> R.string.animation_electric; FeedbackAnimation.SOLAR_CORONA -> R.string.animation_sun; FeedbackAnimation.AURORA_RIBBON -> R.string.animation_aurora; FeedbackAnimation.GLASS_RIPPLE -> R.string.animation_glass; FeedbackAnimation.NEON_PULSE -> R.string.animation_neon; FeedbackAnimation.STARFIELD -> R.string.animation_stars; FeedbackAnimation.ICE_SHARDS -> R.string.animation_ice; FeedbackAnimation.VORTEX -> R.string.animation_vortex; FeedbackAnimation.PRISM_FLOW -> R.string.animation_rain; FeedbackAnimation.EMBER_BLOOM -> R.string.animation_ember; FeedbackAnimation.COMET_TAIL -> R.string.animation_wind; FeedbackAnimation.QUANTUM_RING -> R.string.animation_bubbles; FeedbackAnimation.INK_FLOW -> R.string.animation_ink; FeedbackAnimation.SOLAR_FLARE -> R.string.animation_heat; FeedbackAnimation.ZIPPER_VOID -> R.string.animation_elastic; FeedbackAnimation.BLACK_HOLE_PULL -> R.string.animation_night; FeedbackAnimation.MATRIX_DISSOLVE -> R.string.animation_particles; FeedbackAnimation.HYDRO_WIPE -> R.string.animation_pressure; FeedbackAnimation.DEWDROP_GLASS -> R.string.animation_dew; FeedbackAnimation.PRISM_SHATTER -> R.string.animation_prism; FeedbackAnimation.ICON_ONLY -> R.string.animation_icon_only; FeedbackAnimation.NONE -> R.string.animation_off
})
