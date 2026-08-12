package io.github.omeryol.akisgesture.model

enum class ActionIconColorMode(val id: String) {
    MONOCHROME("monochrome"),
    THEME("theme"),
    FUNCTIONAL("functional");

    companion object {
        fun fromId(id: String?): ActionIconColorMode =
            entries.firstOrNull { it.id == id } ?: FUNCTIONAL
    }
}
