package com.dashlane.login.progress

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.R
import com.dashlane.login.Device
import com.dashlane.login.progress.LoginSyncProgressActivity.Companion.EXTRA_DEVICE_SYNC_LIMIT_UNREGISTRATION
import com.dashlane.login.progress.LoginSyncProgressActivity.Companion.EXTRA_MONOBUCKET_UNREGISTRATION
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.server.api.endpoints.devices.DeactivateDevicesService
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.SessionManager
import com.dashlane.session.authorization
import com.dashlane.sync.DataSync
import com.dashlane.sync.DataSyncState
import com.dashlane.sync.repositories.SyncProgress
import com.dashlane.util.stackTraceToSafeString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val CHRONOLOGICAL_SYNC_PERCENT_HALF = 40

@HiltViewModel
class LoginSyncProgressViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val dataSync: DataSync,
    private val deactivateDevicesService: DeactivateDevicesService,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _stateFlow = MutableViewStateFlow<LoginSyncProgressState.View, LoginSyncProgressState.SideEffect>(LoginSyncProgressState.View())
    val stateFlow: ViewStateFlow<LoginSyncProgressState.View, LoginSyncProgressState.SideEffect> = _stateFlow

    fun viewStarted() {
        viewModelScope.launch {
            if (_stateFlow.value.message != null) return@launch

            val isMonobucketUnregistration: Boolean = savedStateHandle[EXTRA_MONOBUCKET_UNREGISTRATION] ?: false
            val devicesToUnregister = savedStateHandle.get<Array<Parcelable>>(EXTRA_DEVICE_SYNC_LIMIT_UNREGISTRATION)
                ?.map { it as Device }?.toTypedArray()?.toList() ?: emptyList()

            val initialMessage = when {
                
                
                
                isMonobucketUnregistration -> R.string.login_sync_progress_monobucket_unregister
                
                devicesToUnregister.isNotEmpty() -> R.string.login_sync_progress_device_limit_unregister
                else -> R.string.login_sync_progress_deciphering
            }

            _stateFlow.update { state -> state.copy(message = initialMessage) }

            if (devicesToUnregister.isNotEmpty()) {
                unregisterDevices(devicesToUnregister)
                    .onSuccess { initialSync() }
                    .onFailure { _stateFlow.update { state -> state.copy(error = LoginSyncProgressError.Unregister) } }
            } else {
                initialSync()
            }
        }
    }

    fun cancel() {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(error = null) }
            _stateFlow.send(LoginSyncProgressState.SideEffect.Cancel)
        }
    }

    fun retry() {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(error = null) }
            unregisterDevices(_stateFlow.value.devicesToUnregister)
                .onSuccess { initialSync() }
                .onFailure { _stateFlow.update { state -> state.copy(error = LoginSyncProgressError.Unregister) } }
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun initialSync() {
        dataSync.dataSyncState
            .mapNotNull { syncState ->
                when (syncState) {
                    is DataSyncState.Idle.Failure -> {
                        sessionManager.session?.let { sessionManager.destroySession(it, byUser = false, forceLogout = false) }
                        LoginSyncProgressState.SideEffect.SyncError
                    }
                    is DataSyncState.Active -> {
                        syncState.progress.getProgressAndMessage()?.let { (progress, message) ->
                            _stateFlow.value.copy(progress = progress, message = message)
                        }
                    }
                    DataSyncState.Idle.Success -> {
                        finalize()
                        null
                    }
                    else -> null
                }
            }
            .catch { e ->
                sessionManager.session?.let { sessionManager.destroySession(it, byUser = false, forceLogout = false) }
                emit(LoginSyncProgressState.SideEffect.Cancel)
            }
            .onEach { state ->
                when (state) {
                    is LoginSyncProgressState.View -> _stateFlow.update { state }
                    is LoginSyncProgressState.SideEffect -> _stateFlow.send(state)
                }
            }
            .launchIn(viewModelScope)

        dataSync.initialSync()
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun unregisterDevices(devicesToUnregister: List<Device>): Result<Unit> {
        val session = sessionManager.session ?: return Result.failure(IllegalStateException("Session cannot be null"))
        val devices = devicesToUnregister.filter { it.pairingGroupId == null }
        val groups = devicesToUnregister.minus(devices.toSet())
        try {
            deactivateDevicesService.execute(
                userAuthorization = session.authorization,
                request = DeactivateDevicesService.Request(
                    deviceIds = devices.map { it.id },
                    pairingGroupIds = groups.map { it.pairingGroupId!! }
                )
            )
            return Result.success(Unit)
        } catch (e: DashlaneApiException) {
            return Result.failure(e)
        }
    }

    private fun SyncProgress.getProgressAndMessage(): Pair<Int, Int>? {
        return when (this) {
            is SyncProgress.DecipherRemote -> {
                (progress * CHRONOLOGICAL_SYNC_PERCENT_HALF).toInt() to R.string.login_sync_progress_deciphering
            }
            is SyncProgress.LocalSync -> {
                (CHRONOLOGICAL_SYNC_PERCENT_HALF + progress * CHRONOLOGICAL_SYNC_PERCENT_HALF).toInt() to R.string.login_sync_progress_saving
            }
            SyncProgress.TreatProblem -> {
                CHRONOLOGICAL_SYNC_PERCENT_HALF * 2 to R.string.login_sync_progress_finalizing
            }
            else -> null 
        }
    }

    private fun finalize() {
        flow {
            emit(_stateFlow.value.copy(progress = 100))
            delay(200) 
            emit(_stateFlow.value.copy(hasFinishedLoading = true, message = R.string.login_sync_progress_success))
            delay(200) 
            emit(LoginSyncProgressState.SideEffect.Success)
        }
            .onEach { state ->
                when (state) {
                    is LoginSyncProgressState.SideEffect -> _stateFlow.send(state)
                    is LoginSyncProgressState.View -> _stateFlow.update { state }
                }
            }
            .launchIn(viewModelScope)
    }
}