package io.github.omeryol.akisgesture.model

sealed interface ActionNode {
    val id: String
    val label: String
    val minApi: Int

    // ═══ Gezinme ═══
    data object Back : ActionNode {
        override val id = "back"
        override val label = "Geri"
        override val minApi = 16
    }
    data object Home : ActionNode {
        override val id = "home"
        override val label = "Ana ekran"
        override val minApi = 16
    }
    data object Recents : ActionNode {
        override val id = "recents"
        override val label = "Son uygulamalar"
        override val minApi = 16
    }
    data object SwitchLastApp : ActionNode {
        override val id = "switch_last_app"
        override val label = "Önceki uygulama"
        override val minApi = 16
    }

    // ═══ Sistem ═══
    data object LockScreen : ActionNode {
        override val id = "lock_screen"
        override val label = "Ekranı kilitle"
        override val minApi = 28
    }
    data object Screenshot : ActionNode {
        override val id = "screenshot"
        override val label = "Ekran görüntüsü"
        override val minApi = 28
    }
    data object SplitScreen : ActionNode {
        override val id = "split_screen"
        override val label = "Bölünmüş ekran"
        override val minApi = 24
    }
    data object PowerMenu : ActionNode {
        override val id = "power_menu"
        override val label = "Güç menüsü"
        override val minApi = 21
    }
    data object Menu : ActionNode {
        override val id = "menu"
        override val label = "Menü"
        override val minApi = 16
    }

    // ═══ Paneller ═══
    data object NotificationPanel : ActionNode {
        override val id = "notification_panel"
        override val label = "Bildirimler"
        override val minApi = 16
    }
    data object QuickSettings : ActionNode {
        override val id = "quick_settings"
        override val label = "Hızlı ayarlar"
        override val minApi = 17
    }
    data object SwitchNextApp : ActionNode {
        override val id = "switch_next_app"
        override val label = "Sonraki uygulama"
        override val minApi = 16
    }
    data object InputMethodPicker : ActionNode {
        override val id = "input_method_picker"
        override val label = "Klavye seçici"
        override val minApi = 26
    }
    data object VolumePanel : ActionNode {
        override val id = "volume_panel"
        override val label = "Ses paneli"
        override val minApi = 26
    }
    data object Assistant : ActionNode {
        override val id = "assistant"
        override val label = "Sistem asistanı"
        override val minApi = 26
    }

    // ═══ Döndürme ═══
    data object ToggleAutoRotate : ActionNode {
        override val id = "toggle_auto_rotate"
        override val label = "Otomatik döndürmeyi aç/kapat"
        override val minApi = 16
    }
    data object ForcePortrait : ActionNode {
        override val id = "force_portrait"
        override val label = "Dikey yön"
        override val minApi = 16
    }
    data object ForceLandscape : ActionNode {
        override val id = "force_landscape"
        override val label = "Yatay yön"
        override val minApi = 16
    }
    data object XiaomiOneHandMode : ActionNode {
        override val id = "xiaomi_one_hand"
        override val label = "Tek el modu"
        override val minApi = 21
    }

    // ═══ Medya Kontrolü ═══
    data object MediaPlayPause : ActionNode {
        override val id = "media_play_pause"
        override val label = "Oynat / Duraklat"
        override val minApi = 16
    }
    data object MediaNext : ActionNode {
        override val id = "media_next"
        override val label = "Sonraki parça"
        override val minApi = 16
    }
    data object MediaPrevious : ActionNode {
        override val id = "media_previous"
        override val label = "Önceki parça"
        override val minApi = 16
    }
    data object VolumeUp : ActionNode {
        override val id = "volume_up"
        override val label = "Sesi artır"
        override val minApi = 16
    }
    data object VolumeDown : ActionNode {
        override val id = "volume_down"
        override val label = "Sesi azalt"
        override val minApi = 16
    }
    data object ToggleMute : ActionNode {
        override val id = "toggle_mute"
        override val label = "Sesi kapat / aç"
        override val minApi = 23
    }

    // ═══ Arama ve kısayol ═══
    data object VoiceSearch : ActionNode {
        override val id = "voice_search"
        override val label = "Sesli arama"
        override val minApi = 16
    }
    data object VoiceAssistant : ActionNode {
        override val id = "voice_assistant"
        override val label = "Sesli asistan"
        override val minApi = 21
    }
    data class AppShortcut(
        val packageName: String,
        val shortcutId: String,
        val shortcutLabel: String,
    ) : ActionNode {
        override val id = "app_shortcut:$packageName:$shortcutId"
        override val label get() = shortcutLabel
        override val minApi = 25
    }
    data class SendKeyCode(val keyCode: Int, val keyLabel: String) : ActionNode {
        override val id = "keycode:$keyCode"
        override val label = "Tuş: $keyLabel"
        override val minApi = 16
    }

    // ═══ Sistem arayüzü ═══
    data object ToggleNavBar : ActionNode {
        override val id = "toggle_nav_bar"
        override val label = "Gezinme çubuğunu göster/gizle"
        override val minApi = 21
    }

    // ═══ Donanım ═══
    data object ToggleFlashlight : ActionNode {
        override val id = "toggle_flashlight"
        override val label = "Feneri aç/kapat"
        override val minApi = 23
    }

    data object ForceStopForeground : ActionNode {
        override val id = "force_stop_foreground"
        override val label = "Öndeki uygulamayı kapat"
        override val minApi = 26
    }

    // ═══ Uygulama Başlatma ═══
    data class LaunchApp(
        val packageName: String,
        val appName: String
    ) : ActionNode {
        override val id = "launch_app:$packageName"
        override val label = appName
        override val minApi = 16
    }

    // ═══ Parlaklık ═══
    data object BrightnessUp : ActionNode {
        override val id = "brightness_up"
        override val label = "Parlaklık artır"
        override val minApi = 16
    }
    data object BrightnessDown : ActionNode {
        override val id = "brightness_down"
        override val label = "Parlaklık azalt"
        override val minApi = 16
    }

    data object NoAction : ActionNode {
        override val id = "no_action"
        override val label = "İşlem yok"
        override val minApi = 16
    }

    companion object {
        val allFixed: List<ActionNode> by lazy {
            listOf(
                Back, Home, Recents, SwitchLastApp, SwitchNextApp,
                LockScreen, Screenshot, SplitScreen, PowerMenu, Menu,
                NotificationPanel, QuickSettings, InputMethodPicker, VolumePanel, Assistant,
                ToggleAutoRotate, ForcePortrait, ForceLandscape, XiaomiOneHandMode,
                MediaPlayPause, MediaNext, MediaPrevious, VolumeUp, VolumeDown, ToggleMute,
                BrightnessUp, BrightnessDown,
                VoiceSearch, VoiceAssistant,
                ToggleFlashlight,
                ForceStopForeground,
                NoAction,
            )
        }

        /** Keep the function overload for source compatibility. */
        fun allFixed(): List<ActionNode> = allFixed

        private val fixedById: Map<String, ActionNode> by lazy {
            (allFixed + ToggleNavBar).associateBy { it.id }
        }

        fun fromId(id: String): ActionNode? {
            if (id.startsWith("launch_app:")) {
                val pkg = id.removePrefix("launch_app:")
                return LaunchApp(pkg, pkg)
            }
            if (id.startsWith("app_shortcut:")) {
                val parts = id.removePrefix("app_shortcut:").split(":", limit = 2)
                if (parts.size == 2) {
                    return AppShortcut(parts[0], parts[1], parts[1])
                }
                return null
            }
            if (id.startsWith("keycode:")) {
                val code = id.removePrefix("keycode:").toIntOrNull() ?: return null
                return SendKeyCode(code, code.toString())
            }
            return fixedById[id]
        }
    }
}

fun ActionNode.toSymbol(pack: ActionIconPack = ActionIconPack.EMOJI_MODERN): String = pack.getSymbol(this)
