package com.dashlane.session.observer

import com.dashlane.logger.utils.LogsSender
import com.dashlane.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.useractivity.AggregateUserActivity
import com.dashlane.util.Constants

class LogsObserver(
    private val aggregateUserActivity: AggregateUserActivity,
    private val logsSender: LogsSender
) : SessionObserver {

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        aggregateUserActivity.send()
    }

    override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        aggregateUserActivity.send(0)
        Constants.TIME.LOGOUT_TIME_SECONDS = System.currentTimeMillis() / 1000
        Constants.TIME.LOGOUT_TIME_SECONDS = 0
        Constants.TIME.LOGIN_TIME_SECONDS = 0
        
        logsSender.flushLogs()
    }
}