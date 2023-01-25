package com.dashlane.session.observer

import com.dashlane.core.DataSync
import com.dashlane.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver



class SyncObserver(private val dataSync: DataSync) : SessionObserver {

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        dataSync.maySync()
    }

    override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        
        dataSync.stopSync()
    }
}