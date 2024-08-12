package com.dashlane.pin.settings.success

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.pin.PinSetupRepository
import com.dashlane.pin.settings.PinSettingsNavigation
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PinSettingSuccessViewModel @Inject constructor(
    private val pinSetupRepository: PinSetupRepository,
    private val sessionManager: SessionManager,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val obfuscatedPin = checkNotNull(savedStateHandle.get<ObfuscatedByteArray>(PinSettingsNavigation.PIN_KEY))

    private val stateFlow: MutableStateFlow<PinSettingsSuccessState> = MutableStateFlow(PinSettingsSuccessState())
    private val navigationFlow: Channel<PinSettingsSuccessNavigationState> = Channel()

    val uiState: StateFlow<PinSettingsSuccessState> = stateFlow.asStateFlow()
    val navigationState: Flow<PinSettingsSuccessNavigationState> = navigationFlow.receiveAsFlow()

    fun viewStarted() {
        viewModelScope.launch {
            val session = sessionManager.session ?: return@launch
            val storedMP = sessionCredentialsSaver.areCredentialsSaved(session.username)
            if (storedMP) {
                
                savePin()
                return@launch
            }
            stateFlow.update { state -> state.copy(isMPStoreDialogShown = true) }
        }
    }

    fun onContinue() {
        viewModelScope.launch {
            savePin()
        }
    }

    fun cancel() {
        viewModelScope.launch {
            navigationFlow.send(PinSettingsSuccessNavigationState.Cancel)
            stateFlow.update { state -> state.copy(isMPStoreDialogShown = false) }
        }
    }

    private suspend fun savePin() {
        val pin = obfuscatedPin.decodeUtf8ToString()
        pinSetupRepository.savePinValue(pin)
        navigationFlow.send(PinSettingsSuccessNavigationState.Success)
        stateFlow.update { state -> state.copy(isMPStoreDialogShown = false) }
    }
}