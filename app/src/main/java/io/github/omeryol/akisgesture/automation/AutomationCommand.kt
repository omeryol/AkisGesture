package io.github.omeryol.akisgesture.automation

/**
 * Dış otomasyon araçlarından (MacroDroid, Tasker ve Locale uyumlu diğer
 * uygulamalar) gelen komutların tek yetkili çözümleyicisidir.
 *
 * Kurallar bilinçli olarak saf (pure) tutulur: cihaz gerekmeden birim testleriyle
 * doğrulanabilir. Yalnızca tam eşleşen değerler kabul edilir; bilinmeyen,
 * büyük/küçük harf farklı, boşluk veya kontrol karakteri içeren hiçbir değer
 * komuta dönüştürülmez.
 */
object AutomationCommand {

    enum class Command { START, STOP, TOGGLE }

    const val ACTION_START = "io.github.omeryol.akisgesture.action.START"
    const val ACTION_STOP = "io.github.omeryol.akisgesture.action.STOP"
    const val ACTION_TOGGLE = "io.github.omeryol.akisgesture.action.TOGGLE"

    /**
     * Eski OpenSwipe sürümlerinden kalan uyumluluk adları. Yalnızca okunur ve
     * aynı otomasyon anahtarına tabidir; yeni kurulumlarda kullanılmaz.
     */
    const val LEGACY_ACTION_START = "com.openswipe.action.START"
    const val LEGACY_ACTION_STOP = "com.openswipe.action.STOP"
    const val LEGACY_ACTION_TOGGLE = "com.openswipe.action.TOGGLE"

    private val ACTION_TABLE: Map<String, Command> = mapOf(
        ACTION_START to Command.START,
        ACTION_STOP to Command.STOP,
        ACTION_TOGGLE to Command.TOGGLE,
        LEGACY_ACTION_START to Command.START,
        LEGACY_ACTION_STOP to Command.STOP,
        LEGACY_ACTION_TOGGLE to Command.TOGGLE,
    )

    private val PLUGIN_TABLE: Map<String, Command> = mapOf(
        "start" to Command.START,
        "stop" to Command.STOP,
        "toggle" to Command.TOGGLE,
    )

    /** Manifestte ilan edilen tüm komut action adları. */
    val declaredActions: Set<String>
        get() = ACTION_TABLE.keys

    /** Gelen intent action adını komuta çevirir; yalnızca tam eşleşme geçerlidir. */
    fun fromAction(action: String?): Command? = action?.let { ACTION_TABLE[it] }

    /** Eklenti paketindeki komut metnini komuta çevirir; yalnızca tam eşleşme geçerlidir. */
    fun fromPluginValue(value: String?): Command? = value?.let { PLUGIN_TABLE[it] }

    /** Komutun eklenti paketinde saklanan kanonik biçimi. */
    fun pluginValueOf(command: Command): String = when (command) {
        Command.START -> "start"
        Command.STOP -> "stop"
        Command.TOGGLE -> "toggle"
    }

    /** Komutun dışarıya ilan edilen action adı. */
    fun actionOf(command: Command): String = when (command) {
        Command.START -> ACTION_START
        Command.STOP -> ACTION_STOP
        Command.TOGGLE -> ACTION_TOGGLE
    }

    /**
     * Komutun hedef durumunu belirler.
     * @param currentlyEnabled hizmetin o anki durumu (TOGGLE için anlamlıdır)
     */
    fun targetState(command: Command, currentlyEnabled: Boolean): Boolean = when (command) {
        Command.START -> true
        Command.STOP -> false
        Command.TOGGLE -> !currentlyEnabled
    }
}
