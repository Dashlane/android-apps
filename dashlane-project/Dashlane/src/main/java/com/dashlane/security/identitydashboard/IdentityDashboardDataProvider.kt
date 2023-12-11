package com.dashlane.security.identitydashboard

import com.dashlane.events.AppEvents
import com.dashlane.events.BreachStatusChangedEvent
import com.dashlane.events.clearLastEvent
import com.dashlane.events.register
import com.dashlane.events.unregister
import com.dashlane.security.identitydashboard.password.AuthentifiantSecurityEvaluator
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.teamspaces.manager.TeamspaceManagerWeakListener
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.skocken.presentation.provider.BaseDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.lang.ref.WeakReference
import javax.inject.Inject

class IdentityDashboardDataProvider @Inject constructor(
    private val creditMonitoringManager: CreditMonitoringManager,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val authentifiantSecurityEvaluator: AuthentifiantSecurityEvaluator,
    private val sessionManager: SessionManager,
    private val teamspaceRepository: TeamspaceManagerRepository,
    private val appEvents: AppEvents
) : BaseDataProvider<IdentityDashboardContract.Presenter>(),
IdentityDashboardContract.DataProvider,
    TeamspaceManager.Listener {

    lateinit var coroutineScope: CoroutineScope
    private val appEventListener = AppEventsListener(appEvents, this)
    private var latestSecurityScoreEvaluatorResult: Deferred<AuthentifiantSecurityEvaluator.Result?>? = null
    private val teamspaceManagerListener = TeamspaceManagerWeakListener(this)

    override fun hasProtectionPackage(): Boolean {
        return userFeaturesChecker.has(UserFeaturesChecker.Capability.CREDIT_MONITORING)
    }

    override fun shouldIdentityRestorationBeVisible(): Boolean {
        return userFeaturesChecker.has(UserFeaturesChecker.Capability.IDENTITY_RESTORATION)
    }

    override suspend fun getCreditMonitoringLink(): String? {
        return creditMonitoringManager.getLink()
    }

    override fun getAuthentifiantsSecurityInfoAsync(): Deferred<AuthentifiantSecurityEvaluator.Result?> {
        return latestSecurityScoreEvaluatorResult
            ?: coroutineScope.async(Dispatchers.Default) {
                getAuthentifiantSecurityInfoAsync()
            }.apply {
                latestSecurityScoreEvaluatorResult = this
            }
    }

    private suspend fun getAuthentifiantSecurityInfoAsync(): AuthentifiantSecurityEvaluator.Result? {
        val current = sessionManager.session?.let {
            teamspaceRepository.getTeamspaceManager(it)?.current
        } ?: return null
        return authentifiantSecurityEvaluator.computeResult(current)
    }

    override fun listenForChanges() {
        sessionManager.session?.let {
            teamspaceManagerListener.listen(teamspaceRepository.getTeamspaceManager(it))
        }
        appEventListener.listen()
    }

    override fun unlistenForChanges() {
        teamspaceManagerListener.listen(null) 
        appEventListener.unlisten()
    }

    override fun onStatusChanged(teamspace: Teamspace?, previousStatus: String?, newStatus: String?) {
        
    }

    override fun onChange(teamspace: Teamspace?) {
        latestSecurityScoreEvaluatorResult = null
        refreshUI()
    }

    override fun onTeamspacesUpdate() {
        
    }

    private fun refreshUI() {
        presenter.requireRefresh()
    }

    class AppEventsListener(
        private val appEvents: AppEvents,
        dataProvider: IdentityDashboardDataProvider
    ) {
        private val dataProviderRef = WeakReference(dataProvider)

        fun listen() {
            appEvents.register<BreachStatusChangedEvent>(this, true) {
                dataProviderRef.get()?.apply {
                    appEvents.clearLastEvent<BreachStatusChangedEvent>()
                    refreshUI()
                } ?: appEvents.unregister<BreachStatusChangedEvent>(this)
            }
        }

        fun unlisten() {
            runCatching {
                appEvents.unregister<BreachStatusChangedEvent>(this)
            }
        }
    }
}