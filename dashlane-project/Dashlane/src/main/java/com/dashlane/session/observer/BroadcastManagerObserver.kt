package com.dashlane.session.observer

import com.dashlane.async.BroadcastManager
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver

class BroadcastManagerObserver : SessionObserver {

    override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        BroadcastManager.removeAllBufferedIntent()
    }
}