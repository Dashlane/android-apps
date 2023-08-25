package com.dashlane.session.repository

import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.teamspaces.db.TeamspaceForceCategorizationManager
import com.dashlane.teamspaces.db.TeamspaceUsageLogSpaceChanged
import com.dashlane.teamspaces.manager.SpaceAnonIdDataProvider
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.teamspaces.manager.TeamspaceUpdater
import com.dashlane.useractivity.log.usage.UsageLogRepository
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamspaceManagerRepository @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val accountStatusRepository: Lazy<AccountStatusRepository>,
    private val teamspaceUpdater: Lazy<TeamspaceUpdater>, 
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val forceCategorizationManager: Lazy<TeamspaceForceCategorizationManager>
) : SessionObserver, BySessionRepository<TeamspaceAccessor> {
    private val teamspaceManagerPerSession = mutableMapOf<Session, TeamspaceManager>()

    fun getTeamspaceManager(session: Session) = teamspaceManagerPerSession[session]

    fun sessionInitializing(session: Session) {
        val teamspacesJson = accountStatusRepository.get().getPremiumStatus(session).teamspaces
        val spaceAnonIdDataProvider = SpaceAnonIdDataProvider()
        val spaces = teamspaceUpdater.get().deserializeTeamspaces(teamspacesJson)
        teamspaceManagerPerSession[session] =
            TeamspaceManager(
                spaceAnonIdDataProvider,
                userDataRepository.getSettingsManager(session),
                TeamspaceUsageLogSpaceChanged(bySessionUsageLogRepository, session)
            ).also {
                it.init(spaces, forceCategorizationManager.get())
            }
    }

    override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        teamspaceManagerPerSession.remove(session)
    }

    override fun get(session: Session?): TeamspaceManager? = session?.let { getTeamspaceManager(it) }
}