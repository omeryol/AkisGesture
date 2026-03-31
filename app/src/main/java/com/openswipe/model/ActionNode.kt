package com.openswipe.model

sealed interface ActionNode {
    val id: String
    val label: String
    val minApi: Int

    // ═══ 系统导航 ═══
    data object Back : ActionNode {
        override val id = "back"
        override val label = "返回"
        override val minApi = 16
    }
    data object Home : ActionNode {
        override val id = "home"
        override val label = "主页"
        override val minApi = 16
    }
    data object Recents : ActionNode {
        override val id = "recents"
        override val label = "最近任务"
        override val minApi = 16
    }
    data object SwitchLastApp : ActionNode {
        override val id = "switch_last_app"
        override val label = "切换上个应用"
        override val minApi = 16
    }

    // ═══ 系统控制 ═══
    data object LockScreen : ActionNode {
        override val id = "lock_screen"
        override val label = "锁屏"
        override val minApi = 28
    }
    data object Screenshot : ActionNode {
        override val id = "screenshot"
        override val label = "截屏"
        override val minApi = 28
    }
    data object SplitScreen : ActionNode {
        override val id = "split_screen"
        override val label = "分屏"
        override val minApi = 24
    }
    data object PowerMenu : ActionNode {
        override val id = "power_menu"
        override val label = "电源菜单"
        override val minApi = 21
    }

    // ═══ 面板 ═══
    data object NotificationPanel : ActionNode {
        override val id = "notification_panel"
        override val label = "通知栏"
        override val minApi = 16
    }
    data object QuickSettings : ActionNode {
        override val id = "quick_settings"
        override val label = "快速设置"
        override val minApi = 17
    }

    // ═══ 媒体控制 ═══
    data object MediaPlayPause : ActionNode {
        override val id = "media_play_pause"
        override val label = "播放/暂停"
        override val minApi = 16
    }
    data object MediaNext : ActionNode {
        override val id = "media_next"
        override val label = "下一曲"
        override val minApi = 16
    }
    data object MediaPrevious : ActionNode {
        override val id = "media_previous"
        override val label = "上一曲"
        override val minApi = 16
    }
    data object VolumeUp : ActionNode {
        override val id = "volume_up"
        override val label = "音量+"
        override val minApi = 16
    }
    data object VolumeDown : ActionNode {
        override val id = "volume_down"
        override val label = "音量-"
        override val minApi = 16
    }

    // ═══ 硬件 ═══
    data object ToggleFlashlight : ActionNode {
        override val id = "toggle_flashlight"
        override val label = "开关闪光灯"
        override val minApi = 23
    }

    // ═══ 应用启动 ═══
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
        override val label = "无操作"
        override val minApi = 16
    }

    companion object {
        fun allFixed(): List<ActionNode> = listOf(
            Back, Home, Recents, SwitchLastApp,
            LockScreen, Screenshot, SplitScreen, PowerMenu,
            NotificationPanel, QuickSettings,
            MediaPlayPause, MediaNext, MediaPrevious, VolumeUp, VolumeDown,
            ToggleFlashlight,
            NoAction,
        )

        fun fromId(id: String): ActionNode? {
            if (id.startsWith("launch_app:")) {
                val pkg = id.removePrefix("launch_app:")
                return LaunchApp(pkg, pkg)
            }
            return allFixed().find { it.id == id }
        }
    }
}
