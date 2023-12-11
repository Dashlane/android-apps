package com.dashlane.login.accountrecoverykey.recovery
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.account.UserAccountInfo
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.SecurityFeature
import com.dashlane.authentication.login.AuthenticationPasswordRepository
import com.dashlane.core.KeyChainHelper
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.events.user.UseAccountRecoveryKey
import com.dashlane.lock.UnlockEvent
import com.dashlane.login.LoginStrategy
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryKeyRepository
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockPass
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.pages.password.LoginPasswordRepository
import com.dashlane.masterpassword.MasterPasswordChanger
import com.dashlane.network.tools.authorization
import com.dashlane.notificationcenter.NotificationCenterRepositoryImpl
import com.dashlane.notificationcenter.view.ActionItemType
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sync.MasterPasswordUploadService
import com.dashlane.session.AppKey
import com.dashlane.session.Session
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

@HiltViewModel
class RecoveryViewModel @Inject constructor(
    private val loginAccountRecoveryKeyRepository: LoginAccountRecoveryKeyRepository,
    private val authenticationPasswordRepository: AuthenticationPasswordRepository,
    private val loginPasswordRepository: LoginPasswordRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val loginStrategy: LoginStrategy,
    private val lockManager: LockManager,
    private val masterPasswordChanger: MasterPasswordChanger,
    private val keyChainHelper: KeyChainHelper,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val logRepository: LogRepository,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val stateFlow = MutableStateFlow<RecoveryState>(RecoveryState.Initial(progress = 0))
    val uiState = stateFlow.asStateFlow()

    fun viewStarted() {
        if (stateFlow.value !is RecoveryState.Initial) return
        listenForMasterPasswordChangerProgress()
        recoverAccount()
    }

    fun retry() {
        recoverAccount()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @VisibleForTesting
    fun recoverAccount() {
        flow {
            val accountType =
                loginAccountRecoveryKeyRepository.state.value.accountType ?: throw IllegalStateException("accountType was null at recoverAccount")
            emit(accountType)
        }
            .flatMapMerge { accountType ->
                when (accountType) {
                    UserAccountInfo.AccountType.InvisibleMasterPassword -> recoverInvisibleMasterPasswordAccount(accountType)
                    UserAccountInfo.AccountType.MasterPassword -> recoverMasterPasswordAccount(accountType)
                }
            }
            .flowOn(ioDispatcher)
            .catch {
                emit(RecoveryState.Error(progress = stateFlow.value.progress))
            }
            .onStart { emit(RecoveryState.Loading(progress = 0)) }
            .onEach { state ->
                stateFlow.emit(state)
            }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    fun recoverMasterPasswordAccount(accountType: UserAccountInfo.AccountType): Flow<RecoveryState> {
        return flow {
            val data = loginAccountRecoveryKeyRepository.state.value

            val registeredUserDevice = data.registeredUserDevice ?: throw IllegalStateException("registeredUserDevice was null at recoverAccount")
            val obfuscatedVaultKey = data.obfuscatedVaultKey ?: throw IllegalStateException("obfuscatedVaultKey was null at recoverAccount")
            val newMasterPassword = data.newMasterPassword ?: throw IllegalStateException("newMasterPassword was null at recoverAccount")

            emit(RecoveryState.Loading(progress = 10))
            val (session, strategy) = validatePasswordAndCreateSession(obfuscatedVaultKey, registeredUserDevice, accountType)

            val masterPasswordChangerResult = masterPasswordChanger.updateMasterPassword(
                newPassword = newMasterPassword,
                uploadReason = MasterPasswordUploadService.Request.UploadReason.COMPLETE_ACCOUNT_RECOVERY
            )

            if (!masterPasswordChangerResult) {
                emit(RecoveryState.Error(progress = stateFlow.value.progress))
                return@flow
            }

            unlockAndDisableARK(masterPassword = newMasterPassword, authorization = session.authorization)

            logRepository.queueEvent(UseAccountRecoveryKey(flowStep = FlowStep.COMPLETE))
            logRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.LOGIN_MASTER_PASSWORD_ACCOUNT_RECOVERY_SUCCESS)

            emit(RecoveryState.Finish(progress = 100, strategy = strategy))
        }
    }

    @VisibleForTesting
    fun recoverInvisibleMasterPasswordAccount(accountType: UserAccountInfo.AccountType): Flow<RecoveryState> {
        return flow {
            val data = loginAccountRecoveryKeyRepository.state.value

            val registeredUserDevice = data.registeredUserDevice ?: throw IllegalStateException("registeredUserDevice was null at recoverAccount")
            val obfuscatedVaultKey = data.obfuscatedVaultKey ?: throw IllegalStateException("obfuscatedVaultKey was null at recoverAccount")
            val pin = data.pin ?: throw IllegalStateException("Pin cannot be null for MPLess accounts")
            val biometricEnabled = data.biometricEnabled

            emit(RecoveryState.Loading(progress = 10))
            val (session, strategy) = validatePasswordAndCreateSession(obfuscatedVaultKey, registeredUserDevice, accountType)

            setupPinAndBiometric(session = session, pin = pin.decodeUtf8ToString(), biometricEnabled = biometricEnabled)

            loginAccountRecoveryKeyRepository.clearData()
            emit(RecoveryState.Finish(progress = 100, strategy = strategy))
        }
    }

    @VisibleForTesting
    fun listenForMasterPasswordChangerProgress() {
        masterPasswordChanger.progressStateFlow
            .map { progressState ->
                when (progressState) {
                    MasterPasswordChanger.Progress.Initializing -> {
                        RecoveryState.Loading(progress = 0)
                    }
                    MasterPasswordChanger.Progress.Downloading -> {
                        RecoveryState.Loading(progress = 20)
                    }
                    is MasterPasswordChanger.Progress.Ciphering -> {
                        val progress = 30 + 50 * (progressState.index / progressState.total)
                        RecoveryState.Loading(progress = progress)
                    }
                    MasterPasswordChanger.Progress.Uploading -> {
                        RecoveryState.Loading(progress = 80)
                    }
                    MasterPasswordChanger.Progress.Confirmation -> {
                        RecoveryState.Loading(progress = 90)
                    }
                    MasterPasswordChanger.Progress.Completed.Success -> {
                        RecoveryState.Loading(progress = 100)
                    }
                    is MasterPasswordChanger.Progress.Completed.Error -> {
                        RecoveryState.Loading(progress = 100)
                    }
                }
            }
            .catch {
            }
            .onEach { state ->
                stateFlow.emit(state)
            }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun validatePasswordAndCreateSession(
        obfuscatedVaultKey: ObfuscatedByteArray,
        registeredUserDevice: RegisteredUserDevice,
        accountType: UserAccountInfo.AccountType
    ): Pair<Session, LoginStrategy.Strategy?> {
        val validatePasswordResult: AuthenticationPasswordRepository.Result =
            authenticationPasswordRepository.validate(registeredUserDevice, obfuscatedVaultKey)

        return when (validatePasswordResult) {
            is AuthenticationPasswordRepository.Result.Local -> {
                val session = loginPasswordRepository.createSessionForLocalPassword(registeredUserDevice, validatePasswordResult)
                session to getLocalStrategy(session)
            }
            is AuthenticationPasswordRepository.Result.Remote -> {
                val session = loginPasswordRepository.createSessionForRemotePassword(validatePasswordResult, accountType = accountType)
                session to getRemoteStrategy(session, validatePasswordResult.securityFeatures)
            }
        }
    }

    private fun setupPinAndBiometric(session: Session, pin: String, biometricEnabled: Boolean) {
        keyChainHelper.initializeKeyStoreIfNeeded(session.userId)
        lockManager.setLockType(LockTypeManager.LOCK_TYPE_PIN_CODE)

        userPreferencesManager.putBoolean(ConstantsPrefs.HOME_PAGE_GETTING_STARTED_PIN_IGNORE, true)
        userSecureStorageManager.storePin(session, pin)
        sessionCredentialsSaver.saveCredentials(session)

        if (biometricEnabled) {
            
            NotificationCenterRepositoryImpl.setDismissed(userPreferencesManager, ActionItemType.BIOMETRIC.trackingKey, false)
            lockManager.setLockType(LockTypeManager.LOCK_TYPE_BIOMETRIC)
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun getLocalStrategy(session: Session): LoginStrategy.Strategy? {
        val shouldLaunchInitialSync = userPreferencesManager.getInt(ConstantsPrefs.TIMESTAMP_LABEL, 0) == 0
        val strategy = when {
            
            shouldLaunchInitialSync -> loginStrategy.getStrategy(session)
            else -> null
        }
        return strategy
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun getRemoteStrategy(session: Session, securityFeatures: Set<SecurityFeature>): LoginStrategy.Strategy {
        val strategy = loginStrategy.getStrategy(session, securityFeatures)
        if (strategy == LoginStrategy.Strategy.MONOBUCKET) {
            userPreferencesManager.ukiRequiresMonobucketConfirmation = true
        }
        return strategy
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun unlockAndDisableARK(masterPassword: ObfuscatedByteArray, authorization: Authorization.User) {
        lockManager.unlock(LockPass.ofPassword(AppKey.Password(masterPassword)))
        lockManager.hasEnteredMP = true
        
        kotlin.runCatching { lockManager.sendUnLock(UnlockEvent.Reason.AppAccess(), true) }

        loginAccountRecoveryKeyRepository.disableRecoveryKeyAfterUse(authorization)
        loginAccountRecoveryKeyRepository.clearData()

        userPreferencesManager.putBoolean(ConstantsPrefs.DISABLED_ACCOUNT_RECOVERY_KEY, true)
    }
}
