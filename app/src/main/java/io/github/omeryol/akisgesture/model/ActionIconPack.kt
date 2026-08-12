package io.github.omeryol.akisgesture.model

import io.github.omeryol.akisgesture.R

enum class ActionIconPack(
    val id: String,
    val titleResId: Int,
    val descriptionResId: Int,
    val assetPrefix: String,
) {
    PHOSPHOR("phosphor", R.string.icon_pack_phosphor, R.string.icon_pack_phosphor_desc, "phosphor"),
    TABLER("tabler", R.string.icon_pack_tabler, R.string.icon_pack_tabler_desc, "tabler"),
    ICONOIR("iconoir", R.string.icon_pack_iconoir, R.string.icon_pack_iconoir_desc, "iconoir"),
    HEROICONS("heroicons", R.string.icon_pack_heroicons, R.string.icon_pack_heroicons_desc, "heroicons"),
    BOOTSTRAP("bootstrap", R.string.icon_pack_bootstrap, R.string.icon_pack_bootstrap_desc, "bootstrap"),
    EVA("eva", R.string.icon_pack_eva, R.string.icon_pack_eva_desc, "eva");

    companion object {
        private val legacyAliases = mapOf(
            "system_default" to PHOSPHOR,
            "emoji_modern" to PHOSPHOR,
            "minimal_line" to ICONOIR,
            "neon_cyber" to TABLER,
            "tech_symbol" to TABLER,
            "retro_classic" to EVA,
        )

        fun fromId(id: String?): ActionIconPack =
            entries.firstOrNull { it.id == id } ?: legacyAliases[id] ?: PHOSPHOR

        fun migrationTarget(id: String): ActionIconPack? = legacyAliases[id]

        val legacyIds: Set<String> get() = legacyAliases.keys
    }
}
