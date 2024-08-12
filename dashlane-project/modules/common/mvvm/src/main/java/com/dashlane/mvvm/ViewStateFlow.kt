package com.dashlane.mvvm

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

interface ViewStateFlow<T : State.View, S : State.SideEffect> {
    val viewState: StateFlow<T>
    val sideEffect: Flow<S>
}

class MutableViewStateFlow<T : State.View, S : State.SideEffect>(initialState: T) : ViewStateFlow<T, S> {

    @PublishedApi
    internal val viewStateFlow: MutableStateFlow<T> = MutableStateFlow(initialState)

    private val sideEffectFlow: Channel<S> = Channel()

    override val viewState: StateFlow<T> = viewStateFlow.asStateFlow()

    override val sideEffect: Flow<S> = sideEffectFlow.receiveAsFlow()

    val value: T
        get() = viewState.value

    inline fun update(function: (T) -> T): Unit = viewStateFlow.update(function)

    suspend fun send(state: S): Unit = sideEffectFlow.send(state)
}
