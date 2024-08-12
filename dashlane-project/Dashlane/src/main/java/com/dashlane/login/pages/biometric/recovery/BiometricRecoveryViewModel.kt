package com.dashlane.login.pages.biometric.recovery

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.lock.UnlockEvent
import com.dashlane.login.LoginStrategy
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockPass
import com.dashlane.masterpassword.MasterPasswordChanger
import com.dashlane.server.api.endpoints.sync.MasterPasswordUploadService
import com.dashlane.crypto.keys.AppKey
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BiometricRecoveryViewModel @Inject constructor(
    private val lockManager: LockManager,
    private val masterPasswordChanger: MasterPasswordChanger,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val stateFlow = MutableStateFlow(BiometricRecoveryState())
    private val navigationStateFlow = Channel<BiometricRecoveryNavigationState>()

    val uiState = stateFlow.asStateFlow()
    val navigationState = navigationStateFlow.receiveAsFlow()

    fun updateNewMasterPassword(obfuscatedMasterPassword: ObfuscatedByteArray) {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(obfuscatedMasterPassword = obfuscatedMasterPassword) }
        }
    }

    fun cancel() {
        viewModelScope.launch {
            lockManager.lockWithoutEvents()
        }
    }

    fun viewStarted() {
        if (stateFlow.value.progress != 0) return
        listenForMasterPasswordChangerProgress()
        recoverAccount()
    }

    fun retry() {
        recoverAccount()
    }

    fun cancelClicked() {
        viewModelScope.launch {
            lockManager.lockWithoutEvents()
            stateFlow.update { state -> state.copy(isError = false) }
            navigationStateFlow.send(BiometricRecoveryNavigationState.Cancel)
        }
    }

    fun confirmReminder() {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(showReminderDialog = false) }
            navigationStateFlow.send(BiometricRecoveryNavigationState.Success(LoginStrategy.Strategy.UNLOCK))
        }
    }

    @VisibleForTesting
    fun recoverAccount() {
        recoverMasterPasswordAccount()
            .flowOn(ioDispatcher)
            .catch {
                emit(stateFlow.value.copy(isError = true))
            }
            .onStart { emit(stateFlow.value.copy(progress = 0)) }
            .onEach { state ->
                stateFlow.update { state }
            }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    fun recoverMasterPasswordAccount(): Flow<BiometricRecoveryState> {
        return flow {
            val newMasterPassword =
                stateFlow.value.obfuscatedMasterPassword ?: throw IllegalStateException("newMasterPassword was null at recoverAccount")

            emit(stateFlow.value.copy(progress = 10))

            val masterPasswordChangerResult = masterPasswordChanger.updateMasterPassword(
                newPassword = newMasterPassword,
                uploadReason = MasterPasswordUploadService.Request.UploadReason.COMPLETE_ACCOUNT_RECOVERY
            )

            if (!masterPasswordChangerResult) {
                emit(stateFlow.value.copy(isError = true))
                return@flow
            }

            unlockMP(masterPassword = newMasterPassword)
            emit(stateFlow.value.copy(progress = 100, showReminderDialog = true))
        }
    }

    @VisibleForTesting
    fun listenForMasterPasswordChangerProgress() {
        masterPasswordChanger.progressStateFlow
            .map { progressState ->
                when (progressState) {
                    MasterPasswordChanger.Progress.Initializing -> {
                        stateFlow.value.copy(progress = 0)
                    }
                    MasterPasswordChanger.Progress.Downloading -> {
                        stateFlow.value.copy(progress = 20)
                    }
                    is MasterPasswordChanger.Progress.Ciphering -> {
                        val progress = 30 + 50 * (progressState.index / progressState.total)
                        stateFlow.value.copy(progress = progress)
                    }
                    MasterPasswordChanger.Progress.Uploading -> {
                        stateFlow.value.copy(progress = 80)
                    }
                    MasterPasswordChanger.Progress.Confirmation -> {
                        stateFlow.value.copy(progress = 90)
                    }
                    MasterPasswordChanger.Progress.Completed.Success -> {
                        stateFlow.value.copy(progress = 100)
                    }
                    is MasterPasswordChanger.Progress.Completed.Error -> {
                        stateFlow.value.copy(progress = 100)
                    }
                }
            }
            .catch {
                
            }
            .onEach { state ->
                stateFlow.update { state }
            }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    fun unlockMP(masterPassword: ObfuscatedByteArray) {
        lockManager.unlock(LockPass.ofPassword(AppKey.Password(masterPassword)))
        lockManager.hasEnteredMP = true
        
        kotlin.runCatching { lockManager.sendUnLock(UnlockEvent.Reason.AppAccess(), true) }
    }
}
