package com.dashlane.utils.coroutines

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

fun <T, R> Flow<T>.takeUntil(notifier: Flow<R>): Flow<T> = flow {
    try {
        coroutineScope {
            val job = launch(start = CoroutineStart.UNDISPATCHED) {
                notifier.take(1).collect()
                throw ClosedException(this@flow)
            }

            collect { emit(it) }
            job.cancel()
        }
    } catch (e: ClosedException) {
        e.checkOwnership(this@flow)
    }
}

fun <T, R> Flow<T>.scanOptional(
    initial: R,
    operation: suspend (accumulator: R, value: T) -> R?,
): Flow<R> = flow {
    var accumulator = initial
    emit(accumulator)
    collect { value ->
        operation(accumulator, value)?.let {
            accumulator = it
            emit(accumulator)
        }
    }
}

fun <T, R> Flow<T>.flatMapConcatDropLatest(transform: suspend (value: T) -> Flow<R>): Flow<R> {
    return flow {
        channelFlow {
            collect { upstreamValue -> trySend(upstreamValue) }
        }
            .buffer(capacity = 0)
            .collect { upstreamValue -> transform(upstreamValue).collect { emit(it) } }
    }
}

internal class ClosedException(val owner: FlowCollector<*>) :
    Exception("Flow was aborted, no more elements needed")

internal fun ClosedException.checkOwnership(owner: FlowCollector<*>) {
    if (this.owner !== owner) throw this
}