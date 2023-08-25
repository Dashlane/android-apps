package com.dashlane.login.devicelimit

import android.app.Activity
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.CallToAction
import com.dashlane.login.LoginIntents
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.premium.offer.list.view.OfferListFragment
import com.dashlane.premium.offer.list.view.OfferListFragment.Companion.ORIGIN_DEVICE_LIMIT
import com.dashlane.premium.offer.list.view.OffersActivity
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.ui.premium.inappbilling.service.StoreOffersCache
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.util.setCurrentPageView
import com.dashlane.util.startActivity
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.UserFeaturesChecker.Capability.DEVICES_LIMIT
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.dashlane.hermes.generated.events.user.CallToAction as UserCallToAction

class DeviceLimitPresenter @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    private val activity: Activity,
    private val sessionManager: SessionManager,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val userPreferencesManager: UserPreferencesManager,
    private val logRepository: LogRepository,
    storeOffersCache: StoreOffersCache
) : DeviceLimitContract.Presenter {

    init {
        
        storeOffersCache.prefetchProductsForCurrentUser(applicationCoroutineScope)
    }

    override fun onStart() {
        userPreferencesManager.isOnLoginPaywall = userFeaturesChecker.has(DEVICES_LIMIT)
        if (!userFeaturesChecker.has(DEVICES_LIMIT)) {
            activity.startActivity(LoginIntents.createProgressActivityIntent(activity))
            activity.finish()
        }
    }

    override fun onUpgradePremium() {
        sendUsageLog("see_premium")
        logUserAction(chosenAction = CallToAction.ALL_OFFERS)
        activity.startActivity<OffersActivity> {
            putExtra(OfferListFragment.EXTRA_ORIGIN, ORIGIN_DEVICE_LIMIT)
        }
    }

    override fun onUnlinkPreviousDevices() {
        sendUsageLog("unlink_previous")
        logUserAction(chosenAction = CallToAction.UNLINK)
        activity.startActivity<UnlinkDevicesActivity> {
            
            putExtras(activity.intent)
        }
    }

    override fun onLogOut() {
        sendUsageLog("logout")
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
        sendUsageLog("seen")
        activity.setCurrentPageView(page = AnyPage.PAYWALL_DEVICE_SYNC_LIMIT)
    }

    private fun sendUsageLog(action: String) {
        bySessionUsageLogRepository[sessionManager.session]
            ?.enqueue(
                UsageLogCode75(
                    type = "device_sync_limit",
                    subtype = "limit_prompt",
                    action = action
                )
            )
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
