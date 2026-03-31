package com.openswipe.rule

import com.openswipe.model.*
import com.openswipe.overlay.Edge

object Presets {

    val IOS_STYLE = GestureRuleGraph(
        listOf(
            GestureRule(id = "ios_left_short", trigger = TriggerNode(Edge.LEFT, SectionRange.ALL, GestureType.SHORT_SWIPE), action = ActionNode.Back),
            GestureRule(id = "ios_right_short", trigger = TriggerNode(Edge.RIGHT, SectionRange.ALL, GestureType.SHORT_SWIPE), action = ActionNode.Back),
            GestureRule(id = "ios_bottom_short", trigger = TriggerNode(Edge.BOTTOM, SectionRange.ALL, GestureType.SHORT_SWIPE), action = ActionNode.Home),
        )
    )

    val ANDROID_CLASSIC = GestureRuleGraph(
        listOf(
            GestureRule(id = "classic_bottom_left", trigger = TriggerNode(Edge.BOTTOM, SectionRange.thirds(0), GestureType.SHORT_SWIPE), action = ActionNode.Back),
            GestureRule(id = "classic_bottom_mid", trigger = TriggerNode(Edge.BOTTOM, SectionRange.thirds(1), GestureType.SHORT_SWIPE), action = ActionNode.Home),
            GestureRule(id = "classic_bottom_right", trigger = TriggerNode(Edge.BOTTOM, SectionRange.thirds(2), GestureType.SHORT_SWIPE), action = ActionNode.Recents),
        )
    )

    val MEDIA_CONTROL = GestureRuleGraph(
        listOf(
            GestureRule(id = "media_bottom_left", trigger = TriggerNode(Edge.BOTTOM, SectionRange.thirds(0), GestureType.SHORT_SWIPE), action = ActionNode.MediaPrevious),
            GestureRule(id = "media_bottom_mid", trigger = TriggerNode(Edge.BOTTOM, SectionRange.thirds(1), GestureType.SHORT_SWIPE), action = ActionNode.MediaPlayPause),
            GestureRule(id = "media_bottom_right", trigger = TriggerNode(Edge.BOTTOM, SectionRange.thirds(2), GestureType.SHORT_SWIPE), action = ActionNode.MediaNext),
        )
    )

    /** Default preset: iOS-style with long swipe extensions */
    val DEFAULT = GestureRuleGraph(
        listOf(
            // Left/right short swipe → Back
            GestureRule(id = "default_left_short", trigger = TriggerNode(Edge.LEFT, SectionRange.ALL, GestureType.SHORT_SWIPE), action = ActionNode.Back),
            GestureRule(id = "default_right_short", trigger = TriggerNode(Edge.RIGHT, SectionRange.ALL, GestureType.SHORT_SWIPE), action = ActionNode.Back),
            // Left/right long swipe → Switch last app
            GestureRule(id = "default_left_long", trigger = TriggerNode(Edge.LEFT, SectionRange.ALL, GestureType.LONG_SWIPE), action = ActionNode.SwitchLastApp),
            GestureRule(id = "default_right_long", trigger = TriggerNode(Edge.RIGHT, SectionRange.ALL, GestureType.LONG_SWIPE), action = ActionNode.SwitchLastApp),
            // Bottom short swipe → Home
            GestureRule(id = "default_bottom_short", trigger = TriggerNode(Edge.BOTTOM, SectionRange.ALL, GestureType.SHORT_SWIPE), action = ActionNode.Home),
            // Bottom long swipe → Recents
            GestureRule(id = "default_bottom_long", trigger = TriggerNode(Edge.BOTTOM, SectionRange.ALL, GestureType.LONG_SWIPE), action = ActionNode.Recents),
        )
    )
}
