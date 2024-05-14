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
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.provider.BaseDataProvider
import java.lang.ref.WeakReference
import javax.inject.Inject

class DarkWebMonitoringDataProvider @Inject constructor(
    private val breachLoader: BreachLoader,
    private val darkWebMonitoringManager: DarkWebMonitoringManager,
    private val breachDataHelper: BreachDataHelper,
    private val appEvents: AppEvents
) : BaseDataProvider<DarkWebMonitoringContract.Presenter>(), DarkWebMonitoringContract.DataProvider {

    private val appEventListener = AppEventsListener(appEvents, this)

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
        appEventListener.listen()
    }

    override fun unlistenForChanges() {
        appEventListener.unlisten()
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