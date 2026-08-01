package io.github.omeryol.akisgesture.model

import java.util.UUID

enum class TriggerMode {
    /** Dokunma anında tespit et — kenarın üstünde hemen yanıt ver */
    TOUCH,
    /** Kayırma ile tetikle — kaydırma eşiği geçilene kadar dokunmayı alt katmana geçir */
    SWIPE,
}

data class GestureRule(
    val id: String = UUID.randomUUID().toString(),
    val trigger: TriggerNode,
    val action: ActionNode,
    val enabled: Boolean = true,
    val triggerMode: TriggerMode = TriggerMode.SWIPE,
)
