package com.dashlane.login.pages.secrettransfer.confirmemail

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.dashlane.account.UserAccountStorage
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.SecurityFeature
import com.dashlane.authentication.UnauthenticatedUser
import com.dashlane.authentication.login.AuthenticationEmailRepository
import com.dashlane.authentication.login.AuthenticationSecretTransferRepository
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferDestination.ConfirmEmail
import com.dashlane.login.root.LoginRepository
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import com.dashlane.server.api.endpoints.authentication.RemoteKey
import com.dashlane.user.UserSecuritySettings
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfirmEmailViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val userAccountStorage: UserAccountStorage,
    private val authenticationSecretTransferRepository: AuthenticationSecretTransferRepository,
    private val authenticationEmailRepository: AuthenticationEmailRepository,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val login = savedStateHandle.toRoute<ConfirmEmail>().email
    private val stateFlow = MutableStateFlow<ConfirmEmailState>(ConfirmEmailState.ConfirmEmail(ConfirmEmailData(login)))

    val uiState = stateFlow.asStateFlow()

    fun emailConfirmed(secretTransferPayload: SecretTransferPayload) {
        verifyAccountType(secretTransferPayload)
    }

    fun cancel() {
        viewModelScope.launch { stateFlow.emit(ConfirmEmailState.Cancelled(data = stateFlow.value.data)) }
    }

    fun hasNavigated() {
        viewModelScope.launch { stateFlow.emit(ConfirmEmailState.ConfirmEmail(data = stateFlow.value.data)) }
    }

    fun cancelOnError() {
        viewModelScope.launch { stateFlow.emit(ConfirmEmailState.Cancelled(data = stateFlow.value.data)) }
    }

    fun retry(secretTransferPayload: SecretTransferPayload) {
        verifyAccountType(secretTransferPayload)
    }

    @VisibleForTesting
    fun verifyAccountType(secretTransferPayload: SecretTransferPayload) {
        flow {
            when (secretTransferPayload.vaultKey.type) {
                SecretTransferPayload.Type.MASTER_PASSWORD -> {
                    when (val secondFactor = getAuthenticationSecondFactor(secretTransferPayload.login)) {
                        is AuthenticationSecondFactor.EmailToken -> {
                            val registeredUserDevice = registrationWithAuthTicket(
                                login = secretTransferPayload.login,
                                token = secretTransferPayload.token ?: throw IllegalStateException(),
                                securityFeatures = secondFactor.securityFeatures,
                                remoteKeyType = RemoteKey.Type.MASTER_PASSWORD
                            )
                            loginRepository.updateRegisteredUserDevice(registeredUserDevice)
                            emit(ConfirmEmailState.RegisterSuccess(stateFlow.value.data))
                        }

                        is AuthenticationSecondFactor.Totp -> {
                            emit(ConfirmEmailState.AskForTOTP(stateFlow.value.data))
                        }

                        else -> throw IllegalStateException("Invalid second factor")
                    }
                }

                SecretTransferPayload.Type.INVISIBLE_MASTER_PASSWORD -> {
                    val registeredUserDevice = registrationWithAuthTicket(
                        login = secretTransferPayload.login,
                        token = secretTransferPayload.token ?: throw IllegalStateException(),
                        securityFeatures = emptySet(),
                        remoteKeyType = RemoteKey.Type.MASTER_PASSWORD
                    )
                    loginRepository.updateRegisteredUserDevice(registeredUserDevice)
                    emit(ConfirmEmailState.RegisterSuccess(stateFlow.value.data))
                }

                SecretTransferPayload.Type.SSO -> {
                    val registeredUserDevice = registrationWithAuthTicket(
                        login = secretTransferPayload.login,
                        token = secretTransferPayload.token ?: throw IllegalStateException(),
                        securityFeatures = setOf(SecurityFeature.SSO),
                        remoteKeyType = RemoteKey.Type.SSO
                    )
                    loginRepository.updateRegisteredUserDevice(registeredUserDevice)
                    emit(ConfirmEmailState.RegisterSuccess(stateFlow.value.data))
                }
            }
        }
            .catch {
                stateFlow.emit(ConfirmEmailState.Error(stateFlow.value.data))
            }
            .onEach { state -> stateFlow.emit(state) }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    suspend fun registrationWithAuthTicket(
        login: String,
        token: String,
        securityFeatures: Set<SecurityFeature>,
        remoteKeyType: RemoteKey.Type
    ): RegisteredUserDevice.Remote {
        userAccountStorage.saveSecuritySettings(username = login, securitySettings = UserSecuritySettings(isToken = true))
        return authenticationSecretTransferRepository.register(
            login = login,
            securityFeatures = securityFeatures,
            token = token,
            remoteKeyType = remoteKeyType
        )
    }

    @VisibleForTesting
    suspend fun getAuthenticationSecondFactor(login: String): AuthenticationSecondFactor? {
        return when (val result = authenticationEmailRepository.getUserStatus(UnauthenticatedUser(login))) {
            is AuthenticationEmailRepository.Result.RequiresDeviceRegistration.SecondFactor -> result.secondFactor
            else -> null
        }
    }
}
