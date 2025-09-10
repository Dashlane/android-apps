package com.dashlane.login.devicelimit

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.data.login.Device
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.featureflipping.getDevicesLimitValue
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.CallToAction
import com.dashlane.login.devicelimit.DeviceLimitActivity.Companion.EXTRA_DEVICES
import com.dashlane.login.monobucket.MonobucketUseCase
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceLimitViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val preferencesManager: PreferencesManager,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val sessionManager: SessionManager,
    private val logRepository: LogRepository,
) : ViewModel() {
    private val _stateFlow = MutableViewStateFlow<DeviceLimitState.View, DeviceLimitState.SideEffect>(DeviceLimitState.View())
    val stateFlow: ViewStateFlow<DeviceLimitState.View, DeviceLimitState.SideEffect> = _stateFlow

    fun viewStarted() {
        viewModelScope.launch {
            if (_stateFlow.viewState.value.devices.isNotEmpty()) return@launch
            val maxDevices = userFeaturesChecker.getDevicesLimitValue()

            val extraDevices = savedStateHandle.get<Array<Parcelable>>(EXTRA_DEVICES)
                ?.map { it as Device }
                ?.toTypedArray()
                ?.toList() ?: emptyList()
            val myDeviceId = sessionManager.session?.accessKey

            val devices = extraDevices
                .filterNot { it.id == myDeviceId }
                .sortedByDescending { it.lastActivityDate }

            _stateFlow.update { state -> state.copy(maxDevices = maxDevices, devices = devices) }
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            if (_stateFlow.viewState.value.showDevicesDialog) {
                MonobucketUseCase.logUserActionStep2(logRepository, chosenAction = null)
                _stateFlow.update { state -> state.copy(showDevicesDialog = false) }
            } else {
                _stateFlow.send(DeviceLimitState.SideEffect.UserLoggedOut)
            }
        }
    }

    fun onUpgradePremium() {
        viewModelScope.launch {
            MonobucketUseCase.logUserActionStep1(logRepository, chosenAction = CallToAction.ALL_OFFERS)
            _stateFlow.send(DeviceLimitState.SideEffect.Premium)
        }
    }

    fun unlinkDevices() {
        viewModelScope.launch {
            MonobucketUseCase.logUserActionStep1(logRepository, chosenAction = CallToAction.UNLINK)
            _stateFlow.update { state -> state.copy(showDevicesDialog = true) }
        }
    }

    fun cancel() {
        viewModelScope.launch {
            MonobucketUseCase.logUserActionStep2(logRepository, chosenAction = null)
            _stateFlow.update { state -> state.copy(showDevicesDialog = false) }
        }
    }

    fun deviceSelected(device: Device, isChecked: Boolean) {
        viewModelScope.launch {
            _stateFlow.update { state ->
                val devices = state.devices.map { if (it.id == device.id) it.copy(selected = isChecked) else it }
                val selectCount = devices.filter { it.selected }.size
                
                
                val unlinkEnabled = devices.size - selectCount <= _stateFlow.value.maxDevices - 1
                state.copy(devices = devices, unlinkEnabled = unlinkEnabled)
            }
        }
    }

    fun onConfirmUnregisterDevice() {
        viewModelScope.launch {
            MonobucketUseCase.confirmUnregisterDevice(logRepository, preferencesManager, sessionManager)
            val devicesToUnlink: List<Device> = _stateFlow.value.devices.filter { it.selected }
            _stateFlow.send(DeviceLimitState.SideEffect.ConfirmUnregisterDevice(devicesToUnlink))
        }
    }

    fun onLogOut() {
        viewModelScope.launch {
            MonobucketUseCase.logout(logRepository, preferencesManager, sessionManager)
            _stateFlow.send(DeviceLimitState.SideEffect.UserLoggedOut)
        }
    }
}