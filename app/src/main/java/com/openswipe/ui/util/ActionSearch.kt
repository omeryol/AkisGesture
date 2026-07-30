package com.omer.akisgesture.ui.util

import com.omer.akisgesture.model.ActionNode
import java.util.Locale

private val turkishLocale = Locale("tr", "TR")

fun filterActions(
    actions: List<ActionNode>,
    query: String,
): List<ActionNode> {
    val normalized = query.trim().lowercase(turkishLocale)
    if (normalized.isBlank()) return actions
    return actions.filter { action ->
        action.label.lowercase(turkishLocale).contains(normalized) ||
            (action is ActionNode.LaunchApp &&
                action.packageName.lowercase(Locale.ROOT).contains(normalized))
    }
}
