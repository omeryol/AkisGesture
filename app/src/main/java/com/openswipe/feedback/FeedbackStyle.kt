package com.omer.akisgesture.feedback

enum class FeedbackAnimation(val label: String) {
    OCEAN_LIQUID("Okyanus Sıvı & Metaball Fiziği 🌊"),
    PLASMA_FIRE("Plazma Ateş & Volkanik Kıvılcım 🔥"),
    ELECTRIC_STORM("Elektrik Fırtınası & Şimşek ⚡"),
    ATMOSPHERIC_MIST("Atmosferik Sis & Buhar 💨"),
    SOLAR_CORONA("Güneş Koronası & Yıldız Işını ☀️"),
    FLUID("Okyanus Sıvı 🌊"),
    FIRE("Plazma Ateş 🔥"),
    WATER("Okyanus Sıvı 🌊"),
    LIGHTNING("Elektrik Fırtınası ⚡"),
    STEAM("Atmosferik Sis 💨"),
    SUN("Güneş Koronası ☀️"),
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
