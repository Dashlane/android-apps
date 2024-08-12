package com.dashlane.login.monobucket

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.CallToAction
import com.dashlane.login.Device
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.server.api.endpoints.premium.PremiumStatus.PremiumCapability.Capability
import com.dashlane.session.SessionManager
import com.dashlane.ui.premium.inappbilling.service.StoreOffersCache
import com.dashlane.featureflipping.UserFeaturesChecker
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
    private val logRepository: LogRepository
) : ViewModel(), MonobucketViewModelContract {

    override val monobucketOwner: Device? = savedStateHandle.get<Device>(MonobucketActivity.EXTRA_BUCKET_OWNER)
    private val mutableState: MutableStateFlow<MonobucketState> = MutableStateFlow(MonobucketState.Idle)
    override val state = mutableState.asStateFlow()

    init {
        
        storeOffersCache.prefetchProductsForCurrentUser(viewModelScope)
    }

    override fun hasSync(): Boolean {
        val userHasSync = userFeaturesChecker.has(Capability.SYNC)
        userPreferencesManager.isOnLoginPaywall = !userHasSync
        return userHasSync
    }

    override fun onUpgradePremium() {
        logUserActionStep1(chosenAction = CallToAction.ALL_OFFERS)
    }

    override fun onUnlinkPreviousDevice() {
        logUserActionStep1(chosenAction = CallToAction.UNLINK)
    }

    override fun onConfirmUnregisterDevice() {
        logUserActionStep2(chosenAction = CallToAction.UNLINK)

        userPreferencesManager.ukiRequiresMonobucketConfirmation = false
        userPreferencesManager.isOnLoginPaywall = false
        mutableState.tryEmit(MonobucketState.ConfirmUnregisterDevice)
    }

    override fun onCancelUnregisterDevice() {
        logUserActionStep2(chosenAction = null)
        mutableState.tryEmit(MonobucketState.CanceledUnregisterDevice)
    }

    override fun onLogOut() {
        logUserActionStep1(chosenAction = null)
        viewModelScope.launch {
            userPreferencesManager.isOnLoginPaywall = false
            sessionManager.session?.let { sessionManager.destroySession(it, false) }
            mutableState.tryEmit(MonobucketState.UserLoggedOut)
        }
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
