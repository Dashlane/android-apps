package com.dashlane.pin.setup

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.security.SecurityHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PinSetupViewModel @Inject constructor(
    private val securityHelper: SecurityHelper,
) : ViewModel() {

    companion object {
        const val PIN_LENGTH = 4
        const val CLEAR_PIN_VALUE = ""
    }

    private val stateFlow: MutableStateFlow<PinSetupState> = MutableStateFlow(PinSetupState.Initial(PinSetupData(CLEAR_PIN_VALUE)))
    private val navigationFlow: Channel<PinSetupNavigationState> = Channel()

    val uiState: StateFlow<PinSetupState> = stateFlow.asStateFlow()
    val navigationState: Flow<PinSetupNavigationState> = navigationFlow.receiveAsFlow()

    fun onViewResumed(isCancellable: Boolean) {
        viewModelScope.launch {
            val isSystemLockSetup = securityHelper.isDeviceSecured()
            stateFlow.update { PinSetupState.Initial(uiState.value.data.copy(isCancellable = isCancellable, isSystemLockSetup = isSystemLockSetup)) }
        }
    }

    fun onGoToSystemLockSetting() {
        viewModelScope.launch {
            securityHelper.intentHelper.findEnableDeviceLockIntent()?.let { intent ->
                navigationFlow.send(PinSetupNavigationState.GoToSystemLockSetting(intent))
            }
        }
    }

    fun onPinUpdated(newPin: String) {
        viewModelScope.launch {
            val pinCode = sanitizePin(newPin)
            val data = uiState.value.data

            when (uiState.value.data.confirming) {
                false -> handlePinSelection(pinCode = pinCode)
                true -> {
                    handlePinConfirmation(
                        pinCode = pinCode,
                        chosenPin = data.chosenPin
                    )
                }
            }
        }
    }

    fun cancel() {
        viewModelScope.launch {
            navigationFlow.send(PinSetupNavigationState.Cancel)
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun handlePinSelection(pinCode: String) {
        val data = uiState.value.data
        if (pinCode.length == PIN_LENGTH) {
            stateFlow.update { PinSetupState.PinUpdated(data.copy(pinCode = pinCode)) }
            delay(200) 
            stateFlow.update { PinSetupState.PinUpdated(data.copy(pinCode = CLEAR_PIN_VALUE, confirming = true, chosenPin = pinCode)) }
        } else {
            stateFlow.update { PinSetupState.PinUpdated(data.copy(pinCode = pinCode)) }
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun handlePinConfirmation(
        pinCode: String,
        chosenPin: String
    ) {
        val data = uiState.value.data
        if (pinCode.length == PIN_LENGTH) {
            if (pinCode == chosenPin) {
                stateFlow.update { PinSetupState.PinUpdated(data.copy(pinCode = pinCode, chosenPin = chosenPin)) }
                delay(200) 
                navigationFlow.send(PinSetupNavigationState.GoToNext(pinCode))
            } else {
                stateFlow.update { PinSetupState.PinUpdated(data.copy(pinCode = CLEAR_PIN_VALUE), hasError = true) }
            }
        } else {
            stateFlow.update { PinSetupState.PinUpdated(data.copy(pinCode = pinCode, chosenPin = chosenPin)) }
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    fun sanitizePin(newPin: String): String {
        if (newPin.toIntOrNull() == null) {
            return CLEAR_PIN_VALUE
        }

        return if (newPin.length > PIN_LENGTH) {
            newPin.take(PIN_LENGTH)
        } else {
            newPin
        }
    }
}
