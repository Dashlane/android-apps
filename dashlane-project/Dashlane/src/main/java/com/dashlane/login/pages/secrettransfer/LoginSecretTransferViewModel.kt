package com.dashlane.login.pages.secrettransfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.authentication.login.AuthenticationPasswordRepository
import com.dashlane.authentication.login.AuthenticationSecretTransferRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockType
import com.dashlane.login.root.LoginRepository
import com.dashlane.notificationcenter.NotificationCenterRepositoryImpl
import com.dashlane.notificationcenter.view.ActionItemType
import com.dashlane.pin.PinSetupRepository
import com.dashlane.preference.PreferencesManager
import com.dashlane.secrettransfer.domain.SecretTransferAnalytics
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import com.dashlane.secrettransfer.domain.toUserAccountInfoType
import com.dashlane.secrettransfer.loginPassword
import com.dashlane.secrettransfer.loginSSO
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionInitializer
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionResult
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class LoginSecretTransferViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val preferencesManager: PreferencesManager,
    private val lockManager: LockManager,
    private val sessionInitializer: SessionInitializer,
    private val authenticationSecretTransferRepository: AuthenticationSecretTransferRepository,
    private val authenticationLocalKeyRepository: AuthenticationLocalKeyRepository,
    private val authenticationPasswordRepository: AuthenticationPasswordRepository,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val sessionManager: SessionManager,
    private val secretTransferAnalytics: SecretTransferAnalytics,
    private val pinSetupRepository: PinSetupRepository,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val stateFlow = MutableStateFlow<LoginSecretTransferState>(LoginSecretTransferState.Initial(LoginSecretTransferData()))

    val uiState = stateFlow.asStateFlow()

    fun hasNavigated() {
        viewModelScope.launch {
            stateFlow.emit(LoginSecretTransferState.Initial(stateFlow.value.data))
        }
    }

    fun logPageView(page: AnyPage) {
        secretTransferAnalytics.pageView(page)
    }

    fun logCompleteTransfer(biometricsEnabled: Boolean) {
        secretTransferAnalytics.completeDeviceTransfer(biometricsEnabled)
    }

    fun payloadFetched(secretTransferPayload: SecretTransferPayload) {
        viewModelScope.launch {
            stateFlow.emit(LoginSecretTransferState.ConfirmEmail(stateFlow.value.data.copy(secretTransferPayload = secretTransferPayload)))
        }
    }

    fun cancelOnError() {
        viewModelScope.launch { stateFlow.emit(LoginSecretTransferState.Cancelled(data = stateFlow.value.data)) }
    }

    fun pinSetup(pin: String) {
        viewModelScope.launch {
            sessionManager.session?.let { session ->
                pinSetupRepository.savePinValue(session, pin)
            }
        }
    }

    fun onEnableBiometrics() {
        viewModelScope.launch {
            sessionManager.session?.let { session ->
                sessionCredentialsSaver.saveCredentials(session)

                
                NotificationCenterRepositoryImpl.setDismissed(preferencesManager[session.username], ActionItemType.BIOMETRIC.trackingKey, false)
                lockManager.addLock(session.username, LockType.Biometric)
            }
        }
    }

    fun login() {
        flow<LoginSecretTransferState> {
            val state = stateFlow.value
            val secretTransferPayload = state.data.secretTransferPayload ?: throw IllegalStateException("SecretTransferPayload must not be empty")
            val registeredUserDevice = loginRepository.getRegisteredUserDevice() as? RegisteredUserDevice.Remote
                ?: throw IllegalStateException("registeredUserDevice must not be empty and should always be Remote for SecretTransfer")
            val accountType = secretTransferPayload.vaultKey.type.toUserAccountInfoType()

            val (sessionResult, username, lockPass) = when (secretTransferPayload.vaultKey.type) {
                SecretTransferPayload.Type.MASTER_PASSWORD,
                SecretTransferPayload.Type.INVISIBLE_MASTER_PASSWORD -> loginPassword(
                    sessionInitializer = sessionInitializer,
                    authenticationPasswordRepository = authenticationPasswordRepository,
                    secretTransferPayload = secretTransferPayload,
                    registeredUserDevice = registeredUserDevice,
                    accountType = accountType,
                )
                SecretTransferPayload.Type.SSO -> loginSSO(
                    secretTransferPayload = secretTransferPayload,
                    registeredUserDevice = registeredUserDevice,
                    sessionInitializer = sessionInitializer,
                    authenticationLocalKeyRepository = authenticationLocalKeyRepository,
                    authenticationSecretTransferRepository = authenticationSecretTransferRepository
                )
            }
            when (sessionResult) {
                is SessionResult.Error -> throw CannotStartSessionException(SESSION_ERROR_MESSAGE, sessionResult.cause)
                is SessionResult.Success -> {
                    preferencesManager[username].userSettingsBackupTimeMillis = registeredUserDevice.settingsDate.toEpochMilli()
                    lockManager.unlock(sessionResult.session, lockPass)
                    runCatching {
                        lockManager.sendUnlockEvent(
                            LockEvent.Unlock(reason = LockEvent.Unlock.Reason.AppAccess, lockType = LockType.MasterPassword)
                        )
                    }
                }
            }

            
            delay(500)
            emit(LoginSecretTransferState.Success(stateFlow.value.data, accountType))
        }
            .catch {
                emit(LoginSecretTransferState.Error(stateFlow.value.data))
            }
            .onEach { state -> stateFlow.emit(state) }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)
    }

    companion object {
        private const val SESSION_ERROR_MESSAGE = "Error on Session creation"
    }
}

class CannotStartSessionException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
