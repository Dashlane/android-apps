package com.dashlane.login.pages.enforce2fa

import com.dashlane.limitations.Enforce2faLimiter
import com.dashlane.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun updateRedirectionFlag(
    enforce2faLimiter: Enforce2faLimiter,
    state: Enforce2faLimitState.View.UpdateRedirectionFlag,
): Flow<Enforce2faLimitEvent> {
    return flow {
        enforce2faLimiter.setRedirectionDone(state.redirectionDone)
        emit(Enforce2faLimitEvent.RedirectionFlagUpdated)
    }
}

fun logout(
    sessionManager: SessionManager,
    state: Enforce2faLimitState.View.Logout,
): Flow<Enforce2faLimitEvent> {
    return flow {
        sessionManager.session?.let { session ->
            sessionManager.destroySession(session, false)
        }
        emit(Enforce2faLimitEvent.GoToLogin)
    }
}