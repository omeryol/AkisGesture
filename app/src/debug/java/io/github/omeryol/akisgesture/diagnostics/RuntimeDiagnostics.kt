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
    fun feedbackSymbol(edge: String, gesture: String, symbol: String) = Unit
    fun ringOpened(edge: String) = Unit
    fun ringTiming(edge: String, phase: String, elapsedMs: Long) = Unit
    fun ringAnimation(edge: String, phase: String, slot: Int? = null) = Unit
    fun ringSelected(edge: String, index: Int) = Unit
    fun ringAction(edge: String, index: Int, actionId: String) = Unit
    fun ringDismissed(edge: String) = Unit
    fun ringHitProbe(edge: String, index: Int, x: Float, y: Float, touch: Float) = Unit
    fun actionFinished(actionId: String, result: ActionResult) = Unit
    fun repairFinished(action: String, result: RootResult) = Unit
}
