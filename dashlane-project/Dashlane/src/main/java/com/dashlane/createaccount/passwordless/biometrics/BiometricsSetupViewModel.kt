package com.dashlane.createaccount.passwordless.biometrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BiometricsSetupViewModel @Inject constructor(
    private val biometricAuthModule: BiometricAuthModule
) : ViewModel() {

    private val stateFlow = MutableStateFlow<BiometricSetupState>(BiometricSetupState.Loading)
    val uiState = stateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            if (biometricAuthModule.isHardwareSetUp()) {
                stateFlow.emit(BiometricSetupState.HardwareEnabled)
            } else {
                stateFlow.emit(BiometricSetupState.HardwareDisabled)
            }
        }
    }
}