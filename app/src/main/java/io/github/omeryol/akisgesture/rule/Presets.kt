package io.github.omeryol.akisgesture.rule

import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.GestureRule
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.model.SectionRange
import io.github.omeryol.akisgesture.model.TriggerNode
import io.github.omeryol.akisgesture.overlay.Edge

object Presets {
    data class Template(
        val name: String,
        val description: String,
        val graph: GestureRuleGraph,
    )

    private fun rule(
        id: String,
        edge: Edge,
        section: SectionRange,
        gesture: GestureType,
        action: ActionNode,
    ) = GestureRule(id, TriggerNode(edge, section, gesture), action)

    val SIMPLE = GestureRuleGraph(
        listOf(rule("simple_home", Edge.BOTTOM, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Home)),
    )

    val ANDROID_CLASSIC = GestureRuleGraph(
        listOf(
            rule("classic_back", Edge.BOTTOM, SectionRange.thirds(0), GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("classic_home", Edge.BOTTOM, SectionRange.thirds(1), GestureType.QUICK_SWIPE, ActionNode.Home),
            rule("classic_recents", Edge.BOTTOM, SectionRange.thirds(2), GestureType.QUICK_SWIPE, ActionNode.Recents),
        ),
    )

    val IOS_STYLE = GestureRuleGraph(
        listOf(
            rule("ios_left", Edge.LEFT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("ios_right", Edge.RIGHT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("ios_bottom", Edge.BOTTOM, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Home),
        ),
    )

    val ONE_HAND_RIGHT = GestureRuleGraph(
        listOf(
            rule("right_upper_quick", Edge.RIGHT, SectionRange.halves(0), GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("right_upper_hold", Edge.RIGHT, SectionRange.halves(0), GestureType.SWIPE_HOLD, ActionNode.NotificationPanel),
            rule("right_lower_quick", Edge.RIGHT, SectionRange.halves(1), GestureType.QUICK_SWIPE, ActionNode.Home),
            rule("right_lower_hold", Edge.RIGHT, SectionRange.halves(1), GestureType.SWIPE_HOLD, ActionNode.Recents),
        ),
    )

    val ONE_HAND_LEFT = GestureRuleGraph(
        listOf(
            rule("left_upper_quick", Edge.LEFT, SectionRange.halves(0), GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("left_upper_hold", Edge.LEFT, SectionRange.halves(0), GestureType.SWIPE_HOLD, ActionNode.NotificationPanel),
            rule("left_lower_quick", Edge.LEFT, SectionRange.halves(1), GestureType.QUICK_SWIPE, ActionNode.Home),
            rule("left_lower_hold", Edge.LEFT, SectionRange.halves(1), GestureType.SWIPE_HOLD, ActionNode.Recents),
        ),
    )

    val DUAL_EDGE_ADVANCED = GestureRuleGraph(
        listOf(
            rule("dual_left_quick", Edge.LEFT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("dual_left_hold", Edge.LEFT, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.Recents),
            rule("dual_right_quick", Edge.RIGHT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("dual_right_hold", Edge.RIGHT, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.SwitchLastApp),
            rule("dual_bottom_quick", Edge.BOTTOM, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Home),
            rule("dual_bottom_hold", Edge.BOTTOM, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.Assistant),
        ),
    )

    val PRODUCTIVITY = GestureRuleGraph(
        listOf(
            rule("work_left_quick", Edge.BOTTOM, SectionRange.thirds(0), GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("work_left_hold", Edge.BOTTOM, SectionRange.thirds(0), GestureType.SWIPE_HOLD, ActionNode.NotificationPanel),
            rule("work_mid_quick", Edge.BOTTOM, SectionRange.thirds(1), GestureType.QUICK_SWIPE, ActionNode.Home),
            rule("work_mid_hold", Edge.BOTTOM, SectionRange.thirds(1), GestureType.SWIPE_HOLD, ActionNode.Assistant),
            rule("work_right_quick", Edge.BOTTOM, SectionRange.thirds(2), GestureType.QUICK_SWIPE, ActionNode.Recents),
            rule("work_right_hold", Edge.BOTTOM, SectionRange.thirds(2), GestureType.SWIPE_HOLD, ActionNode.QuickSettings),
        ),
    )

    val MEDIA_CONTROL = GestureRuleGraph(
        listOf(
            rule("media_previous", Edge.BOTTOM, SectionRange.thirds(0), GestureType.QUICK_SWIPE, ActionNode.MediaPrevious),
            rule("media_volume_down", Edge.BOTTOM, SectionRange.thirds(0), GestureType.SWIPE_HOLD, ActionNode.VolumeDown),
            rule("media_play", Edge.BOTTOM, SectionRange.thirds(1), GestureType.QUICK_SWIPE, ActionNode.MediaPlayPause),
            rule("media_mute", Edge.BOTTOM, SectionRange.thirds(1), GestureType.SWIPE_HOLD, ActionNode.ToggleMute),
            rule("media_next", Edge.BOTTOM, SectionRange.thirds(2), GestureType.QUICK_SWIPE, ActionNode.MediaNext),
            rule("media_volume_up", Edge.BOTTOM, SectionRange.thirds(2), GestureType.SWIPE_HOLD, ActionNode.VolumeUp),
        ),
    )

    val ROOT_POWER = GestureRuleGraph(
        listOf(
            rule("root_left_quick", Edge.LEFT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("root_left_hold", Edge.LEFT, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.ForceStopForeground),
            rule("root_right_quick", Edge.RIGHT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("root_right_hold", Edge.RIGHT, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.ForceStopForeground),
            rule("root_bottom_quick", Edge.BOTTOM, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Home),
            rule("root_bottom_hold", Edge.BOTTOM, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.Recents),
        ),
    )

    val NIGHT_MODE = GestureRuleGraph(
        listOf(
            rule("night_back", Edge.LEFT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("night_mute", Edge.RIGHT, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.ToggleMute),
            rule("night_home", Edge.BOTTOM, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Home),
        ),
    )

    val READER = GestureRuleGraph(
        listOf(
            rule("reader_back", Edge.LEFT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("reader_brightness", Edge.RIGHT, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.BrightnessDown),
            rule("reader_home", Edge.BOTTOM, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Home),
        ),
    )

    val TRAVEL = GestureRuleGraph(
        listOf(
            rule("travel_back", Edge.LEFT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("travel_panel", Edge.RIGHT, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.QuickSettings),
            rule("travel_recent", Edge.BOTTOM, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Recents),
        ),
    )

    val COMMUNICATION = GestureRuleGraph(
        listOf(
            rule("communication_back", Edge.LEFT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("communication_assistant", Edge.RIGHT, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.VoiceAssistant),
            rule("communication_notifications", Edge.BOTTOM, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.NotificationPanel),
        ),
    )

    val CREATOR = GestureRuleGraph(
        listOf(
            rule("creator_back", Edge.LEFT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("creator_capture", Edge.RIGHT, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.Screenshot),
            rule("creator_home", Edge.BOTTOM, SectionRange.thirds(0), GestureType.QUICK_SWIPE, ActionNode.Home),
            rule("creator_recent", Edge.BOTTOM, SectionRange.thirds(2), GestureType.QUICK_SWIPE, ActionNode.Recents),
        ),
    )

    val MINIMAL_EDGE = GestureRuleGraph(
        listOf(
            rule("minimal_back", Edge.LEFT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("minimal_home", Edge.BOTTOM, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Home),
        ),
    )

    val BROWSING = GestureRuleGraph(
        listOf(
            rule("browse_back", Edge.LEFT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("browse_previous", Edge.RIGHT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.SwitchLastApp),
            rule("browse_recents", Edge.BOTTOM, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.Recents),
        ),
    )

    val GAMING = GestureRuleGraph(
        listOf(
            rule("gaming_home", Edge.LEFT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Home),
            rule("gaming_panel", Edge.RIGHT, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.QuickSettings),
            rule("gaming_back", Edge.BOTTOM, SectionRange.thirds(0), GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("gaming_recents", Edge.BOTTOM, SectionRange.thirds(2), GestureType.QUICK_SWIPE, ActionNode.Recents),
        ),
    )

    val STUDY = GestureRuleGraph(
        listOf(
            rule("study_back", Edge.LEFT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("study_brightness", Edge.RIGHT, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.BrightnessUp),
            rule("study_assistant", Edge.BOTTOM, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.Assistant),
        ),
    )

    val POWER_SAVER = GestureRuleGraph(
        listOf(
            rule("power_back", Edge.LEFT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.Back),
            rule("power_mute", Edge.RIGHT, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.ToggleMute),
            rule("power_lock", Edge.BOTTOM, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.LockScreen),
        ),
    )

    val MEDIA_EDGE = GestureRuleGraph(
        listOf(
            rule("edge_previous", Edge.LEFT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.MediaPrevious),
            rule("edge_next", Edge.RIGHT, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.MediaNext),
            rule("edge_play", Edge.BOTTOM, SectionRange.ALL, GestureType.QUICK_SWIPE, ActionNode.MediaPlayPause),
            rule("edge_volume", Edge.BOTTOM, SectionRange.ALL, GestureType.SWIPE_HOLD, ActionNode.VolumeUp),
        ),
    )

    val DEFAULT = IOS_STYLE

    val TEMPLATES: List<Template> = listOf(
        Template("Başlangıç · Sade", "Yalnızca alt kenardan ana ekrana geçiş. En az müdahale.", SIMPLE),
        Template("Başlangıç · Android klasik", "Alt kenarın üç bölgesinde geri, ana ekran ve son uygulamalar.", ANDROID_CLASSIC),
        Template("Genel · Dengeli", "Sol ve sağdan geri, alttan ana ekran. Günlük kullanım için dengeli.", IOS_STYLE),
        Template("Tek el · Sağ kenar", "Sağ üst/alt bölgede geri ve ana ekran; bekletmede bildirim ve son uygulamalar.", ONE_HAND_RIGHT),
        Template("Tek el · Sol kenar", "Sol üst/alt bölgede geri ve ana ekran; bekletmede bildirim ve son uygulamalar.", ONE_HAND_LEFT),
        Template("İleri · Çift kenar", "İki kenarda geri/son uygulama, altta ana ekran/asistan kombinasyonu.", DUAL_EDGE_ADVANCED),
        Template("İleri · Üretkenlik", "Alt üçlüde geri, ana ekran, son uygulamalar; bekletmede paneller ve asistan.", PRODUCTIVITY),
        Template("Özel · Medya kontrolü", "Alt kenarın üç bölgesinde parça, oynatma ve ses kontrolleri.", MEDIA_CONTROL),
        Template("Root · Güçlü kullanım", "Bekletme hareketleriyle öndeki uygulamayı kapatma. Root gerekir.", ROOT_POWER),
        Template("Gece · Sessiz kullanım", "Geri, ana ekran ve bekletmeyle sessize alma. Gece için düşük dikkat dağıtma.", NIGHT_MODE),
        Template("Okuma · Göz konforu", "Geri, bekletmeyle parlaklık azaltma ve ana ekran.", READER),
        Template("Seyahat · Hızlı erişim", "Geri, hızlı ayarlar ve son uygulamalar tek elle erişilebilir.", TRAVEL),
        Template("İletişim · Bildirim odaklı", "Geri, sesli asistan ve bildirim paneli.", COMMUNICATION),
        Template("Üretici · Yakala ve yönet", "Ekran görüntüsü, ana ekran ve son uygulamalarla içerik üretimi.", CREATOR),
        Template("Minimal · İki hareket", "Sadece geri ve ana ekran. Temiz ve sade deneyim.", MINIMAL_EDGE),
        Template("Tarama · Uygulama geçişi", "Geri ve önceki uygulama; bekletmeyle son uygulamalar.", BROWSING),
        Template("Oyun · Ekranı bölme", "Ana ekran, hızlı ayarlar ve alt kenardan geri/son uygulamalar.", GAMING),
        Template("Ders · Odaklı ekran", "Geri, bekletmeyle parlaklık artırma ve asistan.", STUDY),
        Template("Pil · Sessiz güç", "Geri, sessize alma ve bekletmeyle ekran kilidi.", POWER_SAVER),
        Template("Medya · Kenar kumandası", "Sol/sağ parça değişimi; altta oynat ve bekletmeyle ses.", MEDIA_EDGE),
    )

    val ALL: List<Pair<String, GestureRuleGraph>> = TEMPLATES.map { it.name to it.graph }

    val DESCRIPTIONS: Map<String, String> = TEMPLATES.associate { it.name to it.description }
}
