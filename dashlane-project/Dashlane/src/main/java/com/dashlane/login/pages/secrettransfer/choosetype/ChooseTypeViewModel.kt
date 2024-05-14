package com.dashlane.login.pages.secrettransfer.choosetype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.hermes.generated.definitions.DeviceSelected
import com.dashlane.hermes.generated.definitions.TransferMethod
import com.dashlane.secrettransfer.domain.SecretTransferAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ChooseTypeViewModel @Inject constructor(
    private val secretTransferAnalytics: SecretTransferAnalytics
) : ViewModel() {

    private val stateFlow = MutableStateFlow<ChooseTypeState>(ChooseTypeState.Initial(ChooseTypeData()))

    val uiState = stateFlow.asStateFlow()

    fun viewStarted(email: String?) {
        viewModelScope.launch {
            if (email != null) stateFlow.emit(ChooseTypeState.Initial(stateFlow.value.data.copy(email = email)))
        }
    }

    fun viewNavigated() {
        viewModelScope.launch { stateFlow.emit(ChooseTypeState.Initial(stateFlow.value.data)) }
    }

    fun computerClicked() {
        secretTransferAnalytics.selectTransferMethod(transferMethod = TransferMethod.SECURITY_CHALLENGE, deviceSelected = DeviceSelected.COMPUTER)
        viewModelScope.launch { stateFlow.emit(ChooseTypeState.GoToUniversal(stateFlow.value.data)) }
    }

    fun mobileClicked() {
        secretTransferAnalytics.selectTransferMethod(transferMethod = TransferMethod.QR_CODE, deviceSelected = DeviceSelected.MOBILE)
        viewModelScope.launch { stateFlow.emit(ChooseTypeState.GoToQR(stateFlow.value.data)) }
    }

    fun helpClicked() {
        viewModelScope.launch { stateFlow.emit(ChooseTypeState.GoToHelp(stateFlow.value.data)) }
    }
}
