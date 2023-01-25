package com.dashlane.login.monobucket

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.CallToAction
import com.dashlane.login.Device
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.ui.premium.inappbilling.service.StoreOffersCache
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.userfeatures.UserFeaturesChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.dashlane.hermes.generated.events.user.CallToAction as UserCallToAction

@HiltViewModel
class MonobucketViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userPreferencesManager: UserPreferencesManager,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val sessionManager: SessionManager,
    storeOffersCache: StoreOffersCache,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val logRepository: LogRepository
) : ViewModel(), MonobucketViewModelContract {

    override val monobucketOwner: Device? = savedStateHandle.get<Device>(MonobucketActivity.EXTRA_BUCKET_OWNER)
    private val mutableState: MutableStateFlow<MonobucketState> = MutableStateFlow(MonobucketState.Idle)
    override val state = mutableState.asStateFlow()

    init {
        
        storeOffersCache.prefetchProductsForCurrentUser(viewModelScope)
    }

    override fun hasSync(): Boolean {
        val userHasSync = userFeaturesChecker.has(UserFeaturesChecker.Capability.SYNC)
        userPreferencesManager.isOnLoginPaywall = !userHasSync
        return userHasSync
    }

    override fun onUpgradePremium() {
        sendUsageLog("see_premium")
        logUserActionStep1(chosenAction = CallToAction.ALL_OFFERS)
    }

    override fun onUnlinkPreviousDevice() {
        sendUsageLog("unlink_previous")
        logUserActionStep1(chosenAction = CallToAction.UNLINK)
    }

    override fun onConfirmUnregisterDevice() {
        sendConfirmationUsageLog("unlink")
        logUserActionStep2(chosenAction = CallToAction.UNLINK)

        userPreferencesManager.ukiRequiresMonobucketConfirmation = false
        userPreferencesManager.isOnLoginPaywall = false
        mutableState.tryEmit(MonobucketState.ConfirmUnregisterDevice)
    }

    override fun onCancelUnregisterDevice() {
        sendConfirmationUsageLog("cancel_unlink")
        logUserActionStep2(chosenAction = null)
        mutableState.tryEmit(MonobucketState.CanceledUnregisterDevice)
    }

    override fun onLogOut() {
        sendUsageLog("logout")
        logUserActionStep1(chosenAction = null)
        viewModelScope.launch {
            userPreferencesManager.isOnLoginPaywall = false
            sessionManager.session?.let { sessionManager.destroySession(it, false) }
            mutableState.tryEmit(MonobucketState.UserLoggedOut)
        }
    }

    override fun onShow() = sendUsageLog("seen")

    private fun sendUsageLog(action: String) = sendUsageLog("mono_menu", action)

    private fun sendConfirmationUsageLog(action: String) = sendUsageLog("unlink_menu", action)

    private fun sendUsageLog(subtype: String, action: String) {
        bySessionUsageLogRepository[sessionManager.session]?.enqueue(
            UsageLogCode75(
                type = "mono_test",
                subtype = subtype,
                action = action
            )
        )
    }

    private fun logUserActionStep1(chosenAction: CallToAction?) =
        logRepository.queueEvent(
            UserCallToAction(
                callToActionList = listOf(CallToAction.ALL_OFFERS, CallToAction.UNLINK),
                hasChosenNoAction = chosenAction == null,
                chosenAction = chosenAction
            )
        )

    private fun logUserActionStep2(chosenAction: CallToAction?) =
        logRepository.queueEvent(
            UserCallToAction(
                callToActionList = listOf(CallToAction.UNLINK),
                hasChosenNoAction = chosenAction == null,
                chosenAction = chosenAction
            )
        )
}
