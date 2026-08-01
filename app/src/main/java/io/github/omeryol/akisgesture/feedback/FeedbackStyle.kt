package io.github.omeryol.akisgesture.feedback

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
    CHEVRON("Yön Oku 🏹", "🏹"),
    ARROW_LEFT("Tekli Sol ◀", "◀"),
    ARROW_RIGHT("Tekli Sağ ▶", "▶"),
    HOME("Ana Ekran 🏠", "🏠"),
    RECENTS("Son Uygulamalar 📱", "📱"),
    LOCK("Kilit 🔒", "🔒"),
    CAMERA("Kamera 📸", "📸"),
    FLASHLIGHT("Fener 🔦", "🔦"),
    STAR("Yıldız ⭐", "⭐"),
    HEART("Kalp ❤️", "❤️"),
    FIRE("Ateş 🔥", "🔥"),
    ROCKET("Roket 🚀", "🚀"),
    CLOSE("Kapat ❌", "❌"),
    NONE("Simge yok", ""),
}
