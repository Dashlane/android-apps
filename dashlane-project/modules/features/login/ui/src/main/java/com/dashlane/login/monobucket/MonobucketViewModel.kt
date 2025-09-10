package com.dashlane.login.monobucket

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.data.login.Device
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.CallToAction
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.preference.PreferencesManager
import com.dashlane.premium.StoreOffersCache
import com.dashlane.server.api.endpoints.premium.PremiumStatus.PremiumCapability.Capability
import com.dashlane.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonobucketViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val preferencesManager: PreferencesManager,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val sessionManager: SessionManager,
    private val storeOffersCache: StoreOffersCache,
    private val logRepository: LogRepository,
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
            MonobucketUseCase.logUserActionStep1(logRepository, chosenAction = CallToAction.ALL_OFFERS)
            _stateFlow.send(MonobucketState.SideEffect.Premium)
        }
    }

    fun unlinkPreviousDevice() {
        viewModelScope.launch {
            MonobucketUseCase.logUserActionStep1(logRepository, chosenAction = CallToAction.UNLINK)
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
            MonobucketUseCase.confirmUnregisterDevice(logRepository, preferencesManager, sessionManager)
            _stateFlow.send(MonobucketState.SideEffect.ConfirmUnregisterDevice)
        }
    }

    fun onLogOut() {
        viewModelScope.launch {
            MonobucketUseCase.logout(logRepository, preferencesManager, sessionManager)
            _stateFlow.send(MonobucketState.SideEffect.UserLoggedOut)
        }
    }
}