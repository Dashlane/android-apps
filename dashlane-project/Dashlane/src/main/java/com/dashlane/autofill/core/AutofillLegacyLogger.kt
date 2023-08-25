package com.dashlane.autofill.core

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogRepository

open class AutofillLegacyLogger(
    protected val sessionManager: SessionManager,
    protected val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) {
    protected fun log(log: UsageLog) {
        bySessionUsageLogRepository[sessionManager.session]
            ?.enqueue(log)
    }
}
