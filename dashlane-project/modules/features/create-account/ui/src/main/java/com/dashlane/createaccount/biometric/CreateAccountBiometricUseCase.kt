package com.dashlane.createaccount.biometric

import com.dashlane.createaccount.CreateAccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun saveBiometricSettings(
    createAccountRepository: CreateAccountRepository,
    state: CreateAccountBiometricState.View.Loading,
): Flow<CreateAccountBiometricEvent> {
    return flow {

        createAccountRepository.updateBiometric(state.data.biometricChecked)
        createAccountRepository.updateBiometricRecovery(state.data.biometricRecoveryChecked)
        emit(CreateAccountBiometricEvent.Success)
    }
}