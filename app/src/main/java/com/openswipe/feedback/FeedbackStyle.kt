package com.omer.akisgesture.feedback

enum class FeedbackAnimation(val label: String) {
    OCEAN_WAVE("Okyanus Dalgası 🌊"),
    MERCURY_TEARDROP("Kopan Sıvı Damlası 💧"),
    PLASMA_FIRE("Plazma Ateş & Kıvılcım 🔥"),
    ATMOSPHERIC_MIST("Atmosferik Sis & Buhar 💨"),
    ELECTRIC_STORM("Elektrik Fırtınası & Şimşek ⚡"),
    SOLAR_CORONA("Güneş Koronası & Işınlar ☀️"),

    // Compatibility mappings
    OCEAN_LIQUID("Okyanus Dalgası 🌊"),
    WATER("Okyanus Dalgası 🌊"),
    FLUID("Okyanus Dalgası 🌊"),
    FIRE("Plazma Ateş 🔥"),
    LIGHTNING("Elektrik Fırtınası ⚡"),
    STEAM("Atmosferik Sis 💨"),
    SUN("Güneş Koronası ☀️"),
    TEARDROP("Kopan Sıvı Damlası 💧"),
    BUBBLE("Kopan Sıvı Damlası 💧"),
    NEON_PULSE("Elektrik Fırtınası ⚡"),
    CYBER_HEX("Elektrik Fırtınası ⚡"),
    ORB_GLOW("Güneş Koronası ☀️"),
    MINIMAL_PADDLE("Okyanus Dalgası 🌊"),

    ICON_ONLY("Sade Simge 🎯"),
    NONE("Kapalı 🚫"),
}

enum class FeedbackIcon(val label: String, val symbol: String) {
    CHEVRON("Yön oku", "›"),
    ARROW_LEFT("Tekli sol", "‹"),
    ARROW_RIGHT("Tekli sağ", "›"),
    DOUBLE_ARROW_LEFT("Çift sol", "«"),
    DOUBLE_ARROW_RIGHT("Çift sağ", "»"),
    HOME("Ana ekran", "⌂"),
    RECENTS("Kartlar", "⊞"),
    STAR("Yıldız", "★"),
    DOT("Nokta", "●"),
    CLOSE("Kapat", "✕"),
    NONE("Simge yok", ""),
}
