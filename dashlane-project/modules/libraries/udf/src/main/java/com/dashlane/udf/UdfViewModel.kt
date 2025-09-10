package com.dashlane.udf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.State
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.utils.coroutines.scanOptional
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

const val UDF_TAG = "UDF"

@OptIn(ExperimentalCoroutinesApi::class)
abstract class UDFViewModel<S : State.View, T : State.SideEffect, E : Event>(
    private val useCases: MutableList<UseCase<S, E>>,
    private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private lateinit var _stateFlow: MutableViewStateFlow<S, T>

    lateinit var stateFlow: ViewStateFlow<S, T>
        private set

    private val eventChannel: Channel<E> = Channel()

    private var hasStarted = false

    fun start(stateMachine: StateMachine<S, E, T>) {
        if (hasStarted) return

        _stateFlow = MutableViewStateFlow(stateMachine.initialState)
        stateFlow = _stateFlow

        hasStarted = true
        useCases.add(eventChannel.receiveAsFlow().toUseCase())

        _stateFlow.viewState
            .let { states -> useCases.map { it(states) }.asFlow().flattenMerge() }
            .scanOptional(stateMachine.initialState) { previousState, event ->
                val transition = stateMachine.transition(previousState, event)
                transition?.let {
                    it.sideEffect?.let { sideEffect ->
                        viewModelScope.launch {
                            _stateFlow.send(sideEffect)
                        }
                    }
                    it.toState
                }
            }
            .onEach {
                _stateFlow.emit(it)
            }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)
    }

    fun sendEvent(event: E) {
        viewModelScope.launch { eventChannel.send(event) }
    }
}