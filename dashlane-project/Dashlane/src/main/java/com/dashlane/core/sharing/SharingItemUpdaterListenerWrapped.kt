package com.dashlane.core.sharing

import com.dashlane.util.logE
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
                logE(throwable = e) { "Unable to apply the sharing request" }
                listener?.onException(e)
            }
        }
    }

    interface Listener {
        fun onExecutionOver()
        fun onException(e: Exception)
    }
}