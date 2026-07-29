package com.omer.akisgesture.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Keeps app-internal navigation out of the accessibility action dispatcher
 * while allowing the Back action to behave naturally inside Akış itself.
 */
object InternalNavigationBus {
    private val _backRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val backRequests = _backRequests.asSharedFlow()

    fun requestBack(): Boolean = _backRequests.tryEmit(Unit)
}
