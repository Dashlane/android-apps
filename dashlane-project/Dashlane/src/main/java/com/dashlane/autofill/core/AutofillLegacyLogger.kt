package com.dashlane.autofill.core

import com.dashlane.BuildConfig
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.install.InstallLog
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogRepository



open class AutofillLegacyLogger(
    protected val sessionManager: SessionManager,
    protected val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    protected val installLogRepository: InstallLogRepository? = null
) {
    protected fun log(log: UsageLog) {
        bySessionUsageLogRepository[sessionManager.session]
            ?.enqueue(log)
    }

    protected fun log(log: InstallLog) {
        if (installLogRepository == null && BuildConfig.DEBUG) {
            error("Cannot send install without a valid InstallLogRepository")
        }
        installLogRepository?.enqueue(log)
    }
}
