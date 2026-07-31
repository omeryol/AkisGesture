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
    CHEVRON("Yön oku 🏹", "🏹"),
    ARROW_LEFT("Geri oku ◀", "◀"),
    ARROW_RIGHT("İleri oku ▶", "▶"),
    HOME("Ana ekran 🏠", "🏠"),
    RECENTS("Son uygulamalar 📱", "📱"),
    CAMERA("Kamera 📸", "📸"),
    FLASHLIGHT("Fener 🔦", "🔦"),
    STAR("Yıldız ⭐", "⭐"),
    HEART("Kalp ❤️", "❤️"),
    FIRE("Ateş 🔥", "🔥"),
    ROCKET("Roket 🚀", "🚀"),
    CLOSE("Kapat ❌", "❌"),
    NONE("Simge yok", ""),
}
