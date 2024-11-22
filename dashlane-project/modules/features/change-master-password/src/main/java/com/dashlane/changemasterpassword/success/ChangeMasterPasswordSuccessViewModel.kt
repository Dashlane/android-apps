package com.dashlane.changemasterpassword.success

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.accountrecoverykey.AccountRecoveryKeySettingStateRefresher
import com.dashlane.changemasterpassword.MasterPasswordChanger
import com.dashlane.crypto.keys.AppKey
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.exception.NotLoggedInException
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.ChangeMasterPasswordError
import com.dashlane.hermes.generated.definitions.DeleteKeyReason
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.events.user.ChangeMasterPassword
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPass
import com.dashlane.lock.LockType
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.server.api.endpoints.sync.MasterPasswordUploadService
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sync.cryptochanger.SyncCryptoChangerCryptographyException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeMasterPasswordSuccessViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val masterPasswordChanger: MasterPasswordChanger,
    private val lockManager: LockManager,
    private val accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
    private val accountRecoveryKeySettingStateRefresher: AccountRecoveryKeySettingStateRefresher,
    private val logRepository: LogRepository,
) : ViewModel() {

    private val _stateFlow =
        MutableViewStateFlow<ChangeMasterPasswordSuccessState.View, ChangeMasterPasswordSuccessState.SideEffect>(ChangeMasterPasswordSuccessState.View())
    val stateFlow: ViewStateFlow<ChangeMasterPasswordSuccessState.View, ChangeMasterPasswordSuccessState.SideEffect> = _stateFlow

    fun viewStarted() {
        listenForMasterPasswordChangerProgress()
        changeMasterPassword()
    }

    fun updateNewMasterPassword(obfuscatedMasterPassword: ObfuscatedByteArray) {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(obfuscatedMasterPassword = obfuscatedMasterPassword) }
        }
    }

    fun dismissReminderDialog() {
        viewModelScope.launch {
            logRepository.queueEvent(ChangeMasterPassword(flowStep = FlowStep.COMPLETE))
            _stateFlow.update { state -> state.copy(showReminderDialog = false) }
            _stateFlow.send(ChangeMasterPasswordSuccessState.SideEffect.Success)
        }
    }

    fun dismissCompletedButSyncErrorDialog() {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(error = null) }
            _stateFlow.send(ChangeMasterPasswordSuccessState.SideEffect.Logout)
        }
    }

    fun dismissErrorDialog() {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(error = null) }
            _stateFlow.send(ChangeMasterPasswordSuccessState.SideEffect.Cancel)
        }
    }

    @VisibleForTesting
    fun changeMasterPassword() {
        flow {
            logRepository.queueEvent(ChangeMasterPassword(flowStep = FlowStep.START))
            emit(_stateFlow.value.copy(progress = 10))

            val obfuscatedMasterPassword =
                _stateFlow.value.obfuscatedMasterPassword ?: throw IllegalStateException("obfuscatedMasterPassword is null")

            val masterPasswordChangerResult = masterPasswordChanger.updateMasterPassword(
                newPassword = obfuscatedMasterPassword,
                uploadReason = MasterPasswordUploadService.Request.UploadReason.COMPLETE_ACCOUNT_RECOVERY
            )

            if (!masterPasswordChangerResult) {
                return@flow
            }

            val session = sessionManager.session ?: throw IllegalStateException("session is null")
            unlockMP(session, masterPassword = obfuscatedMasterPassword)
            disableARK()

            emit(_stateFlow.value.copy(progress = 100, hasFinishedLoading = true))
            delay(1_000) 
            emit(_stateFlow.value.copy(showReminderDialog = true))
        }
            .catch {
                emit(_stateFlow.value.copy(error = ChangeMasterPasswordSuccessError.Generic))
            }
            .onEach { state ->
                _stateFlow.update { state }
            }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    fun listenForMasterPasswordChangerProgress() {
        masterPasswordChanger.progressStateFlow
            .map { progressState ->
                when (progressState) {
                    MasterPasswordChanger.Progress.Initializing -> {
                        _stateFlow.value.copy(progress = 0)
                    }
                    MasterPasswordChanger.Progress.Downloading -> {
                        _stateFlow.value.copy(progress = 20)
                    }
                    is MasterPasswordChanger.Progress.Ciphering -> {
                        val progress = 30 + 50 * (progressState.index / progressState.total)
                        _stateFlow.value.copy(progress = progress)
                    }
                    MasterPasswordChanger.Progress.Uploading -> {
                        _stateFlow.value.copy(progress = 80)
                    }
                    MasterPasswordChanger.Progress.Confirmation -> {
                        _stateFlow.value.copy(progress = 90)
                    }
                    MasterPasswordChanger.Progress.Completed.Success -> {
                        _stateFlow.value.copy(progress = 100)
                    }
                    is MasterPasswordChanger.Progress.Completed.Error -> {
                        val error = if (progressState.progress == MasterPasswordChanger.Progress.Confirmation) {
                            
                            ChangeMasterPasswordSuccessError.CompletedButSyncError
                        } else {
                            ChangeMasterPasswordSuccessError.Generic
                        }
                        logError(progressState.error, progressState.progress)
                        _stateFlow.value.copy(progress = 100, error = error)
                    }
                }
            }
            .catch {
                emit(_stateFlow.value.copy(error = ChangeMasterPasswordSuccessError.Generic))
            }
            .onEach { state ->
                _stateFlow.update { state }
            }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun unlockMP(session: Session, masterPassword: ObfuscatedByteArray) {
        lockManager.unlock(session = session, pass = LockPass.ofPassword(AppKey.Password(masterPassword)))
        lockManager.hasEnteredMP = true
        
        kotlin.runCatching {
            lockManager.sendUnlockEvent(LockEvent.Unlock(LockEvent.Unlock.Reason.AppAccess, LockType.MasterPassword))
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun disableARK() {
        accountRecoveryKeyRepository.disableRecoveryKey(DeleteKeyReason.VAULT_KEY_CHANGED)
        accountRecoveryKeySettingStateRefresher.refresh()
    }

    private fun logError(e: Exception, progress: MasterPasswordChanger.Progress?) {
        val errorName = when {
            e is SyncCryptoChangerCryptographyException -> ChangeMasterPasswordError.DECIPHER_ERROR
            e is NotLoggedInException -> ChangeMasterPasswordError.LOGIN_ERROR
            e is MasterPasswordChanger.SyncFailedException -> ChangeMasterPasswordError.SYNC_FAILED_ERROR
            progress == MasterPasswordChanger.Progress.Downloading -> ChangeMasterPasswordError.DOWNLOAD_ERROR
            progress == MasterPasswordChanger.Progress.Uploading -> ChangeMasterPasswordError.UPLOAD_ERROR
            progress == MasterPasswordChanger.Progress.Confirmation -> ChangeMasterPasswordError.CONFIRMATION_ERROR
            else -> ChangeMasterPasswordError.UNKNOWN_ERROR
        }
        logRepository.queueEvent(ChangeMasterPassword(flowStep = FlowStep.ERROR, errorName = errorName))
    }
}