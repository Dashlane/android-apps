package com.dashlane.ui.screens.settings

import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.hermes.generated.events.user.UserSettings
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockType
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.user.Username
import dagger.Reusable
import javax.inject.Inject

@Reusable
class UserSettingsLogRepository @Inject constructor(
    private val lockManager: LockManager,
    private val inAppLoginManager: InAppLoginManager,
    private val biometricRecovery: BiometricRecovery,
    private val preferencesManager: PreferencesManager
) {
    fun get(username: Username): UserSettings {
        val locks = lockManager.getLocks(username)
        val userPreferencesManager = preferencesManager[username]

        val hasBiometrics = LockType.Biometric in locks

        return UserSettings(
            hasAuthenticationWithPin = LockType.PinCode in locks,
            hasAuthenticationWithBiometrics = hasBiometrics,
            hasAutofillActivated = inAppLoginManager.isEnableForApp(),
            hasMasterPasswordBiometricReset = hasBiometrics && biometricRecovery.isFeatureEnabled,
            hasUnlockItemWithBiometric = hasBiometrics,
            hasLockOnExit = lockManager.isLockOnExit(),
            lockAutoTimeout = lockManager.lockTimeout?.seconds?.toInt(),
            hasClearClipboard = userPreferencesManager.getBoolean(ConstantsPrefs.CLEAR_CLIPBOARD_ON_TIMEOUT),
            hasAutomaticTwoFactorAuthenticationTokenCopy = userPreferencesManager.getBoolean(ConstantsPrefs.HAS_AUTOMATIC_2FA_TOKEN_COPY)
        )
    }
}
