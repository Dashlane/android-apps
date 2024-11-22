package com.dashlane.session.observer

import com.dashlane.login.LoginInfo
import com.dashlane.session.authorization
import com.dashlane.notification.FcmHelper
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver

class FcmRegistrationObserver(private val fcmHelper: FcmHelper) : SessionObserver {

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        fcmHelper.register(session.authorization)
    }

    override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        fcmHelper.unregister(session.authorization)
    }
}