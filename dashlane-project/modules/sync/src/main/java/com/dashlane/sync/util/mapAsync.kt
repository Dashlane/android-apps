package com.dashlane.sync.util

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.coroutineScope

@Suppress("EXPERIMENTAL_API_USAGE")
internal suspend inline fun <R, T> Iterable<T>.mapAsync(
    progressChannel: SendChannel<Unit>?,
    crossinline transform: (T) -> R
): List<R> = coroutineScope {
    val deferredChannel = produce<Deferred<R>> {
        for (item in this@mapAsync) {
            send(
                async {
                val result = transform(item)
                progressChannel?.send(Unit)
                result
            }
            )
        }
    }
    deferredChannel.toList().awaitAll()
}