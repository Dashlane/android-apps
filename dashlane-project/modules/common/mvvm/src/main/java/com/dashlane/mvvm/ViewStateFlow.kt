package com.dashlane.mvvm

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

interface ViewStateFlow<T : State.View, S : State.SideEffect> {
    val viewState: StateFlow<T>
    val sideEffect: Flow<S>
}

class MutableViewStateFlow<T : State.View, S : State.SideEffect>(initialState: T) :
    ViewStateFlow<T, S>, MutableStateFlow<T> by MutableStateFlow(initialState) {
    private val sideEffectFlow: Channel<S> = Channel()

    override val viewState: StateFlow<T> = asStateFlow()

    override val sideEffect: Flow<S> = sideEffectFlow.receiveAsFlow()

    suspend fun send(state: S): Unit = sideEffectFlow.send(state)
}
