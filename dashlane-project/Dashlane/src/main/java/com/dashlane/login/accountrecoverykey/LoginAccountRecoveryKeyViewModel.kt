package com.dashlane.login.accountrecoverykey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginAccountRecoveryKeyViewModel @Inject constructor(
    private val loginAccountRecoveryKeyRepository: LoginAccountRecoveryKeyRepository
) : ViewModel() {

    private val stateFlow = MutableStateFlow<LoginAccountRecoveryKeyState>(LoginAccountRecoveryKeyState.Initial(LoginAccountRecoveryKeyData()))
    val uiState = stateFlow.asStateFlow()

    fun arkFlowStarted(registeredUserDevice: RegisteredUserDevice, authTicket: String?) {
        viewModelScope.launch {
            if (authTicket != null) {
                stateFlow.emit(
                    LoginAccountRecoveryKeyState.GoToARK(
                        stateFlow.value.data.copy(registeredUserDevice = registeredUserDevice),
                        authTicket = authTicket
                    )
                )
            } else {
                checkUserDeviceStatus(registeredUserDevice)
            }
        }
    }

    fun deviceRegistered(registeredUserDevice: RegisteredUserDevice, authTicket: String) {
        viewModelScope.launch {
            stateFlow.emit(LoginAccountRecoveryKeyState.GoToARK(stateFlow.value.data.copy(registeredUserDevice = registeredUserDevice), authTicket))
        }
    }

    fun vaultKeyDecrypted(decryptedVaultKey: String) {
        viewModelScope.launch {
            stateFlow.emit(LoginAccountRecoveryKeyState.FinishWithSuccess(stateFlow.value.data, decryptedVaultKey))
        }
    }

    fun checkUserDeviceStatus(registeredUserDevice: RegisteredUserDevice) {
        flow {
            when (registeredUserDevice) {
                is RegisteredUserDevice.Remote -> Unit 
                is RegisteredUserDevice.ToRestore -> throw IllegalStateException("Restore is not supported")
                is RegisteredUserDevice.Local -> {
                    when (loginAccountRecoveryKeyRepository.get2FAStatusForRecovery(registeredUserDevice)) {
                        AuthSecurityType.TOTP_LOGIN,
                        AuthSecurityType.TOTP_DEVICE_REGISTRATION -> emit(LoginAccountRecoveryKeyState.GoToTOTP(stateFlow.value.data))
                        AuthSecurityType.EMAIL_TOKEN -> emit(LoginAccountRecoveryKeyState.GoToToken(stateFlow.value.data))
                        AuthSecurityType.SSO -> throw IllegalStateException("SSO does not support ARK")
                    }
                }
            }
        }
            .catch {
            }
            .onStart { emit(LoginAccountRecoveryKeyState.Loading(stateFlow.value.data)) }
            .onEach { stateFlow.emit(it) }
            .launchIn(viewModelScope)
    }
}
