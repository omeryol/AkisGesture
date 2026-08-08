package io.github.omeryol.akisgesture.ui.util

import io.github.omeryol.akisgesture.model.ActionNode
import java.util.Locale

private val turkishLocale = Locale("tr", "TR")

fun filterActions(
    actions: List<ActionNode>,
    query: String,
    labelForAction: (ActionNode) -> String = ActionNode::label,
): List<ActionNode> {
    val normalized = query.trim().lowercase(turkishLocale)
    if (normalized.isBlank()) return actions
    return actions.filter { action ->
        labelForAction(action).lowercase(turkishLocale).contains(normalized) ||
            (action is ActionNode.LaunchApp &&
                action.packageName.lowercase(Locale.ROOT).contains(normalized))
    }
}
