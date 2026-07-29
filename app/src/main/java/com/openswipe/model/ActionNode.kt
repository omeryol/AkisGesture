package com.omer.akisgesture.model

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

    // ═══ Medya控制 ═══
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

    // ═══ Uygula启动 ═══
    data class LaunchApp(
        val packageName: String,
        val appName: String
    ) : ActionNode {
        override val id = "launch_app:$packageName"
        override val label = appName
        override val minApi = 16
    }

    // ═══ 空操作 ═══
    data object NoAction : ActionNode {
        override val id = "no_action"
        override val label = "İşlem yok"
        override val minApi = 16
    }

    companion object {
        val allFixed: List<ActionNode> by lazy {
            listOf(
                Back, Home, Recents, SwitchLastApp, SwitchNextApp,
                LockScreen, Screenshot, SplitScreen, PowerMenu,
                NotificationPanel, QuickSettings, InputMethodPicker, VolumePanel, Assistant,
                MediaPlayPause, MediaNext, MediaPrevious, VolumeUp, VolumeDown, ToggleMute,
                ToggleFlashlight,
                ForceStopForeground,
                NoAction,
            )
        }

        /** Keep the function overload for source compatibility. */
        fun allFixed(): List<ActionNode> = allFixed

        private val fixedById: Map<String, ActionNode> by lazy {
            allFixed.associateBy { it.id }
        }

        fun fromId(id: String): ActionNode? {
            if (id.startsWith("launch_app:")) {
                val pkg = id.removePrefix("launch_app:")
                return LaunchApp(pkg, pkg)
            }
            return fixedById[id]
        }
    }
}
