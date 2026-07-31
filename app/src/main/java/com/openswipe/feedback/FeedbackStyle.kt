package com.omer.akisgesture.feedback

enum class FeedbackAnimation(val label: String) {
    OCEAN_WAVE("Okyanus Dalgası 🌊"),
    MERCURY_TEARDROP("Kopan Damla 💧"),
    PLASMA_FIRE("Plazma Ateş 🔥"),
    ATMOSPHERIC_MIST("Atmosferik Sis 💨"),
    ELECTRIC_STORM("Şimşek Fırtınası ⚡"),
    SOLAR_CORONA("Güneş Koronası ☀️"),
    ICON_ONLY("Sade Simge 🎯"),
    NONE("Kapalı 🚫"),
}

enum class FeedbackIcon(val label: String, val symbol: String) {
    CHEVRON("Daire Hedef 🎯", "🎯"),
    ARROW_LEFT("Sol Oku ◀", "◀"),
    ARROW_RIGHT("Sağ Oku ▶", "▶"),
    HOME("Daire Düğme 🔘", "🔘"),
    RECENTS("Daire Beyaz ⚪", "⚪"),
    ORB("Nazar Küre 🧿", "🧿"),
    STAR("Daire Yıldız 🌟", "🌟"),
    RED_ORB("Kırmızı Küre 🔴", "🔴"),
    BLUE_ORB("Mavi Küre 🔵", "🔵"),
    GREEN_ORB("Yeşil Küre 🟢", "🟢"),
    PURPLE_ORB("Mor Küre 🟣", "🟣"),
    CLOSE("Daire Kapat ⭕", "⭕"),
    NONE("Simge yok", ""),
}
