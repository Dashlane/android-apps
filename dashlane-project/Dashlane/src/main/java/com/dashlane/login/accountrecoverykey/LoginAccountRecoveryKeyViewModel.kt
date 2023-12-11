package com.dashlane.login.accountrecoverykey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.toObfuscated
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.events.user.UseAccountRecoveryKey
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class LoginAccountRecoveryKeyViewModel @Inject constructor(
    private val loginAccountRecoveryKeyRepository: LoginAccountRecoveryKeyRepository,
    private val logRepository: LogRepository
) : ViewModel() {

    fun onBackPressed() {
        logRepository.queueEvent(UseAccountRecoveryKey(FlowStep.CANCEL))
    }

    fun deviceRegistered(registeredUserDevice: RegisteredUserDevice, authTicket: String) {
        viewModelScope.launch {
            loginAccountRecoveryKeyRepository.updateRegisteredDevice(registeredUserDevice, authTicket)
        }
    }

    fun vaultKeyDecrypted(obfuscatedVaultKey: ObfuscatedByteArray) {
        viewModelScope.launch {
            loginAccountRecoveryKeyRepository.updateObfuscatedVaultKey(obfuscatedVaultKey)
        }
    }

    fun masterPasswordChanged(newMasterPassword: ObfuscatedByteArray) {
        viewModelScope.launch {
            loginAccountRecoveryKeyRepository.updateNewMasterPassword(newMasterPassword)
        }
    }

    fun pinSetup(pin: String) {
        viewModelScope.launch {
            loginAccountRecoveryKeyRepository.updatePin(pin.encodeToByteArray().toObfuscated())
        }
    }

    fun onSkipBiometric() {
        viewModelScope.launch {
            loginAccountRecoveryKeyRepository.updateBiometricEnabled(false)
        }
    }

    fun onEnableBiometrics() {
        viewModelScope.launch {
            loginAccountRecoveryKeyRepository.updateBiometricEnabled(true)
        }
    }
}
