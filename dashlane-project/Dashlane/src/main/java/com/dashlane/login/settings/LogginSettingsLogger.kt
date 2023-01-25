package com.dashlane.login.settings

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class LogginSettingsLogger @Inject constructor(
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val sessionManager: SessionManager
) : LoginSettingsContract.Logger {
    override fun logDisplay() {
        log35("display")
    }

    override fun logNext() {
        log35("nextScreen")
    }

    override fun logShowFAQ() {
        log35("showFaq")
    }

    private fun log35(action: String) {
        log(UsageLogCode35(type = "settingsFirstLogin", action = action))
    }

    private fun log(log: UsageLog) {
        bySessionUsageLogRepository[sessionManager.session]?.enqueue(log)
    }
}