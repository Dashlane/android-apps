package com.dashlane.session.repository

import android.content.Context
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.teamspaces.db.TeamspaceForceCategorizationManager
import com.dashlane.teamspaces.manager.RevokedDetector
import com.dashlane.teamspaces.manager.SpaceAnonIdDataProvider
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.teamspaces.manager.TeamspaceRestrictionNotificator
import com.dashlane.teamspaces.manager.TeamspaceUpdater
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.util.userfeatures.UserFeaturesChecker
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamspaceManagerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDataRepository: UserDataRepository,
    private val accountStatusRepository: Lazy<AccountStatusRepository>,
    private val teamspaceUpdater: Lazy<TeamspaceUpdater>, 
    private val forceCategorizationManager: Lazy<TeamspaceForceCategorizationManager>,
    private val revokedDetector: RevokedDetector,
    private val notificator: TeamspaceRestrictionNotificator,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val userPreferencesManager: UserPreferencesManager,
    @ApplicationCoroutineScope
    private val coroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainDispatcher: CoroutineDispatcher
) : SessionObserver, BySessionRepository<TeamspaceAccessor> {
    private val teamspaceManagerPerSession = mutableMapOf<Session, TeamspaceManager>()

    fun getTeamspaceManager(session: Session) = teamspaceManagerPerSession[session]

    fun sessionInitializing(session: Session) {
        val teamspacesJson = accountStatusRepository.get().getPremiumStatus(session).teamspaces
        val spaceAnonIdDataProvider = SpaceAnonIdDataProvider()
        val spaces = teamspaceUpdater.get().deserializeTeamspaces(teamspacesJson)
        teamspaceManagerPerSession[session] =
            TeamspaceManager(
                context = context,
                dataProvider = spaceAnonIdDataProvider,
                restrictionNotificator = notificator,
                settingsManager = userDataRepository.getSettingsManager(session),
                revokedDetector = revokedDetector,
                coroutineScope = coroutineScope,
                mainDispatcher = mainDispatcher,
                userFeaturesChecker = userFeaturesChecker,
                userPreferencesManager = userPreferencesManager
            ).also {
                it.init(spaces, forceCategorizationManager.get())
            }
    }

    override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        teamspaceManagerPerSession.remove(session)
    }

    override fun get(session: Session?): TeamspaceManager? = session?.let { getTeamspaceManager(it) }
}