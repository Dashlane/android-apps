package com.dashlane.login.accountrecoverykey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.toObfuscated
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.events.user.UseAccountRecoveryKey
import com.dashlane.user.UserAccountInfo
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

    fun updateAccountType(accountType: UserAccountInfo.AccountType) {
        viewModelScope.launch {
            loginAccountRecoveryKeyRepository.updateAccountType(accountType)
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
