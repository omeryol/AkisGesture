package io.github.omeryol.akisgesture.model

import io.github.omeryol.akisgesture.R

/**
 * Modular Action Icon Pack definitions for Akış Gesture.
 * Allows users to customize the visual symbol style used across gestures,
 * interactive phone maps, rule lists, and feedback notifications.
 */
enum class ActionIconPack(
    val id: String,
    val titleResId: Int,
    val descriptionResId: Int,
    val samplePreview: String,
) {
    SYSTEM_DEFAULT(
        id = "system_default",
        titleResId = R.string.icon_pack_system_default,
        descriptionResId = R.string.icon_pack_system_default_desc,
        samplePreview = "‹ ⌂ ▢ ⇆ ⚿ ⎘ 🕭 ⚙︎ ⏻",
    ),
    EMOJI_MODERN(
        id = "emoji_modern",
        titleResId = R.string.icon_pack_emoji_modern,
        descriptionResId = R.string.icon_pack_emoji_modern_desc,
        samplePreview = "🔙 🏠 📑 🔀 🔒 📸 🔔 ⚙️ ⚡",
    ),
    MINIMAL_LINE(
        id = "minimal_line",
        titleResId = R.string.icon_pack_minimal_line,
        descriptionResId = R.string.icon_pack_minimal_line_desc,
        samplePreview = "◀ ⌂ ☰ ⇄ ⚮ ⎚ 🕭 ⚙ ⏻",
    ),
    NEON_CYBER(
        id = "neon_cyber",
        titleResId = R.string.icon_pack_neon_cyber,
        descriptionResId = R.string.icon_pack_neon_cyber_desc,
        samplePreview = "◁ ⬡ ▤ ⇆ 🔐 ⟁ 🛰️ 🎛️ 💥",
    ),
    TECH_SYMBOL(
        id = "tech_symbol",
        titleResId = R.string.icon_pack_tech_symbol,
        descriptionResId = R.string.icon_pack_tech_symbol_desc,
        samplePreview = "◄ ◈ ☲ ⇄ ⬣ ◨ ⌬ ✇ ⏚",
    ),
    RETRO_CLASSIC(
        id = "retro_classic",
        titleResId = R.string.icon_pack_retro_classic,
        descriptionResId = R.string.icon_pack_retro_classic_desc,
        samplePreview = "👈 🏠 📋 🔁 🔑 📷 📢 🛠️ 🔋",
    );

    fun getSymbol(action: ActionNode): String = when (this) {
        SYSTEM_DEFAULT -> when (action) {
            is ActionNode.Back -> "‹"
            is ActionNode.Home -> "⌂"
            is ActionNode.Recents -> "▢"
            is ActionNode.SwitchLastApp -> "⇆"
            is ActionNode.SwitchNextApp -> "›"
            is ActionNode.LockScreen -> "⚿"
            is ActionNode.Screenshot -> "⎘"
            is ActionNode.NotificationPanel -> "🕭"
            is ActionNode.QuickSettings -> "⚙︎"
            is ActionNode.PowerMenu -> "⏻"
            is ActionNode.MediaPlayPause -> "►"
            is ActionNode.Assistant -> "✦"
            is ActionNode.ToggleFlashlight -> "☀︎"
            is ActionNode.ForceStopForeground -> "✕"
            is ActionNode.LaunchApp -> "◽"
            is ActionNode.SplitScreen -> "◫"
            is ActionNode.Menu -> "≡"
            else -> "⚙︎"
        }
        EMOJI_MODERN -> when (action) {
            is ActionNode.Back -> "🔙"
            is ActionNode.Home -> "🏠"
            is ActionNode.Recents -> "📑"
            is ActionNode.SwitchLastApp -> "🔀"
            is ActionNode.SwitchNextApp -> "⏩"
            is ActionNode.LockScreen -> "🔒"
            is ActionNode.Screenshot -> "📸"
            is ActionNode.NotificationPanel -> "🔔"
            is ActionNode.QuickSettings -> "⚙️"
            is ActionNode.PowerMenu -> "⚡"
            is ActionNode.MediaPlayPause -> "▶️"
            is ActionNode.Assistant -> "🤖"
            is ActionNode.ToggleFlashlight -> "🔦"
            is ActionNode.ForceStopForeground -> "🛑"
            is ActionNode.LaunchApp -> "📱"
            is ActionNode.SplitScreen -> "🔲"
            is ActionNode.Menu -> "📋"
            else -> "⚡"
        }
        MINIMAL_LINE -> when (action) {
            is ActionNode.Back -> "◀"
            is ActionNode.Home -> "⌂"
            is ActionNode.Recents -> "☰"
            is ActionNode.SwitchLastApp -> "⇄"
            is ActionNode.SwitchNextApp -> "➔"
            is ActionNode.LockScreen -> "⚮"
            is ActionNode.Screenshot -> "⎚"
            is ActionNode.NotificationPanel -> "🕭"
            is ActionNode.QuickSettings -> "⚙"
            is ActionNode.PowerMenu -> "⏻"
            is ActionNode.MediaPlayPause -> "▻"
            is ActionNode.Assistant -> "✧"
            is ActionNode.ToggleFlashlight -> "☼"
            is ActionNode.ForceStopForeground -> "✕"
            is ActionNode.LaunchApp -> "⬚"
            is ActionNode.SplitScreen -> "◫"
            is ActionNode.Menu -> "≡"
            else -> "⚙"
        }
        NEON_CYBER -> when (action) {
            is ActionNode.Back -> "◁"
            is ActionNode.Home -> "⬡"
            is ActionNode.Recents -> "▤"
            is ActionNode.SwitchLastApp -> "⇆"
            is ActionNode.SwitchNextApp -> "⇥"
            is ActionNode.LockScreen -> "🔐"
            is ActionNode.Screenshot -> "⟁"
            is ActionNode.NotificationPanel -> "🛰️"
            is ActionNode.QuickSettings -> "🎛️"
            is ActionNode.PowerMenu -> "💥"
            is ActionNode.MediaPlayPause -> "⏯️"
            is ActionNode.Assistant -> "🛸"
            is ActionNode.ToggleFlashlight -> "⚡"
            is ActionNode.ForceStopForeground -> "🚫"
            is ActionNode.LaunchApp -> "🔮"
            is ActionNode.SplitScreen -> "❖"
            is ActionNode.Menu -> "☰"
            else -> "💥"
        }
        TECH_SYMBOL -> when (action) {
            is ActionNode.Back -> "◄"
            is ActionNode.Home -> "◈"
            is ActionNode.Recents -> "☲"
            is ActionNode.SwitchLastApp -> "⇄"
            is ActionNode.SwitchNextApp -> "►"
            is ActionNode.LockScreen -> "⬣"
            is ActionNode.Screenshot -> "◨"
            is ActionNode.NotificationPanel -> "⌬"
            is ActionNode.QuickSettings -> "✇"
            is ActionNode.PowerMenu -> "⏚"
            is ActionNode.MediaPlayPause -> "▶"
            is ActionNode.Assistant -> "◇"
            is ActionNode.ToggleFlashlight -> "☀"
            is ActionNode.ForceStopForeground -> "■"
            is ActionNode.LaunchApp -> "◆"
            is ActionNode.SplitScreen -> "◧"
            is ActionNode.Menu -> "☰"
            else -> "✇"
        }
        RETRO_CLASSIC -> when (action) {
            is ActionNode.Back -> "👈"
            is ActionNode.Home -> "🏠"
            is ActionNode.Recents -> "📋"
            is ActionNode.SwitchLastApp -> "🔁"
            is ActionNode.SwitchNextApp -> "👉"
            is ActionNode.LockScreen -> "🔑"
            is ActionNode.Screenshot -> "📷"
            is ActionNode.NotificationPanel -> "📢"
            is ActionNode.QuickSettings -> "🛠️"
            is ActionNode.PowerMenu -> "🔋"
            is ActionNode.MediaPlayPause -> "📻"
            is ActionNode.Assistant -> "👾"
            is ActionNode.ToggleFlashlight -> "🔦"
            is ActionNode.ForceStopForeground -> "⏹️"
            is ActionNode.LaunchApp -> "📦"
            is ActionNode.SplitScreen -> "🪟"
            is ActionNode.Menu -> "📑"
            else -> "🎮"
        }
    }

    companion object {
        fun fromId(id: String?): ActionIconPack = entries.firstOrNull { it.id == id } ?: EMOJI_MODERN
    }
}
