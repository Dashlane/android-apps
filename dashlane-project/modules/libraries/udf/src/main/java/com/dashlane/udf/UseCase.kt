package com.dashlane.udf

import com.dashlane.mvvm.State
import com.dashlane.utils.coroutines.flatMapConcatDropLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge

typealias UseCase<S, E> = (Flow<S>) -> Flow<E>

@ExperimentalCoroutinesApi
@FlowPreview
fun <S : State, E : Event> ((S) -> Flow<E>).toUseCase(strategy: Strategy = Strategy.ContinueOnNewState): UseCase<S, E> =
    { states ->
        when (strategy) {
            Strategy.ContinueOnNewState -> states.flatMapMerge(transform = this)
            Strategy.CancelOnNewState -> states.flatMapLatest(transform = this)
            Strategy.DropOnNewState -> states.flatMapConcatDropLatest(transform = this)
            is Strategy.Debounce -> states.debounce(strategy.time).flatMapLatest(transform = this)
        }
    }

@ExperimentalCoroutinesApi
@FlowPreview
inline fun <S : State, E : Event, reified SS : S> ((SS) -> Flow<E>).toFilteredUseCase(strategy: Strategy): UseCase<S, E> =
    { states -> states.filterIsInstance<SS>().let(this.toUseCase(strategy)) }

fun <S : State, E : Event> Flow<E>.toUseCase(): UseCase<S, E> = { this }