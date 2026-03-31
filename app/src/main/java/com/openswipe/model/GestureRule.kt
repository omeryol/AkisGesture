package com.openswipe.model

import java.util.UUID

data class GestureRule(
    val id: String = UUID.randomUUID().toString(),
    val trigger: TriggerNode,
    val action: ActionNode
)
