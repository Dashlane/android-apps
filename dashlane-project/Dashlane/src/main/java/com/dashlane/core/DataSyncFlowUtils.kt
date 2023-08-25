package com.dashlane.core

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.transform
import kotlin.time.Duration

@Suppress("FunctionName")
fun <T> SyncCommandFlow() = MutableSharedFlow<T>(
    replay = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)

fun <T> Flow<T>.throttleLatest(period: Duration): Flow<T> = this
    .conflate()
    .transform {
        emit(it)
        delay(period)
    }
