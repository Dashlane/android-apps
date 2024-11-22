package com.dashlane.login.accountrecoverykey.recovery

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.login.AuthenticationPasswordRepository
import com.dashlane.changemasterpassword.MasterPasswordChanger
import com.dashlane.crypto.keys.AppKey
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.events.user.UseAccountRecoveryKey
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPass
import com.dashlane.lock.LockType
import com.dashlane.login.LoginStrategy
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryKeyRepository
import com.dashlane.login.pages.password.LoginPasswordRepository
import com.dashlane.login.root.LoginRepository
import com.dashlane.notificationcenter.NotificationCenterRepositoryImpl
import com.dashlane.notificationcenter.view.ActionItemType
import com.dashlane.pin.PinSetupRepository
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sync.MasterPasswordUploadService
import com.dashlane.session.Session
import com.dashlane.session.authorization
import com.dashlane.user.UserAccountInfo
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

@HiltViewModel
class RecoveryViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val loginAccountRecoveryKeyRepository: LoginAccountRecoveryKeyRepository,
    private val authenticationPasswordRepository: AuthenticationPasswordRepository,
    private val loginPasswordRepository: LoginPasswordRepository,
    private val preferencesManager: PreferencesManager,
    private val lockManager: LockManager,
    private val masterPasswordChanger: MasterPasswordChanger,
    private val logRepository: LogRepository,
    private val pinSetupRepository: PinSetupRepository,
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
            val registeredUserDevice =
                loginRepository.getRegisteredUserDevice() ?: throw IllegalStateException("registeredUserDevice was null at recoverAccount")

            val data = loginAccountRecoveryKeyRepository.state.value
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

            unlockMP(session = session, masterPassword = newMasterPassword)
            disableARK(accountType = accountType, authorization = session.authorization)

            logRepository.queueEvent(UseAccountRecoveryKey(flowStep = FlowStep.COMPLETE))
            logRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.LOGIN_MASTER_PASSWORD_ACCOUNT_RECOVERY_SUCCESS)

            emit(RecoveryState.Finish(progress = 100, strategy = strategy))
        }
    }

    @VisibleForTesting
    fun recoverInvisibleMasterPasswordAccount(accountType: UserAccountInfo.AccountType): Flow<RecoveryState> {
        return flow {
            val registeredUserDevice =
                loginRepository.getRegisteredUserDevice() ?: throw IllegalStateException("registeredUserDevice was null at recoverAccount")

            val data = loginAccountRecoveryKeyRepository.state.value
            val obfuscatedVaultKey = data.obfuscatedVaultKey ?: throw IllegalStateException("obfuscatedVaultKey was null at recoverAccount")
            val pin = data.pin ?: throw IllegalStateException("Pin cannot be null for MPLess accounts")
            val biometricEnabled = data.biometricEnabled

            emit(RecoveryState.Loading(progress = 10))
            val (session, strategy) = validatePasswordAndCreateSession(obfuscatedVaultKey, registeredUserDevice, accountType)

            setupPinAndBiometric(session = session, pin = pin.decodeUtf8ToString(), biometricEnabled = biometricEnabled)
            disableARK(accountType = accountType, authorization = session.authorization)

            logRepository.queueEvent(UseAccountRecoveryKey(flowStep = FlowStep.COMPLETE))
            logRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.LOGIN_MASTER_PASSWORD_ACCOUNT_RECOVERY_SUCCESS)

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
                session to loginPasswordRepository.getLocalStrategy(session)
            }
            is AuthenticationPasswordRepository.Result.Remote -> {
                val session = loginPasswordRepository.createSessionForRemotePassword(validatePasswordResult, accountType = accountType)
                session to loginPasswordRepository.getRemoteStrategy(session, validatePasswordResult.securityFeatures)
            }
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun setupPinAndBiometric(session: Session, pin: String, biometricEnabled: Boolean) {
        pinSetupRepository.savePinValue(session, pin)
        lockManager.unlock(session = session, pass = LockPass.ofPin(pin))
        runCatching {
            lockManager.sendUnlockEvent(LockEvent.Unlock(reason = LockEvent.Unlock.Reason.AppAccess, lockType = LockType.PinCode))
        }

        if (biometricEnabled) {
            
            NotificationCenterRepositoryImpl.setDismissed(preferencesManager[session.username], ActionItemType.BIOMETRIC.trackingKey, false)
            lockManager.addLock(session.username, LockType.Biometric)
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun unlockMP(session: Session, masterPassword: ObfuscatedByteArray) {
        lockManager.unlock(session = session, pass = LockPass.ofPassword(AppKey.Password(masterPassword)))
        lockManager.hasEnteredMP = true
        
        runCatching {
            lockManager.sendUnlockEvent(
                LockEvent.Unlock(reason = LockEvent.Unlock.Reason.AppAccess, lockType = LockType.MasterPassword)
            )
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun disableARK(accountType: UserAccountInfo.AccountType, authorization: Authorization.User) {
        loginAccountRecoveryKeyRepository.disableRecoveryKeyAfterUse(accountType, authorization)
        loginAccountRecoveryKeyRepository.clearData()

        preferencesManager[authorization.login].putBoolean(ConstantsPrefs.DISABLED_ACCOUNT_RECOVERY_KEY, true)
    }
}
