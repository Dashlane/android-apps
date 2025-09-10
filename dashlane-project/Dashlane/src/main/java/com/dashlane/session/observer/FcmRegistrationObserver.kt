package com.dashlane.session.observer

import com.dashlane.data.login.LoginInfo
import com.dashlane.notification.FcmHelper
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.session.authorization

class FcmRegistrationObserver(private val fcmHelper: FcmHelper) : SessionObserver {
    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        fcmHelper.register(session.authorization)
    }

    override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        fcmHelper.unregister(session.authorization)
    }
}