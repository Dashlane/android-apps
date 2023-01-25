package com.dashlane.ui.menu

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository

class MenuUsageLogger(
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) {

    fun logMenuLock() {
        log(
            UsageLogCode75(
                type = "lock",
                subtype = "leftMenu",
                action = "lock"
            )
        )
    }

    fun logSpaceChange(teamspace: Teamspace?) {
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