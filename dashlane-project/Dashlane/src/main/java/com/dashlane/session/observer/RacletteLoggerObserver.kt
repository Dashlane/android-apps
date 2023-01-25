package com.dashlane.session.observer

import com.dashlane.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.useractivity.RacletteLogger



class RacletteLoggerObserver(private val racletteLogger: RacletteLogger) : SessionObserver {

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        racletteLogger.sessionStartedIfLegacyDatabase(session)
    }

    override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        racletteLogger.sessionEndedIfLegacyDatabase(session)
    }
}