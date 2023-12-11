package com.dashlane.createaccount.passwordless.pincodesetup

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinSetupViewModel @Inject constructor() : ViewModel() {

    companion object {
        const val PIN_LENGTH = 4
        const val TRANSITION_DELAY_MS = 100L
        const val CLEAR_PIN_VALUE = ""
    }

    private val _uiState = MutableStateFlow<PinSetupState>(
        PinSetupState.Default(CLEAR_PIN_VALUE)
    )
    val uiState: StateFlow<PinSetupState> = _uiState.asStateFlow()

    fun onPinUpdated(newPin: String) {
        viewModelScope.launch {
            val pinCode = sanitizePin(newPin)

            when (val state = uiState.value) {
                is PinSetupState.Choose -> handlePinSelection(pinCode)
                is PinSetupState.Confirm -> handlePinConfirmation(
                    pinCode = pinCode,
                    chosenPin = state.chosenPin
                )

                else -> Unit
            }
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun handlePinSelection(pinCode: String) {
        if (pinCode.length == PIN_LENGTH) {
            _uiState.emit(PinSetupState.Transition(pinCode))
            delay(TRANSITION_DELAY_MS)
            _uiState.emit(
                PinSetupState.Confirm(
                    chosenPin = pinCode,
                    pinCode = CLEAR_PIN_VALUE
                )
            )
        } else {
            _uiState.emit(
                PinSetupState.Choose(pinCode)
            )
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun handlePinConfirmation(
        pinCode: String,
        chosenPin: String
    ) {
        if (pinCode.length == PIN_LENGTH) {
            if (pinCode == chosenPin) {
                _uiState.emit(
                    PinSetupState.Transition(pinCode)
                )
                delay(TRANSITION_DELAY_MS)
                _uiState.emit(
                    PinSetupState.GoToNext(pinCode)
                )
            } else {
                _uiState.emit(
                    PinSetupState.Choose(
                        pinCode = CLEAR_PIN_VALUE,
                        hasError = true
                    )
                )
            }
        } else {
            _uiState.emit(
                PinSetupState.Confirm(
                    pinCode = pinCode,
                    chosenPin = chosenPin
                )
            )
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

    fun hasNavigated() {
        viewModelScope.launch {
            _uiState.emit(
                PinSetupState.Default(CLEAR_PIN_VALUE)
            )
        }
    }

    fun onViewStarted() {
        viewModelScope.launch {
            if (_uiState.value is PinSetupState.Default) {
                _uiState.emit(
                    PinSetupState.Choose(CLEAR_PIN_VALUE)
                )
            }
        }
    }
}
