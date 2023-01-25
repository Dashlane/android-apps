package com.dashlane.session.observer

import com.dashlane.logger.utils.LogsSender
import com.dashlane.session.BySessionRepository
import com.dashlane.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.useractivity.AggregateUserActivity
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode3
import com.dashlane.util.Constants



class LogsObserver(
    private val aggregateUserActivity: AggregateUserActivity,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : SessionObserver {

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        aggregateUserActivity.send()
    }

    override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        aggregateUserActivity.send(0)
        Constants.TIME.LOGOUT_TIME_SECONDS = System.currentTimeMillis() / 1000
        val timeSpendLoggedIn = Constants.TIME.LOGOUT_TIME_SECONDS - Constants.TIME.LOGIN_TIME_SECONDS
        Constants.TIME.LOGOUT_TIME_SECONDS = 0
        Constants.TIME.LOGIN_TIME_SECONDS = 0
        if (byUser) {
            bySessionUsageLogRepository[session]
                ?.enqueue(
                    UsageLogCode3(
                        sender = UsageLogCode3.Sender.FROM_MOBILE,
                        duration = timeSpendLoggedIn
                    )
                )
        }
        
        LogsSender.flushLogs()
    }
}