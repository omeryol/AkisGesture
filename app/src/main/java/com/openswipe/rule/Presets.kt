package com.omer.akisgesture.rule

import com.omer.akisgesture.model.*
import com.omer.akisgesture.overlay.Edge

object Presets {

    val IOS_STYLE = GestureRuleGraph(
        listOf(
            GestureRule(id = "ios_left", trigger = TriggerNode(Edge.LEFT, SectionRange.ALL, GestureType.QUICK_SWIPE), action = ActionNode.Back),
            GestureRule(id = "ios_right", trigger = TriggerNode(Edge.RIGHT, SectionRange.ALL, GestureType.QUICK_SWIPE), action = ActionNode.Back),
            GestureRule(id = "ios_bottom", trigger = TriggerNode(Edge.BOTTOM, SectionRange.ALL, GestureType.QUICK_SWIPE), action = ActionNode.Home),
        )
    )

    val ANDROID_CLASSIC = GestureRuleGraph(
        listOf(
            GestureRule(id = "classic_bottom_left", trigger = TriggerNode(Edge.BOTTOM, SectionRange.thirds(0), GestureType.QUICK_SWIPE), action = ActionNode.Back),
            GestureRule(id = "classic_bottom_mid", trigger = TriggerNode(Edge.BOTTOM, SectionRange.thirds(1), GestureType.QUICK_SWIPE), action = ActionNode.Home),
            GestureRule(id = "classic_bottom_right", trigger = TriggerNode(Edge.BOTTOM, SectionRange.thirds(2), GestureType.QUICK_SWIPE), action = ActionNode.Recents),
        )
    )

    val MEDIA_CONTROL = GestureRuleGraph(
        listOf(
            GestureRule(id = "media_bottom_left", trigger = TriggerNode(Edge.BOTTOM, SectionRange.thirds(0), GestureType.QUICK_SWIPE), action = ActionNode.MediaPrevious),
            GestureRule(id = "media_bottom_mid", trigger = TriggerNode(Edge.BOTTOM, SectionRange.thirds(1), GestureType.QUICK_SWIPE), action = ActionNode.MediaPlayPause),
            GestureRule(id = "media_bottom_right", trigger = TriggerNode(Edge.BOTTOM, SectionRange.thirds(2), GestureType.QUICK_SWIPE), action = ActionNode.MediaNext),
        )
    )

    /** Default preset: unified swipe gestures */
    val DEFAULT = GestureRuleGraph(
        listOf(
            // Left/right swipe → Back
            GestureRule(id = "default_left", trigger = TriggerNode(Edge.LEFT, SectionRange.ALL, GestureType.QUICK_SWIPE), action = ActionNode.Back),
            GestureRule(id = "default_right", trigger = TriggerNode(Edge.RIGHT, SectionRange.ALL, GestureType.QUICK_SWIPE), action = ActionNode.Back),
            // Bottom swipe → Home
            GestureRule(id = "default_bottom", trigger = TriggerNode(Edge.BOTTOM, SectionRange.ALL, GestureType.QUICK_SWIPE), action = ActionNode.Home),
        )
    )
}
