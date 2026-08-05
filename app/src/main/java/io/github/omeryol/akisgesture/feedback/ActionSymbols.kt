package io.github.omeryol.akisgesture.feedback

import io.github.omeryol.akisgesture.model.ActionIconPack
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.toSymbol

/**
 * Identity-aligned, meaningful symbols for gesture actions.
 */
object ActionSymbols {
    fun symbolFor(action: ActionNode?, pack: ActionIconPack = ActionIconPack.EMOJI_MODERN): String {
        if (action == null || action is ActionNode.NoAction) return ""
        return action.toSymbol(pack)
    }
}
