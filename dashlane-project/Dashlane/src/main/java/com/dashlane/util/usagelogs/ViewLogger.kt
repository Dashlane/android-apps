package com.dashlane.util.usagelogs

import androidx.annotation.VisibleForTesting
import androidx.navigation.NavDestination
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.useractivity.log.usage.UsageLogCode34
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.isSemanticallyNull
import javax.inject.Inject

class ViewLogger @Inject constructor(
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val teamspaceManagerRepository: TeamspaceManagerRepository,
    private val navDestinationConverter: NavDestinationToUsageLogViewName
) {

    @VisibleForTesting
    var lastView: String? = ""

    fun log(destination: NavDestination) {
        log(navDestinationConverter.convert(destination))
    }

    fun log(viewName: String?) {
        if (viewName.isSemanticallyNull() || lastView == viewName) return

        sessionManager.session?.let { session ->
            bySessionUsageLogRepository[session]?.enqueue(
                UsageLogCode34(
                    spaceId = teamspaceManagerRepository[session]?.current?.anonTeamId,
                    viewName = viewName
                )
            )
        }
        lastView = viewName
    }
}