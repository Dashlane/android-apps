package com.dashlane.createaccount.passwordless.pincodesetup

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.security.SecurityHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PinSetupViewModel @Inject constructor(
    private val securityHelper: SecurityHelper
) : ViewModel() {

    companion object {
        const val PIN_LENGTH = 4
        const val CLEAR_PIN_VALUE = ""
    }

    private val mutableStateFlow: MutableStateFlow<PinSetupState> = MutableStateFlow(PinSetupState.Initial(PinSetupData(CLEAR_PIN_VALUE)))
    val uiState: StateFlow<PinSetupState> = mutableStateFlow.asStateFlow()

    fun onViewResumed() {
        viewModelScope.launch {
            val isSystemLockSetup = securityHelper.isDeviceSecured()
            mutableStateFlow.emit(PinSetupState.Initial(uiState.value.data.copy(isSystemLockSetup = isSystemLockSetup)))
        }
    }

    fun hasNavigated() {
        viewModelScope.launch {
            mutableStateFlow.emit(
                PinSetupState.Initial(
                    uiState.value.data.copy(
                        pinCode = CLEAR_PIN_VALUE,
                        chosenPin = CLEAR_PIN_VALUE,
                        confirming = false
                    )
                )
            )
        }
    }

    fun onGoToSystemLockSetting() {
        viewModelScope.launch {
            securityHelper.intentHelper.findEnableDeviceLockIntent()?.let { intent ->
                mutableStateFlow.emit(PinSetupState.GoToSystemLockSetting(uiState.value.data, intent))
            }
        }
    }

    fun onPinUpdated(newPin: String) {
        viewModelScope.launch {
            val pinCode = sanitizePin(newPin)
            val data = uiState.value.data

            when (uiState.value.data.confirming) {
                false -> handlePinSelection(pinCode = pinCode)
                true -> handlePinConfirmation(
                    pinCode = pinCode,
                    chosenPin = data.chosenPin
                )
            }
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun handlePinSelection(pinCode: String) {
        val data = uiState.value.data
        if (pinCode.length == PIN_LENGTH) {
            mutableStateFlow.emit(PinSetupState.PinUpdated(data.copy(pinCode = CLEAR_PIN_VALUE, confirming = true, chosenPin = pinCode)))
        } else {
            mutableStateFlow.emit(PinSetupState.PinUpdated(data.copy(pinCode = pinCode)))
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
                mutableStateFlow.emit(PinSetupState.GoToNext(data.copy(pinCode = pinCode)))
            } else {
                mutableStateFlow.emit(PinSetupState.PinUpdated(data.copy(pinCode = CLEAR_PIN_VALUE), hasError = true))
            }
        } else {
            mutableStateFlow.emit(PinSetupState.PinUpdated(data.copy(pinCode = pinCode, chosenPin = chosenPin)))
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
