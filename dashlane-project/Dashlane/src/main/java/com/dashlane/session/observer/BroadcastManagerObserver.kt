package com.dashlane.session.observer

import com.dashlane.async.SyncBroadcastManager
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import javax.inject.Inject

class BroadcastManagerObserver @Inject constructor(private val syncBroadcastManager: SyncBroadcastManager) :
    SessionObserver {

    override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        syncBroadcastManager.removePasswordBroadcastIntent()
    }
}