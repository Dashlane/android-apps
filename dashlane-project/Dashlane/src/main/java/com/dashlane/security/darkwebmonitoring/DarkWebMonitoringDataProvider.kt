package com.dashlane.security.darkwebmonitoring

import com.dashlane.darkweb.DarkWebEmailStatus
import com.dashlane.darkweb.DarkWebMonitoringManager
import com.dashlane.events.AppEvents
import com.dashlane.events.BreachStatusChangedEvent
import com.dashlane.events.BreachesRefreshedEvent
import com.dashlane.events.DarkWebSetupCompleteEvent
import com.dashlane.events.register
import com.dashlane.events.unregister
import com.dashlane.notificationcenter.alerts.BreachDataHelper
import com.dashlane.security.identitydashboard.breach.BreachLoader
import com.dashlane.security.identitydashboard.breach.BreachWrapper
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.teamspaces.manager.TeamspaceManagerWeakListener
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.provider.BaseDataProvider
import java.lang.ref.WeakReference
import javax.inject.Inject

class DarkWebMonitoringDataProvider @Inject constructor(
    private val breachLoader: BreachLoader,
    private val darkWebMonitoringManager: DarkWebMonitoringManager,
    private val sessionManager: SessionManager,
    private val teamspaceRepository: TeamspaceManagerRepository,
    private val breachDataHelper: BreachDataHelper,
    private val appEvents: AppEvents
) : BaseDataProvider<DarkWebMonitoringContract.Presenter>(),
DarkWebMonitoringContract.DataProvider,
    TeamspaceManager.Listener {

    private val appEventListener = AppEventsListener(appEvents, this)
    private val teamspaceManagerListener = TeamspaceManagerWeakListener(this)

    override suspend fun getDarkwebBreaches(): List<BreachWrapper> = breachLoader.getBreachesWrapper().filter {
        it.publicBreach.isDarkWebBreach()
    }

    override suspend fun deleteBreaches(breaches: List<BreachWrapper>) {
        breaches.forEach {
            breachDataHelper.saveAndRemove(it, SyncObject.SecurityBreach.Status.ACKNOWLEDGED)
        }
        appEvents.post(BreachStatusChangedEvent())
    }

    override suspend fun unlistenDarkWeb(email: String) {
        darkWebMonitoringManager.optOut(email)
    }

    override suspend fun getDarkwebEmailStatuses(): List<DarkWebEmailStatus>? =
        darkWebMonitoringManager.getEmailsWithStatus()

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
        refreshUI()
    }

    override fun onTeamspacesUpdate() {
        
    }

    private fun refreshUI() {
        presenter.requireRefresh()
    }

    class AppEventsListener(
        private val appEvents: AppEvents,
        dataProvider: DarkWebMonitoringDataProvider
    ) {
        private val dataProviderRef = WeakReference(dataProvider)

        fun listen() {
            appEvents.register<DarkWebSetupCompleteEvent>(this) {
                dataProviderRef.get()?.apply {
                    refreshUI()
                } ?: appEvents.unregister<DarkWebSetupCompleteEvent>(this)
            }
            appEvents.register<BreachesRefreshedEvent>(this) {
                dataProviderRef.get()?.apply {
                    refreshUI()
                } ?: appEvents.unregister<BreachesRefreshedEvent>(this)
            }
        }

        fun unlisten() {
            appEvents.unregister<DarkWebSetupCompleteEvent>(this)
            appEvents.unregister<BreachesRefreshedEvent>(this)
        }
    }
}