package com.dashlane.ui.screens.settings

import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.hermes.generated.events.user.UserSettings
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockTypeManager.Companion.LOCK_TYPE_BIOMETRIC
import com.dashlane.login.lock.LockTypeManager.Companion.LOCK_TYPE_MASTER_PASSWORD
import com.dashlane.login.lock.LockTypeManager.Companion.LOCK_TYPE_PIN_CODE
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.util.tryOrNull
import dagger.Reusable
import javax.inject.Inject

@Reusable
class UserSettingsLogRepository @Inject constructor(
    private val lockManager: LockManager,
    private val inAppLoginManager: InAppLoginManager,
    private val biometricRecovery: BiometricRecovery,
    private val userPreferencesManager: UserPreferencesManager
) {
    fun get(): UserSettings {
        val lockType = tryOrNull { lockManager.getLockType() } ?: LOCK_TYPE_MASTER_PASSWORD

        val hasBiometrics = lockType == LOCK_TYPE_BIOMETRIC

        return UserSettings(
            hasAuthenticationWithPin = lockType == LOCK_TYPE_PIN_CODE,
            hasAuthenticationWithBiometrics = hasBiometrics,
            hasAutofillActivated = inAppLoginManager.isEnableForApp(),
            hasMasterPasswordBiometricReset = hasBiometrics && biometricRecovery.isFeatureEnabled,
            hasUnlockItemWithBiometric = hasBiometrics && lockManager.isItemUnlockableByPinOrFingerprint(),
            hasLockOnExit = lockManager.isLockOnExit(),
            lockAutoTimeout = lockManager.lockTimeout?.seconds?.toInt(),
            hasClearClipboard = userPreferencesManager.getBoolean(ConstantsPrefs.CLEAR_CLIPBOARD_ON_TIMEOUT),
            hasAutomaticTwoFactorAuthenticationTokenCopy = userPreferencesManager.getBoolean(ConstantsPrefs.HAS_AUTOMATIC_2FA_TOKEN_COPY)
        )
    }
}
