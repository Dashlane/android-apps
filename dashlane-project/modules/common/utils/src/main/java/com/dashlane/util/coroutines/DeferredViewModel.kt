package com.dashlane.util.coroutines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class DeferredViewModel<T> : ViewModel() {

    private val scope: CoroutineScope = MainScope()

    var deferred: Deferred<T>? = null

    fun async(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> T
    ): Deferred<T> = scope.async(context, start, block).also { deferred = it }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> ViewModelProvider.getDeferredViewModel(tag: String): DeferredViewModel<T> =
    get(tag, DeferredViewModel::class.java) as DeferredViewModel<T>