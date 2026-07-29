package com.omer.akisgesture.rule

import com.omer.akisgesture.model.ActionNode
import com.omer.akisgesture.model.GestureRule
import com.omer.akisgesture.model.GestureType
import com.omer.akisgesture.model.SectionRange
import com.omer.akisgesture.model.TriggerNode
import com.omer.akisgesture.overlay.Edge

object Presets {
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

    val DEFAULT = IOS_STYLE

    val ALL: List<Pair<String, GestureRuleGraph>> = listOf(
        "Başlangıç · Sade" to SIMPLE,
        "Başlangıç · Android klasik" to ANDROID_CLASSIC,
        "Genel · Dengeli" to IOS_STYLE,
        "Tek el · Sağ kenar" to ONE_HAND_RIGHT,
        "Tek el · Sol kenar" to ONE_HAND_LEFT,
        "İleri · Çift kenar" to DUAL_EDGE_ADVANCED,
        "İleri · Üretkenlik" to PRODUCTIVITY,
        "Özel · Medya kontrolü" to MEDIA_CONTROL,
        "Root · Güçlü kullanım" to ROOT_POWER,
    )
}
