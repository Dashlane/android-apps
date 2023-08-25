package com.dashlane.ui.menu

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

interface MenuUsageLogger {
    fun logMenuLock()
    fun logSpaceChange(teamspace: Teamspace?)
}

class MenuUsageLoggerImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : MenuUsageLogger {

    override fun logMenuLock() {
        log(
            UsageLogCode75(
                type = "lock",
                subtype = "leftMenu",
                action = "lock"
            )
        )
    }

    override fun logSpaceChange(teamspace: Teamspace?) {
        log(
            UsageLogCode75(
                type = "MainMenu",
                action = "TeamSpaceSwitch",
                subaction = teamspace?.anonTeamId
            )
        )
    }

    private fun log(log: UsageLog) {
        bySessionUsageLogRepository[sessionManager.session]?.enqueue(log)
    }
}