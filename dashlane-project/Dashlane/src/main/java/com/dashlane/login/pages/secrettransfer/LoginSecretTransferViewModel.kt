package com.dashlane.login.pages.secrettransfer

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.user.UserAccountInfo
import com.dashlane.authentication.AuthenticationInvalidSsoException
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.authentication.login.AuthenticationSecretTransferRepository
import com.dashlane.core.KeyChainHelper
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.login.LoginMode
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockPass
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.pages.secrettransfer.confirmemail.SESSION_ERROR_MESSAGE
import com.dashlane.login.sso.LoginSsoContract
import com.dashlane.notificationcenter.NotificationCenterRepositoryImpl
import com.dashlane.notificationcenter.view.ActionItemType
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.secrettransfer.domain.SecretTransferAnalytics
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import com.dashlane.secrettransfer.domain.toUserAccountInfoType
import com.dashlane.crypto.keys.AppKey
import com.dashlane.crypto.keys.LocalKey
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionInitializer
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionResult
import com.dashlane.user.Username
import com.dashlane.crypto.keys.VaultKey
import com.dashlane.session.repository.LockRepository
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.xml.domain.SyncObject
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
    private val userPreferencesManager: UserPreferencesManager,
    private val lockManager: LockManager,
    private val sessionInitializer: SessionInitializer,
    private val authenticationSecretTransferRepository: AuthenticationSecretTransferRepository,
    private val authenticationLocalKeyRepository: AuthenticationLocalKeyRepository,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val sessionManager: SessionManager,
    private val keyChainHelper: KeyChainHelper,
    private val lockRepository: LockRepository,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val secretTransferAnalytics: SecretTransferAnalytics,
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

    fun deviceRegistered(registeredUserDevice: RegisteredUserDevice.Remote) {
        viewModelScope.launch {
            stateFlow.emit(
                LoginSecretTransferState.LoadingLogin(stateFlow.value.data.copy(registeredUserDevice = registeredUserDevice))
            )
        }
    }

    fun cancelOnError() {
        viewModelScope.launch { stateFlow.emit(LoginSecretTransferState.Cancelled(data = stateFlow.value.data)) }
    }

    fun pinSetup(pin: String) {
        viewModelScope.launch {
            sessionManager.session?.let { session ->
                keyChainHelper.initializeKeyStoreIfNeeded(session.userId)
                lockRepository.getLockManager(session).setLockType(LockTypeManager.LOCK_TYPE_PIN_CODE)

                userPreferencesManager.putBoolean(ConstantsPrefs.HOME_PAGE_GETTING_STARTED_PIN_IGNORE, true)
                userSecureStorageManager.storePin(session.localKey, session.username, pin)
                sessionCredentialsSaver.saveCredentials(session)
            }
        }
    }

    fun onEnableBiometrics() {
        viewModelScope.launch {
            sessionManager.session?.let { session ->
                sessionCredentialsSaver.saveCredentials(session)

                
                NotificationCenterRepositoryImpl.setDismissed(userPreferencesManager, ActionItemType.BIOMETRIC.trackingKey, false)
                lockManager.setLockType(LockTypeManager.LOCK_TYPE_BIOMETRIC)
            }
        }
    }

    fun login() {
        flow<LoginSecretTransferState> {
            val state = stateFlow.value
            val secretTransferPayload = state.data.secretTransferPayload ?: throw IllegalStateException("SecretTransferPayload must not be empty")
            val registeredUserDevice = state.data.registeredUserDevice ?: throw IllegalStateException("registeredUserDevice must not be empty")

            when (secretTransferPayload.vaultKey.type) {
                SecretTransferPayload.Type.MASTER_PASSWORD,
                SecretTransferPayload.Type.INVISIBLE_MASTER_PASSWORD -> loginPassword(secretTransferPayload, registeredUserDevice)

                SecretTransferPayload.Type.SSO -> loginSSO(secretTransferPayload, registeredUserDevice)
            }

            val accountType = when (secretTransferPayload.vaultKey.type) {
                SecretTransferPayload.Type.MASTER_PASSWORD,
                SecretTransferPayload.Type.SSO -> UserAccountInfo.AccountType.MasterPassword

                SecretTransferPayload.Type.INVISIBLE_MASTER_PASSWORD -> UserAccountInfo.AccountType.InvisibleMasterPassword
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

    @VisibleForTesting
    suspend fun loginPassword(
        secretTransferPayload: SecretTransferPayload,
        registeredUserDevice: RegisteredUserDevice.Remote
    ) {
        val username = Username.ofEmail(secretTransferPayload.login)
        val password = AppKey.Password(secretTransferPayload.vaultKey.value, registeredUserDevice.serverKey)
        val localKey = authenticationLocalKeyRepository.createForRemote(
            username = username,
            appKey = password,
            cryptographyMarker = CryptographyMarker.Flexible.Defaults.argon2d
        )
        val settings = authenticationSecretTransferRepository.decryptSettings(password.toVaultKey(), registeredUserDevice.encryptedSettings)
        val sessionResult = createSession(
            result = registeredUserDevice,
            username = username,
            localKey = localKey,
            settings = settings,
            appKey = password,
            remoteKey = null,
            loginMode = LoginMode.DeviceTransfer,
            accountType = secretTransferPayload.vaultKey.type.toUserAccountInfoType()
        )

        userPreferencesManager.userSettingsBackupTimeMillis = registeredUserDevice.settingsDate.toEpochMilli()

        if (sessionResult is SessionResult.Error) throw LoginSsoContract.CannotStartSessionException(SESSION_ERROR_MESSAGE, sessionResult.cause)

        lockManager.unlock(LockPass.ofPassword(password))
    }

    @VisibleForTesting
    suspend fun loginSSO(secretTransferPayload: SecretTransferPayload, registeredUserDevice: RegisteredUserDevice.Remote) {
        val username = Username.ofEmail(secretTransferPayload.login)
        val encryptedRemoteKey = registeredUserDevice.encryptedRemoteKey ?: throw AuthenticationInvalidSsoException()

        val ssoKey = AppKey.SsoKey(secretTransferPayload.vaultKey.value.decodeBase64ToByteArray())
        val localKey = authenticationLocalKeyRepository.createForRemote(
            username = username,
            appKey = ssoKey,
            cryptographyMarker = CryptographyMarker.Flexible.Defaults.noDerivation64
        )

        val remoteKey = authenticationSecretTransferRepository.decryptRemoteKey(ssoKey, encryptedRemoteKey)
        val settings = authenticationSecretTransferRepository.decryptSettings(remoteKey, registeredUserDevice.encryptedSettings)
        val sessionResult = createSession(
            result = registeredUserDevice,
            username = username,
            localKey = localKey,
            settings = settings,
            appKey = ssoKey,
            remoteKey = remoteKey,
            loginMode = LoginMode.DeviceTransfer,
            accountType = UserAccountInfo.AccountType.MasterPassword
        )

        if (sessionResult is SessionResult.Error) {
            throw LoginSsoContract.CannotStartSessionException(SESSION_ERROR_MESSAGE, sessionResult.cause)
        }

        userPreferencesManager.userSettingsBackupTimeMillis = registeredUserDevice.settingsDate.toEpochMilli()

        lockManager.unlock(LockPass.ofPassword(ssoKey))
    }

    private suspend fun createSession(
        result: RegisteredUserDevice.Remote,
        username: Username,
        localKey: LocalKey,
        settings: SyncObject.Settings,
        appKey: AppKey,
        remoteKey: VaultKey.RemoteKey?,
        loginMode: LoginMode,
        accountType: UserAccountInfo.AccountType
    ): SessionResult {
        return sessionInitializer.createSession(
            username = username,
            accessKey = result.accessKey,
            secretKey = result.secretKey,
            localKey = localKey,
            userSettings = settings,
            sharingPublicKey = result.sharingKeys?.publicKey,
            sharingPrivateKey = result.sharingKeys?.encryptedPrivateKey,
            appKey = appKey,
            remoteKey = remoteKey,
            userAnalyticsId = result.userAnalyticsId,
            deviceAnalyticsId = result.deviceAnalyticsId,
            loginMode = loginMode,
            accountType = accountType
        )
    }
}