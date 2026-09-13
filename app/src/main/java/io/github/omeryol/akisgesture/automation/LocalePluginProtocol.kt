package io.github.omeryol.akisgesture.automation

/**
 * Locale/Tasker eklenti protokolü sabitleri. MacroDroid de bu protokolü kullanır.
 *
 * Dış otomasyon yalnızca hizmeti başlatabilir, durdurabilir veya durumunu
 * değiştirebilir; paket içinde başka hiçbir anahtar kabul edilmez.
 */
object LocalePluginProtocol {

    const val ACTION_EDIT_SETTING = "com.twofortyfouram.locale.intent.action.EDIT_SETTING"
    const val ACTION_FIRE_SETTING = "com.twofortyfouram.locale.intent.action.FIRE_SETTING"

    const val EXTRA_BUNDLE = "com.twofortyfouram.locale.intent.extra.BUNDLE"
    const val EXTRA_BLURB = "com.twofortyfouram.locale.intent.extra.BLURB"
    const val EXTRA_SETTING = "com.twofortyfouram.locale.intent.extra.SETTING"

    /** Eklentinin kendi komut verisini taşıdığı anahtar. */
    const val KEY_COMMAND = "akis_command"

    /** Şişirilmiş veya şüpheli eklenti paketlerini reddetmek için üst sınır. */
    const val MAX_BUNDLE_KEYS = 4

    /** Eklenti paketinde yalnızca bilinen anahtarlar bulunabilir. */
    fun isAcceptedBundle(keys: Set<String>): Boolean =
        keys.isNotEmpty() && keys.size <= MAX_BUNDLE_KEYS && keys.all { it == KEY_COMMAND }

    /** Anahtar kümesini (null güvenli) kabul edilebilir paket listesine indirger. */
    fun acceptedKeysOf(vararg keys: String?): Set<String> = keys.filterNotNull().toSet()
}
