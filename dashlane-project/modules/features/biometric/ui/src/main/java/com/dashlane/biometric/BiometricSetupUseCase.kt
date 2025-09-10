package com.dashlane.biometric

import androidx.biometric.BiometricPrompt.CryptoObject
import com.dashlane.biometric.BiometricSetupEvent.BiometricChecked
import com.dashlane.biometric.BiometricSetupEvent.BiometricRecoveryChecked
import com.dashlane.biometric.BiometricSetupEvent.Error
import com.dashlane.biometric.BiometricSetupEvent.Success
import com.dashlane.hardwaresecurity.BiometricActivationStatus
import com.dashlane.hardwaresecurity.BiometricAuthModule
import com.dashlane.hardwaresecurity.BiometricRecovery
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockType
import com.dashlane.notificationcenter.NotificationCenterRepository
import com.dashlane.notificationcenter.view.ActionItemType
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun startBiometricSetup(
    biometricAuthModule: BiometricAuthModule,
    sessionManager: SessionManager,
    state: BiometricSetupState.View.Loading,
): Flow<BiometricSetupEvent> {
    return flow {

        val biometricActivationStatus = biometricAuthModule.getBiometricActivationStatus()

        if (biometricActivationStatus == BiometricActivationStatus.NOT_ENABLED) {
            emit(Error(BiometricSetupError.BiometricNotRegistered))
            return@flow
        }

        if (biometricActivationStatus == BiometricActivationStatus.INSUFFICIENT_STRENGTH) {
            emit(Error(BiometricSetupError.RequireStrongerBiometric))
            return@flow
        }

        val username = sessionManager.session?.userId ?: run {
            emit(BiometricSetupEvent.Cancel)
            return@flow
        }

        if (!biometricAuthModule.createEncryptionKeyForBiometrics(username = username)) {
            
            emit(Error(BiometricSetupError.BiometricNotRegistered))
            return@flow
        }

        val biometricStatus = biometricAuthModule.checkBiometricStatus(username, biometricActivationStatus)
        val promptAuthenticators = biometricAuthModule.getPromptAuthenticator(biometricActivationStatus)

        if (biometricStatus !is BiometricAuthModule.Result.BiometricEnrolled || promptAuthenticators == null) {
            emit(BiometricSetupEvent.Cancel)
            return@flow
        }

        val cryptoObject = biometricStatus.cipher?.let { CryptoObject(it) }

        emit(BiometricChecked(authenticator = promptAuthenticators, cryptoObject = cryptoObject))
    }
}

fun startBiometricRecoverySetup(
    lockManager: LockManager,
    sessionManager: SessionManager,
    biometricRecovery: BiometricRecovery,
    state: BiometricSetupState.View.LoadingRecovery,
): Flow<BiometricSetupEvent> {
    return flow {

        val session = sessionManager.session ?: run {
            emit(BiometricSetupEvent.Cancel)
            return@flow
        }

        if (!biometricRecovery.isFeatureAvailable()) {
            emit(BiometricSetupEvent.Cancel)
            return@flow
        }

        if (LockType.Biometric in lockManager.getLocks(session.username)) {
            saveBiometricRecovery(biometricRecovery)
            emit(Success(goToRecovery = false))
        } else {
            emit(BiometricRecoveryChecked)
        }
    }
}

fun saveBiometric(
    sessionManager: SessionManager,
    biometricSetupRepository: BiometricSetupRepository,
    biometricRecovery: BiometricRecovery,
    preferencesManager: PreferencesManager,
    state: BiometricSetupState.View.SaveBiometric,
): Flow<BiometricSetupEvent> {
    return flow {

        val session = sessionManager.session ?: return@flow
        biometricSetupRepository.enableBiometric(session)

        
        NotificationCenterRepository.setDismissed(preferencesManager[session.username], ActionItemType.BIOMETRIC.trackingKey, false)

        if (state.data.isBiometricRecovery && biometricRecovery.isFeatureAvailable()) {
            saveBiometricRecovery(biometricRecovery)
            emit(Success(goToRecovery = false))
        } else if (biometricRecovery.isFeatureAvailable()) {
            biometricRecovery.isFeatureKnown = true
            emit(Success(goToRecovery = true))
        } else {
            emit(Success(goToRecovery = false))
        }
    }
}

fun saveBiometricRecovery(
    biometricRecovery: BiometricRecovery,
    state: BiometricSetupState.View.SaveBiometricRecovery,
): Flow<BiometricSetupEvent> {
    return flow {
        saveBiometricRecovery(biometricRecovery)
    }
}

private suspend fun saveBiometricRecovery(biometricRecovery: BiometricRecovery) {
    biometricRecovery.isFeatureKnown = true
    biometricRecovery.setBiometricRecoveryFeatureEnabledAsync(true)
}