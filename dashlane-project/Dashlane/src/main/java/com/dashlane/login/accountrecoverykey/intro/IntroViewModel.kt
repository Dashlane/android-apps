package com.dashlane.login.accountrecoverykey.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.account.UserAccountInfo
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.definitions.UseKeyErrorName
import com.dashlane.hermes.generated.events.user.UseAccountRecoveryKey
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryKeyRepository
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@HiltViewModel
class IntroViewModel @Inject constructor(
    private val loginAccountRecoveryKeyRepository: LoginAccountRecoveryKeyRepository,
    private val logRepository: LogRepository,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val stateFlow = MutableStateFlow<IntroState>(IntroState.Initial)
    val uiState = stateFlow.asStateFlow()

    fun hasNavigated() {
        viewModelScope.launch { stateFlow.emit(IntroState.Initial) }
    }

    fun arkFlowStarted(registeredUserDevice: RegisteredUserDevice, authTicket: String?, accountType: UserAccountInfo.AccountType) {
        viewModelScope.launch {
            if (stateFlow.value !is IntroState.Initial) return@launch

            logRepository.queueEvent(UseAccountRecoveryKey(flowStep = FlowStep.START))
            loginAccountRecoveryKeyRepository.updateAccountType(accountType)

            if (authTicket != null && registeredUserDevice is RegisteredUserDevice.Remote) {
                loginAccountRecoveryKeyRepository.updateRegisteredDevice(registeredUserDevice, authTicket)
                stateFlow.emit(IntroState.GoToARK(authTicket = authTicket))
            } else {
                checkUserDeviceStatus(registeredUserDevice)
            }
        }
    }

    fun retry(registeredUserDevice: RegisteredUserDevice) {
        checkUserDeviceStatus(registeredUserDevice)
    }

    private fun checkUserDeviceStatus(registeredUserDevice: RegisteredUserDevice) {
        flow {
            when (registeredUserDevice) {
                is RegisteredUserDevice.Remote -> Unit 
                is RegisteredUserDevice.ToRestore -> throw IllegalStateException("Restore is not supported")
                is RegisteredUserDevice.Local -> {
                    when (loginAccountRecoveryKeyRepository.get2FAStatusForRecovery(registeredUserDevice)) {
                        AuthSecurityType.TOTP_LOGIN,
                        AuthSecurityType.TOTP_DEVICE_REGISTRATION -> emit(IntroState.GoToTOTP)
                        AuthSecurityType.EMAIL_TOKEN -> emit(IntroState.GoToToken)
                        AuthSecurityType.SSO -> throw IllegalStateException("SSO does not support ARK")
                    }
                }
            }
        }
            .flowOn(ioDispatcher)
            .catch {
                logRepository.queueEvent(UseAccountRecoveryKey(flowStep = FlowStep.ERROR, useKeyErrorName = UseKeyErrorName.UNKNOWN))
                emit(IntroState.Error)
            }
            .onStart { emit(IntroState.Loading) }
            .onEach { stateFlow.emit(it) }
            .launchIn(viewModelScope)
    }
}
