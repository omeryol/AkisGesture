package com.openswipe.model

import java.util.UUID

enum class TriggerMode {
    /** 轻触即检测（触碰就响应） */
    TOUCH,
    /** 滑动才触发（点击穿透到下层App） */
    SWIPE,
}

data class GestureRule(
    val id: String = UUID.randomUUID().toString(),
    val trigger: TriggerNode,
    val action: ActionNode,
    val enabled: Boolean = true,
    val triggerMode: TriggerMode = TriggerMode.SWIPE,
)
