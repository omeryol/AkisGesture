package io.github.omeryol.akisgesture.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import io.github.omeryol.akisgesture.model.ActionNode

/**
 * Keeps app-internal navigation out of the accessibility action dispatcher
 * while allowing the Back action to behave naturally inside Akış itself.
 */
object InternalNavigationBus {
    private val _backRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val backRequests = _backRequests.asSharedFlow()

    data class ActionPickerRequest(val token: String, val appSelectionOnly: Boolean = false)
    data class ActionPickerResult(val token: String, val action: ActionNode)

    private val _actionPickerRequests = MutableSharedFlow<ActionPickerRequest>(extraBufferCapacity = 1)
    val actionPickerRequests = _actionPickerRequests.asSharedFlow()
    private val _actionPickerResults = MutableSharedFlow<ActionPickerResult>(extraBufferCapacity = 1)
    val actionPickerResults = _actionPickerResults.asSharedFlow()

    fun requestActionPicker(request: ActionPickerRequest): Boolean = _actionPickerRequests.tryEmit(request)
    fun publishActionPickerResult(result: ActionPickerResult): Boolean = _actionPickerResults.tryEmit(result)

    fun requestBack(): Boolean {
        if (_backRequests.subscriptionCount.value > 0) {
            return _backRequests.tryEmit(Unit)
        }
        return false
    }
}
