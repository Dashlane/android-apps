package com.dashlane.hermes

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class LogFlushLifecycleObserver(private val flush: LogFlush) : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        flush(null)
    }
}