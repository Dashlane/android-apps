package com.dashlane.login.devicelimit

import android.app.Activity
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.CallToAction
import com.dashlane.login.LoginIntents
import com.dashlane.preference.PreferencesManager
import com.dashlane.premium.offer.list.view.OffersActivity
import com.dashlane.server.api.endpoints.premium.PremiumStatus.PremiumCapability.Capability
import com.dashlane.session.SessionManager
import com.dashlane.premium.StoreOffersCache
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.util.setCurrentPageView
import com.dashlane.util.startActivity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.dashlane.hermes.generated.events.user.CallToAction as UserCallToAction

class DeviceLimitPresenter @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    private val activity: Activity,
    private val sessionManager: SessionManager,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val preferencesManager: PreferencesManager,
    private val logRepository: LogRepository,
    storeOffersCache: StoreOffersCache
) : DeviceLimitContract.Presenter {

    private val userPreferencesManager: UserPreferencesManager
        get() = preferencesManager[sessionManager.session?.username]

    init {
        
        storeOffersCache.prefetchProductsForCurrentUser(applicationCoroutineScope)
    }

    override fun onStart() {
        userPreferencesManager.isOnLoginPaywall = userFeaturesChecker.has(Capability.DEVICESLIMIT)
        if (!userFeaturesChecker.has(Capability.DEVICESLIMIT)) {
            activity.startActivity(LoginIntents.createProgressActivityIntent(activity))
            activity.finish()
        }
    }

    override fun onUpgradePremium() {
        logUserAction(chosenAction = CallToAction.ALL_OFFERS)
        activity.startActivity<OffersActivity>()
    }

    override fun onUnlinkPreviousDevices() {
        logUserAction(chosenAction = CallToAction.UNLINK)
        activity.startActivity<UnlinkDevicesActivity> {
            
            putExtras(activity.intent)
        }
    }

    override fun onLogOut() {
        logUserAction(chosenAction = null)
        applicationCoroutineScope.launch(mainCoroutineDispatcher) {
            userPreferencesManager.isOnLoginPaywall = false
            sessionManager.session?.let { session ->
                sessionManager.destroySession(session, false)
            }
            activity.startActivity(LoginIntents.createLoginActivityIntent(activity))
        }
    }

    override fun onShow() {
        activity.setCurrentPageView(page = AnyPage.PAYWALL_DEVICE_SYNC_LIMIT)
    }

    private fun logUserAction(chosenAction: CallToAction?) =
        logRepository.queueEvent(
            UserCallToAction(
                callToActionList = listOf(CallToAction.ALL_OFFERS, CallToAction.UNLINK),
                hasChosenNoAction = chosenAction == null,
                chosenAction = chosenAction
            )
        )
}
