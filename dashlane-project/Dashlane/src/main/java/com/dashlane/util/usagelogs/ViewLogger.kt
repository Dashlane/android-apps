package com.dashlane.util.usagelogs

import androidx.annotation.VisibleForTesting
import androidx.navigation.NavDestination
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.useractivity.log.usage.UsageLogCode34
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.isSemanticallyNull



class ViewLogger @VisibleForTesting internal constructor(
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val bySessionTeamspaceAccessor: BySessionRepository<TeamspaceAccessor>,
    private val navDestinationConverter: NavDestinationToUsageLogViewName
) {

    @VisibleForTesting
    var lastView: String? = ""

    constructor() : this(
        SingletonProvider.getSessionManager(),
        SingletonProvider.getComponent().bySessionUsageLogRepository,
        SingletonProvider.getComponent().teamspaceRepository,
        NavDestinationToUsageLogViewName()
    )

    fun log(destination: NavDestination) {
        log(navDestinationConverter.convert(destination))
    }

    fun log(viewName: String?) {
        if (viewName.isSemanticallyNull() || lastView == viewName) return

        sessionManager.session?.let { session ->
            bySessionUsageLogRepository[session]?.enqueue(
                UsageLogCode34(
                    spaceId = bySessionTeamspaceAccessor[session]?.current?.anonTeamId,
                    viewName = viewName
                )
            )
        }
        lastView = viewName
    }
}