package com.omer.akisgesture.feedback

enum class FeedbackAnimation(val label: String) {
    WATER("Su (Akıcı Nehir) 🌊"),
    FIRE("Ateş (Alev & Kıvılcım) 🔥"),
    STEAM("Buhar (Sıcak Sis) 💨"),
    CLOUD("Bulut (Kümülüs) ☁️"),
    LIGHTNING("Şimşek (Elektrik Arki) ⚡"),
    WIND("Rüzgar (Hava Akımı) 🍃"),
    RAIN("Yağmur (Su Serpintisi) 🌧️"),
    SUN("Güneş (Işıltılı Korona) ☀️"),
    FLUID("Akış 🌊"),
    NEON_PULSE("Neon ✨"),
    CYBER_HEX("Altıgen 💎"),
    ORB_GLOW("Küre 🔮"),
    TEARDROP("Damla 💧"),
    BUBBLE("Baloncuk 🫧"),
    MINIMAL_PADDLE("Kapsül 💊"),
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
