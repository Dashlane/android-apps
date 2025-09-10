package com.dashlane.login.lock

import com.dashlane.hardwaresecurity.BiometricAuthModule
import com.dashlane.hardwaresecurity.SecurityHelper
import com.dashlane.lock.LockType
import com.dashlane.lock.LockTypeManager
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun initialiseOnboardingApplicationLock(
    preferencesManager: PreferencesManager,
    sessionManager: SessionManager,
    state: OnboardingApplicationLockState.View.Initial,
): Flow<OnboardingApplicationLockEvent> {
    return flow {

        
        preferencesManager[sessionManager.session?.username].putLong(
            ConstantsPrefs.LOCK_POPUP_LATEST_TIMESTAMP,
            System.currentTimeMillis(),
        )
    }
}

fun refresh(
    sessionManager: SessionManager,
    lockTypeManager: LockTypeManager,
    securityHelper: SecurityHelper,
    biometricAuthModule: BiometricAuthModule,
    state: OnboardingApplicationLockState.View.Loading,
): Flow<OnboardingApplicationLockEvent> {
    return flow {
        val session = sessionManager.session ?: throw IllegalStateException("session cannot be null")
        val locks = lockTypeManager.getLocks(session.username)

        when {
            LockType.Biometric in locks || LockType.PinCode in locks -> {
                
                emit(OnboardingApplicationLockEvent.NoLockToSuggest)
            }

            !securityHelper.isDeviceSecured(session.username) -> {
                
                emit(OnboardingApplicationLockEvent.NoLockToSuggest)
            }

            else -> {
                val hasBiometrics = state.data.hasBiometricHardware ?: biometricAuthModule.isHardwareSetUp()
                emit(OnboardingApplicationLockEvent.ShowLockSetupSuggestion(hasBiometricHardware = hasBiometrics))
            }
        }
    }
}