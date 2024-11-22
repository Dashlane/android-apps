package com.dashlane.session

sealed class SessionState {
    data object Default : SessionState()
    data object Initializing : SessionState()
    data class Ready(val session: Session) : SessionState()
}