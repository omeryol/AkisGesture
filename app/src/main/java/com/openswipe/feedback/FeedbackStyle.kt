package com.omer.akisgesture.feedback

enum class FeedbackAnimation(val label: String) {
    OCEAN_WAVE("Okyanus Dalgası 🌊"),
    MERCURY_TEARDROP("Kopan Sıvı Damlası 💧"),
    PLASMA_FIRE("Plazma Ateş & Kıvılcım 🔥"),
    ATMOSPHERIC_MIST("Atmosferik Sis & Buhar 💨"),
    ELECTRIC_STORM("Elektrik Fırtınası & Şimşek ⚡"),
    SOLAR_CORONA("Güneş Koronası & Işınlar ☀️"),
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
