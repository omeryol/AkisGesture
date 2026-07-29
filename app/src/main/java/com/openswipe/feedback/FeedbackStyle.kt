package com.omer.akisgesture.feedback

enum class FeedbackAnimation(val label: String) {
    FLUID("Akış"),
    BUBBLE("Baloncuk"),
    TEARDROP("Damla"),
    ICON_ONLY("Sade simge"),
    NONE("Kapalı"),
}

enum class FeedbackIcon(val label: String, val symbol: String) {
    CHEVRON("Yön oku", "›"),
    ARROW_LEFT("Tekli sol", "<"),
    ARROW_RIGHT("Tekli sağ", ">"),
    DOUBLE_ARROW_LEFT("Çift sol", "<<"),
    DOUBLE_ARROW_RIGHT("Çift sağ", ">>"),
    HOME("Ana ekran", "⌂"),
    RECENTS("Kartlar", "□"),
    STAR("Yıldız", "★"),
    DOT("Nokta", "●"),
    CLOSE("Çarpı", "×"),
    NONE("Simge yok", ""),
}
