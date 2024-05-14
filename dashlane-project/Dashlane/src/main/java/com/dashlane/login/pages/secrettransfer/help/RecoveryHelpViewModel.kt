package com.dashlane.login.pages.secrettransfer.help

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.hermes.generated.definitions.DeviceSelected
import com.dashlane.hermes.generated.definitions.TransferMethod
import com.dashlane.secrettransfer.domain.SecretTransferAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RecoveryHelpViewModel @Inject constructor(
    private val deviceInfoRepository: DeviceInfoRepository,
    private val secretTransferAnalytics: SecretTransferAnalytics
) : ViewModel() {

    private val stateFlow = MutableStateFlow<RecoveryHelpState>(RecoveryHelpState.Initial(RecoveryHelpData()))
    val uiState = stateFlow.asStateFlow()

    fun viewStarted(email: String?) {
        viewModelScope.launch {
            if (email != null) stateFlow.emit(RecoveryHelpState.Initial(stateFlow.value.data.copy(email = email)))
        }
    }

    fun viewNavigated() {
        viewModelScope.launch { stateFlow.emit(RecoveryHelpState.Initial(stateFlow.value.data)) }
    }

    fun arkClicked() {
        secretTransferAnalytics.selectTransferMethod(
            transferMethod = TransferMethod.ACCOUNT_RECOVERY_KEY,
            deviceSelected = DeviceSelected.NO_DEVICE_AVAILABLE
        )

        viewModelScope.launch {
            val email = stateFlow.value.data.email ?: return@launch
            val accessKey = deviceInfoRepository.deviceId ?: return@launch
            val registeredUserDevice = RegisteredUserDevice.Local(login = email, securityFeatures = emptySet(), accessKey = accessKey)
            stateFlow.emit(RecoveryHelpState.GoToARK(stateFlow.value.data, registeredUserDevice))
        }
    }

    fun lostKeyClicked() {
        viewModelScope.launch {
            stateFlow.emit(RecoveryHelpState.GoToLostKey(stateFlow.value.data))
        }
    }
}