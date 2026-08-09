package io.github.omeryol.akisgesture.diagnostics

import io.github.omeryol.akisgesture.action.ActionResult
import io.github.omeryol.akisgesture.root.RootResult

/** Debug builds intentionally keep runtime diagnostics disabled. */
object RuntimeDiagnostics {
    fun serviceConnected() = Unit
    fun engineStarted() = Unit
    fun serviceDisconnected(reason: String) = Unit
    fun gestureMatched(edge: String, gesture: String, actionId: String?) = Unit
    fun gestureSignal(edge: String, signal: String) = Unit
    fun actionFinished(actionId: String, result: ActionResult) = Unit
    fun repairFinished(action: String, result: RootResult) = Unit
}
