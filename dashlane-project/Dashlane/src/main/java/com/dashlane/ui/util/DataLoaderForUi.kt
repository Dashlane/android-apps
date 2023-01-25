package com.dashlane.ui.util

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



abstract class DataLoaderForUi<T>(private val lifecycleCoroutineScope: LifecycleCoroutineScope) {

    constructor(lifecycle: LifecycleOwner) : this(lifecycle.lifecycleScope)

    private var loading = false

    @MainThread
    fun start() {
        if (loading) {
            return
        }
        loading = true
        onPreExecute()
        lifecycleCoroutineScope.launchWhenStarted {
            val data = withContext(Dispatchers.Default) { loadData() }
            onPostExecute(data)
            loading = false
        }
    }

    @MainThread
    protected open fun onPreExecute() {
        
    }

    @WorkerThread
    protected abstract fun loadData(): T

    @MainThread
    protected abstract fun onPostExecute(t: T?)
}