package com.dashlane.events

import com.dashlane.event.AppEvent

data class SyncFinishedEvent(
    val state: State,
    val trigger: Trigger
) : AppEvent {
    enum class State {
        SUCCESS,
        ERROR,
        OFFLINE
    }

    enum class Trigger {
        BY_USER,
        OTHER
    }
}