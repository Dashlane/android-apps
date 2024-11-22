package com.dashlane.login.monobucket

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.CallToAction
import com.dashlane.login.Device
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.endpoints.premium.PremiumStatus.PremiumCapability.Capability
import com.dashlane.session.SessionManager
import com.dashlane.premium.StoreOffersCache
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.dashlane.hermes.generated.events.user.CallToAction as UserCallToAction

@HiltViewModel
class MonobucketViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val preferencesManager: PreferencesManager,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val sessionManager: SessionManager,
    private val storeOffersCache: StoreOffersCache,
    private val logRepository: LogRepository
) : ViewModel() {

    private val _stateFlow = MutableViewStateFlow<MonobucketState.View, MonobucketState.SideEffect>(MonobucketState.View())
    val stateFlow: ViewStateFlow<MonobucketState.View, MonobucketState.SideEffect> = _stateFlow

    fun viewStarted() {
        viewModelScope.launch {
            if (userFeaturesChecker.has(Capability.SYNC)) {
                preferencesManager[sessionManager.session?.username].isOnLoginPaywall = false
                _stateFlow.send(MonobucketState.SideEffect.HasSync)
            } else {
                
                storeOffersCache.prefetchProductsForCurrentUser(viewModelScope)
                val device: Device? = savedStateHandle[MonobucketActivity.EXTRA_BUCKET_OWNER]
                _stateFlow.update { state -> state.copy(device = device) }
            }
        }
    }

    fun onUpgradePremium() {
        viewModelScope.launch {
            logUserActionStep1(chosenAction = CallToAction.ALL_OFFERS)
            _stateFlow.send(MonobucketState.SideEffect.Premium)
        }
    }

    fun unlinkPreviousDevice() {
        viewModelScope.launch {
            logUserActionStep1(chosenAction = CallToAction.UNLINK)
            _stateFlow.update { state -> state.copy(showPreviousDeviceDialog = true) }
        }
    }

    fun bottomSheetDismissed() {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(showPreviousDeviceDialog = false) }
        }
    }

    fun onConfirmUnregisterDevice() {
        viewModelScope.launch {
            logUserActionStep2(chosenAction = CallToAction.UNLINK)
            preferencesManager[sessionManager.session?.username].run {
                ukiRequiresMonobucketConfirmation = false
                isOnLoginPaywall = false
            }
            _stateFlow.send(MonobucketState.SideEffect.ConfirmUnregisterDevice)
        }
    }

    fun onLogOut() {
        viewModelScope.launch {
            logUserActionStep1(chosenAction = null)
            preferencesManager[sessionManager.session?.username].isOnLoginPaywall = false
            sessionManager.session?.let { sessionManager.destroySession(it, false) }
            _stateFlow.send(MonobucketState.SideEffect.UserLoggedOut)
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
