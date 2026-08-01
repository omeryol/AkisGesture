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
 * Existing enum names remain stable so saved preferences continue to load.
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
    ELECTRIC_STORM("Elektrik Arkı", NaturalMotion.ELECTRIC, .02f, .06f, .12f),
    SOLAR_CORONA("Isı Koronası", NaturalMotion.CONVECTION, .12f, .12f, .16f),
    AURORA_RIBBON("Aurora Akışı", NaturalMotion.AURORA, .48f, .18f, .34f),
    GLASS_RIPPLE("Frosted Glass Dalga", NaturalMotion.GLASS, .42f, .84f, .44f),
    NEON_PULSE("Sönümlü Işık Halkası", NaturalMotion.GLASS, .30f, .76f, .52f),
    STARFIELD("Yıldız Akışı", NaturalMotion.PARTICLES, .10f, .05f, .24f),
    ICE_SHARDS("Buz Gerilim Çatlağı", NaturalMotion.FRACTURE, .90f, .22f, .74f),
    VORTEX("Su Girdabı", NaturalMotion.VORTEX, .22f, .36f, .26f),
    PRISM_FLOW("Yağmur", NaturalMotion.GLASS, .38f, .80f, .38f),
    EMBER_BLOOM("Yükselen Kor", NaturalMotion.PARTICLES, .06f, .04f, .18f),
    COMET_TAIL("Rüzgâr Akışı", NaturalMotion.PARTICLES, .14f, .06f, .28f),
    QUANTUM_RING("Su Baloncukları", NaturalMotion.GLASS, .28f, .74f, .48f),
    INK_FLOW("Suda Mürekkep", NaturalMotion.MIST, .62f, .16f, .42f),
    SOLAR_FLARE("Isıl Akış", NaturalMotion.CONVECTION, .10f, .08f, .14f),
    ZIPPER_VOID("Elastik Yüzey Ayrımı", NaturalMotion.FRACTURE, .58f, .46f, .40f),
    BLACK_HOLE_PULL("Gece Işığı", NaturalMotion.VORTEX, .16f, .20f, .20f),
    MATRIX_DISSOLVE("Tanecik Çözünmesi", NaturalMotion.PARTICLES, .26f, .02f, .36f),
    HYDRO_WIPE("Basınç Dalgası", NaturalMotion.WATER, .12f, .50f, .18f),
    DEWDROP_GLASS("Cam Üzerinde Çiy", NaturalMotion.DROPLET, .46f, .96f, .38f),
    PRISM_SHATTER("Prizmatik Gerilim", NaturalMotion.FRACTURE, .72f, .28f, .58f),
    ICON_ONLY("Sade Cam Simge", NaturalMotion.MINIMAL, .50f, .80f, .62f),
    NONE("Kapalı", NaturalMotion.NONE, 1f, 0f, 1f),
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
