package com.dashlane.security.identitydashboard

import com.dashlane.events.AppEvents
import com.dashlane.events.BreachStatusChangedEvent
import com.dashlane.events.clearLastEvent
import com.dashlane.events.register
import com.dashlane.events.unregister
import com.dashlane.security.identitydashboard.password.AuthentifiantSecurityEvaluator
import com.dashlane.server.api.endpoints.premium.PremiumStatus.PremiumCapability.Capability
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.featureflipping.UserFeaturesChecker
import com.skocken.presentation.provider.BaseDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.lang.ref.WeakReference
import javax.inject.Inject

class IdentityDashboardDataProvider @Inject constructor(
    private val userFeaturesChecker: UserFeaturesChecker,
    private val authentifiantSecurityEvaluator: AuthentifiantSecurityEvaluator,
    private val appEvents: AppEvents,
    private val currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter
) : BaseDataProvider<IdentityDashboardContract.Presenter>(),
    IdentityDashboardContract.DataProvider {

    lateinit var coroutineScope: CoroutineScope
    private val appEventListener = AppEventsListener(appEvents, this)
    private var latestSecurityScoreEvaluatorResult: Deferred<AuthentifiantSecurityEvaluator.Result?>? = null

    override fun hasProtectionPackage(): Boolean {
        return userFeaturesChecker.has(Capability.CREDITMONITORING)
    }

    override fun shouldIdentityRestorationBeVisible(): Boolean {
        return userFeaturesChecker.has(Capability.IDENTITYRESTORATION)
    }

    override fun getAuthentifiantsSecurityInfoAsync(forceRefresh: Boolean): Deferred<AuthentifiantSecurityEvaluator.Result?> {
        if (forceRefresh) {
            latestSecurityScoreEvaluatorResult = null
        }
        return latestSecurityScoreEvaluatorResult
            ?: coroutineScope.async(Dispatchers.Default) {
                getAuthentifiantSecurityInfoAsync()
            }.apply {
                latestSecurityScoreEvaluatorResult = this
            }
    }

    private suspend fun getAuthentifiantSecurityInfoAsync(): AuthentifiantSecurityEvaluator.Result {
        return authentifiantSecurityEvaluator.computeResult(currentTeamSpaceUiFilter.currentFilter.teamSpace)
    }

    override fun listenForChanges() {
        appEventListener.listen()
    }

    override fun unlistenForChanges() {
        appEventListener.unlisten()
    }

    private fun refreshUI() {
        presenter.requireRefresh(forceRefresh = false)
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