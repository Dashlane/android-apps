package com.dashlane.analytics.metrics.time

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode34



class SpentTimeOnViewLogger(
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val bySessionTeamspaceRepository: BySessionRepository<TeamspaceAccessor>
) {

    fun log(viewName: String, duration: Long) {
        val session = sessionManager.session ?: return

        bySessionUsageLogRepository[session]
            ?.enqueue(
                UsageLogCode34(
                    spaceId = bySessionTeamspaceRepository[session]?.current?.anonTeamId,
                    viewName = viewName,
                    duration = duration
                )
            )
    }
}