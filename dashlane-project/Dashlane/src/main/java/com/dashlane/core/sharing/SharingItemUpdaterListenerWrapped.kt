package com.dashlane.core.sharing

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SharingItemUpdaterListenerWrapped(private val updater: SharingItemUpdater) {

    @OptIn(DelicateCoroutinesApi::class)
    fun execute(request: SharingItemUpdaterRequest, listener: Listener?) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                updater.update(request)
                listener?.onExecutionOver()
            } catch (e: Exception) {
                listener?.onException(e)
            }
        }
    }

    interface Listener {
        fun onExecutionOver()
        fun onException(e: Exception)
    }
}
