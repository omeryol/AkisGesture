package com.omer.akisgesture.model

import java.util.UUID

enum class TriggerMode {
    /** Dokunma即检测（触碰就响应） */
    TOUCH,
    /** Kaydırma才触发（点击穿透到下层App） */
    SWIPE,
}

data class GestureRule(
    val id: String = UUID.randomUUID().toString(),
    val trigger: TriggerNode,
    val action: ActionNode,
    val enabled: Boolean = true,
    val triggerMode: TriggerMode = TriggerMode.SWIPE,
)
