package io.github.omeryol.akisgesture.feedback

/** Natural motion families used by the data-driven renderer. */
enum class NaturalMotion {
    WATER,
    DROPLET,
    MIST,
    CONVECTION,
    ELECTRIC,
    AURORA,
    GLASS,
    PARTICLES,
    VORTEX,
    FRACTURE,
    MINIMAL,
    NONE,
}

/**
 * Physical coefficients are artistic, normalized approximations. They keep
 * every style on the same motion model instead of using unrelated effects.
 * Legacy names are migrated to their surviving canonical styles on load.
 */
enum class FeedbackAnimation(
    val label: String,
    val motion: NaturalMotion,
    val viscosity: Float,
    val surfaceTension: Float,
    val damping: Float,
) {
    OCEAN_WAVE("Su Yüzeyi", NaturalMotion.WATER, .18f, .58f, .22f),
    MERCURY_TEARDROP("Yüzey Gerilimli Damla", NaturalMotion.DROPLET, .34f, .92f, .30f),
    PLASMA_FIRE("Doğal Alev", NaturalMotion.CONVECTION, .08f, .10f, .18f),
    ATMOSPHERIC_MIST("Yavaş Sis", NaturalMotion.MIST, .72f, .04f, .56f),
    SOLAR_CORONA("Isı Koronası", NaturalMotion.CONVECTION, .12f, .12f, .16f),
    AURORA_RIBBON("Aurora Akışı", NaturalMotion.AURORA, .48f, .18f, .34f),
    GLASS_RIPPLE("Frosted Glass Dalga", NaturalMotion.GLASS, .42f, .84f, .44f),
    STARFIELD("Yıldız Akışı", NaturalMotion.PARTICLES, .10f, .05f, .24f),
    VORTEX("Su Girdabı", NaturalMotion.VORTEX, .22f, .36f, .26f),
    PRISM_FLOW("Yağmur", NaturalMotion.GLASS, .38f, .80f, .38f),
    COMET_TAIL("Rüzgâr Akışı", NaturalMotion.PARTICLES, .14f, .06f, .28f),
    QUANTUM_RING("Su Baloncukları", NaturalMotion.GLASS, .28f, .74f, .48f),
    INK_FLOW("Suda Mürekkep", NaturalMotion.MIST, .62f, .16f, .42f),
    BLACK_HOLE_PULL("Gece Işığı", NaturalMotion.VORTEX, .16f, .20f, .20f),
    HYDRO_WIPE("Basınç Dalgası", NaturalMotion.WATER, .12f, .50f, .18f),
    NONE("Kapalı", NaturalMotion.NONE, 1f, 0f, 1f),

    ;

    companion object {
        private val legacyAliases = mapOf(
            "DEWDROP_GLASS" to MERCURY_TEARDROP,
            "ICE_SHARDS" to GLASS_RIPPLE,
            "PRISM_SHATTER" to GLASS_RIPPLE,
            "ZIPPER_VOID" to GLASS_RIPPLE,
            "ICON_ONLY" to GLASS_RIPPLE,
            "MATRIX_DISSOLVE" to INK_FLOW,
            "SOLAR_FLARE" to PLASMA_FIRE,
            "EMBER_BLOOM" to PLASMA_FIRE,
            "ELECTRIC_STORM" to HYDRO_WIPE,
            "NEON_PULSE" to HYDRO_WIPE,
            "FLUID" to OCEAN_WAVE,
            "WATER" to OCEAN_WAVE,
            "OCEAN_LIQUID" to OCEAN_WAVE,
            "MINIMAL_PADDLE" to OCEAN_WAVE,
            "TEARDROP" to MERCURY_TEARDROP,
            "BUBBLE" to MERCURY_TEARDROP,
            "FIRE" to PLASMA_FIRE,
            "STEAM" to ATMOSPHERIC_MIST,
            "LIGHTNING" to HYDRO_WIPE,
            "CYBER_HEX" to HYDRO_WIPE,
            "SUN" to SOLAR_CORONA,
            "ORB_GLOW" to SOLAR_CORONA,
        )

        fun fromStoredName(name: String?): FeedbackAnimation? {
            if (name.isNullOrBlank()) return null
            return entries.firstOrNull { it.name == name } ?: legacyAliases[name]
        }
    }
}

enum class FeedbackIcon(val label: String, val symbol: String) {
    CHEVRON("Yön Oku", "›"),
    ARROW_LEFT("Sol", "‹"),
    ARROW_RIGHT("Sağ", "›"),
    HOME("Ana Ekran", "⌂"),
    RECENTS("Son Uygulamalar", "▣"),
    LOCK("Kilit", "▢"),
    CAMERA("Kamera", "◉"),
    FLASHLIGHT("Fener", "✦"),
    STAR("Yıldız", "✦"),
    HEART("Kalp", "♥"),
    FIRE("Ateş", "◆"),
    ROCKET("Başlat", "▲"),
    CLOSE("Kapat", "×"),
    NONE("Simge yok", ""),
}
