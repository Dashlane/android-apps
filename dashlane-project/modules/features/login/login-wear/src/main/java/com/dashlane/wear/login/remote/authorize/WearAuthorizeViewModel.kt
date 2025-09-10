package com.dashlane.wear.login.remote.authorize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.login.AuthenticationPasswordRepository
import com.dashlane.data.login.RegisteredUserDevice
import com.dashlane.login.LoginStrategy
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import com.dashlane.secrettransfer.domain.toUserAccountInfoType
import com.dashlane.secrettransfer.loginPassword
import com.dashlane.session.Session
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionInitializer
import com.dashlane.session.SessionResult
import com.dashlane.session.SessionTrasher
import com.dashlane.user.Username
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.wear.login.WearLoginRepository
import com.dashlane.wear.login.remote.authorize.WearAuthorizeState.SideEffect
import com.dashlane.wear.login.remote.authorize.WearAuthorizeState.View
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class WearAuthorizeViewModel @Inject constructor(
    private val loginRepository: WearLoginRepository,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val sessionInitializer: SessionInitializer,
    private val sessionTrasher: SessionTrasher,
    private val authenticationPasswordRepository: AuthenticationPasswordRepository,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val loginStrategy: LoginStrategy,
) : ViewModel() {
    private val _stateFlow = MutableViewStateFlow<View, SideEffect>(View.Initial)
    val stateFlow: ViewStateFlow<View, SideEffect> = _stateFlow

    fun login() {
        flow<WearAuthorizeState> {
            val secretTransferPayload = loginRepository.getSecretTransferPayload()
                ?: throw WearAuthorizeException.EmptyPayloadException()
            val registeredUserDevice =
                loginRepository.getRegisteredUserDevice() as? RegisteredUserDevice.Remote
                    ?: throw WearAuthorizeException.EmptyRegisteredUserDeviceException()
            val accountType = secretTransferPayload.vaultKey.type.toUserAccountInfoType()

            val (sessionResult) = when (secretTransferPayload.vaultKey.type) {
                SecretTransferPayload.Type.MASTER_PASSWORD,
                SecretTransferPayload.Type.INVISIBLE_MASTER_PASSWORD,
                -> loginPassword(
                    secretTransferPayload = secretTransferPayload,
                    registeredUserDevice = registeredUserDevice,
                    sessionInitializer = sessionInitializer,
                    authenticationPasswordRepository = authenticationPasswordRepository,
                    accountType = accountType,
                )

                SecretTransferPayload.Type.SSO -> throw IllegalStateException("SSO users are not supported on the watch")
            }

            val session = (sessionResult as SessionResult.Success).session
            checkDeviceStrategy(session)

            globalPreferencesManager.setLastLoggedInUser(session.userId)
            
            sessionCredentialsSaver.saveCredentials(session)
            
            delay(500)
            emit(View.Success)

            
            delay(3_000)
            emit(SideEffect.OnSuccess)
        }
            .catch {
                when (val exception = it as? WearAuthorizeException) {
                    is WearAuthorizeException.DeviceLimitException -> {
                        handleDeviceLimitException(exception)
                    }
                    is WearAuthorizeException.EmptyPayloadException,
                    is WearAuthorizeException.EmptyRegisteredUserDeviceException,
                    null,
                    -> {
                        emit(View.Error)
                    }
                }
            }
            .onEach { state ->
                when (state) {
                    is View -> _stateFlow.update { state }
                    is SideEffect -> _stateFlow.send(state)
                }
            }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)
    }

    private suspend fun FlowCollector<WearAuthorizeState>.handleDeviceLimitException(
        exception: WearAuthorizeException.DeviceLimitException,
    ) {
        sessionTrasher.trash(
            username = exception.username,
            deletePreferences = true,
        )
        emit(SideEffect.GoToFreeUserPaywall(limit = exception.limit))
    }

    @Throws(WearAuthorizeException.DeviceLimitException::class)
    private suspend fun checkDeviceStrategy(session: Session) {
        when (val strategy = loginStrategy.getStrategy(session)) {
            is LoginStrategy.Strategy.Monobucket -> throw WearAuthorizeException.DeviceLimitException(
                username = session.username,
                limit = 1,
            )
            is LoginStrategy.Strategy.DeviceLimit -> throw WearAuthorizeException.DeviceLimitException(
                username = session.username,
                limit = strategy.limit,
            )
            else -> Unit
        }
    }
}

sealed class WearAuthorizeException : Exception() {
    data class DeviceLimitException(val username: Username, val limit: Int) :
        WearAuthorizeException()

    class EmptyPayloadException : WearAuthorizeException()

    class EmptyRegisteredUserDeviceException : WearAuthorizeException()
}