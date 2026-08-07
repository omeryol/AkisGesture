package io.github.omeryol.akisgesture.model

import io.github.omeryol.akisgesture.R

data class ColorPalettePreset(
    val id: String,
    val nameResId: Int,
    val quickColor: Int,
    val holdColor: Int,
    val lSwipeColor: Int,
) {
    companion object {
        val presets = listOf(
            ColorPalettePreset(
                id = "deep_ocean",
                nameResId = R.string.palette_deep_ocean,
                quickColor = 0xFF3D5AFE.toInt(),
                holdColor = 0xFF00E5FF.toInt(),
                lSwipeColor = 0xFF00E676.toInt(),
            ),
            ColorPalettePreset(
                id = "sunset_pulse",
                nameResId = R.string.palette_sunset_pulse,
                quickColor = 0xFFFF3D00.toInt(),
                holdColor = 0xFFFF9100.toInt(),
                lSwipeColor = 0xFFFFEA00.toInt(),
            ),
            ColorPalettePreset(
                id = "cyber_neon",
                nameResId = R.string.palette_cyber_neon,
                quickColor = 0xFF7C4DFF.toInt(),
                holdColor = 0xFFFF007F.toInt(),
                lSwipeColor = 0xFF00E5FF.toInt(),
            ),
            ColorPalettePreset(
                id = "emerald_aurora",
                nameResId = R.string.palette_emerald_aurora,
                quickColor = 0xFF00B0FF.toInt(),
                holdColor = 0xFF00E676.toInt(),
                lSwipeColor = 0xFF76FF03.toInt(),
            ),
            ColorPalettePreset(
                id = "midnight_galaxy",
                nameResId = R.string.palette_midnight_galaxy,
                quickColor = 0xFF651FFF.toInt(),
                holdColor = 0xFF00E5FF.toInt(),
                lSwipeColor = 0xFFF50057.toInt(),
            ),
            ColorPalettePreset(
                id = "lavender_sunset",
                nameResId = R.string.palette_lavender_sunset,
                quickColor = 0xFFE040FB.toInt(),
                holdColor = 0xFFFF6E40.toInt(),
                lSwipeColor = 0xFFFFD700.toInt(),
            ),
            ColorPalettePreset(
                id = "monochrome_slate",
                nameResId = R.string.palette_monochrome_slate,
                quickColor = 0xFF607D8B.toInt(),
                holdColor = 0xFF9E9E9E.toInt(),
                lSwipeColor = 0xFFECEFF1.toInt(),
            ),
        )
    }
}
