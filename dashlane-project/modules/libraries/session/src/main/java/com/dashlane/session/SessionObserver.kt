package com.dashlane.session

import com.dashlane.data.login.LoginInfo

interface SessionObserver {
    suspend fun sessionStarted(session: Session, loginInfo: LoginInfo? = null) = Unit

    suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) = Unit
}