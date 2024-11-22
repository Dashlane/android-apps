package com.dashlane.login.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.hardwaresecurity.BiometricAuthModule
import com.dashlane.hermes.LogRepository
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockType
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.ui.screens.settings.UserSettingsLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginSettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val biometricRecovery: BiometricRecovery,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val lockManager: LockManager,
    private val biometricAuthModule: BiometricAuthModule,
    private val logRepository: LogRepository,
    private val userSettingsLogRepository: UserSettingsLogRepository
) : ViewModel() {
    private val _stateFlow = MutableViewStateFlow<LoginSettingsState.View, LoginSettingsState.SideEffect>(LoginSettingsState.View())
    val stateFlow: ViewStateFlow<LoginSettingsState.View, LoginSettingsState.SideEffect> = _stateFlow

    fun viewStarted() {
        viewModelScope.launch {
            if (!biometricAuthModule.isHardwareSetUp() || biometricAuthModule.isOnlyWeakSupported()) {
                _stateFlow.send(LoginSettingsState.SideEffect.Success)
            } else {
                _stateFlow.update { state -> state.copy(isLoading = false) }
            }
        }
    }

    fun onBiometricCheckedChange(checked: Boolean) {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(
                    biometricChecked = checked,
                    biometricRecoveryChecked = false,
                    snackBarShown = !checked && _stateFlow.value.biometricRecoveryChecked
                )
            }
        }
    }

    fun onBiometricRecoveryCheckedChange(checked: Boolean) {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(
                    biometricChecked = _stateFlow.value.biometricChecked || checked,
                    biometricRecoveryChecked = checked,
                    snackBarShown = checked && !_stateFlow.value.biometricChecked
                )
            }
        }
    }

    fun onHelpClicked() {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(helpShown = !_stateFlow.value.helpShown) }
        }
    }

    fun onNextClicked() {
        viewModelScope.launch {
            val session = sessionManager.session
            if (!_stateFlow.value.biometricChecked || session == null) {
                _stateFlow.send(LoginSettingsState.SideEffect.Success)
                return@launch
            }

            
            val result = biometricAuthModule.createEncryptionKeyForBiometrics(username = session.userId)
            if (!result) return@launch
            
            sessionCredentialsSaver.saveCredentials(session)
            lockManager.addLock(session.username, LockType.Biometric)

            if (_stateFlow.value.biometricRecoveryChecked) {
                
                biometricRecovery.isFeatureKnown = true
                biometricRecovery.setBiometricRecoveryFeatureEnabled(true)
            }

            logRepository.queueEvent(userSettingsLogRepository.get(session.username))

            _stateFlow.send(LoginSettingsState.SideEffect.Success)
        }
    }
}