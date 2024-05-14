package com.dashlane.login.lock

import com.dashlane.account.UserAccountInfo
import com.dashlane.account.UserAccountStorage
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.lock.LockHelper
import com.dashlane.lock.UnlockEvent
import com.dashlane.login.pages.password.LoginPasswordPresenter
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.security.SecurityHelper
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import com.dashlane.util.isNotSemanticallyNull
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class LockTypeManagerImpl @Inject constructor(
    private val preferenceManager: UserPreferencesManager,
    private val securityHelper: SecurityHelper,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val biometricAuthModule: BiometricAuthModule,
    private val sessionManager: SessionManager,
    private val biometricRecovery: BiometricRecovery,
    private val userAccountStorage: UserAccountStorage
) : LockTypeManager {

    private val lockoutDays = 14L

    @LockTypeManager.LockType
    override fun getLockType(): Int {
        return getLockType(true)
    }

    @LockTypeManager.LockType
    override fun getLockType(onlyIfAllow: Boolean): Int {
        if (onlyIfAllow && !securityHelper.isDeviceSecured()) {
            return LockTypeManager.LOCK_TYPE_MASTER_PASSWORD
        }
        if (biometricAuthModule.isHardwareSupported() && biometricAuthModule.isFeatureEnabled()) {
            return LockTypeManager.LOCK_TYPE_BIOMETRIC
        }
        val isPin = preferenceManager.isPinCodeOn
        return if (isPin && isPinSettingValid()) {
            LockTypeManager.LOCK_TYPE_PIN_CODE
        } else {
            LockTypeManager.LOCK_TYPE_MASTER_PASSWORD
        }
    }

    @LockTypeManager.LockType
    override fun getLockType(@LockHelper.LockPrompt lockPrompt: Int): Int = when (lockPrompt) {
        LockHelper.PROMPT_LOCK_FOR_SETTINGS -> !isSSO() && !isMPLess()
        LockHelper.PROMPT_LOCK_FOR_ITEM -> !isItemUnlockableByPinOrFingerprint()
        else -> false
    }.let { forceMasterPassword ->
        if (forceMasterPassword) LockTypeManager.LOCK_TYPE_MASTER_PASSWORD else getLockType()
    }

    override fun setLockType(@LockTypeManager.LockType lockType: Int) {
        if (isMPLess()) {
            setLockTypeForMPLess(lockType)
            return
        }

        
        biometricRecovery.setBiometricRecoveryFeatureEnabled(false)

        var lock = lockType
        if (!securityHelper.isDeviceSecured()) {
            lock = LockTypeManager.LOCK_TYPE_MASTER_PASSWORD
        }
        val session = sessionManager.session ?: return

        preferenceManager.isPinCodeOn = lock == LockTypeManager.LOCK_TYPE_PIN_CODE
        if (!preferenceManager.isPinCodeOn && !isMPLess()) {
            userSecureStorageManager.wipePin(session.username)
        }

        if (lock != LockTypeManager.LOCK_TYPE_PIN_CODE && lock != LockTypeManager.LOCK_TYPE_BIOMETRIC) {
            preferenceManager.remove(ConstantsPrefs.UNLOCK_ITEMS_WITH_PIN_OR_FP)
            sessionCredentialsSaver.deleteSavedCredentials(session.username)
            
            preferenceManager.credentialsSaveDate = Instant.EPOCH
        } else {
            
            resetLockoutTime()
        }

        val hardwareAuthShouldBeEnable = lock == LockTypeManager.LOCK_TYPE_BIOMETRIC
        if (hardwareAuthShouldBeEnable != biometricAuthModule.isFeatureEnabled()) {
            
            if (hardwareAuthShouldBeEnable) {
                biometricAuthModule.enableFeature()
            } else {
                biometricAuthModule.disableFeature()
            }
        }
    }

    private fun setLockTypeForMPLess(@LockTypeManager.LockType lockType: Int) {
        biometricRecovery.setBiometricRecoveryFeatureEnabled(false)
        val lock = if (lockType == LockTypeManager.LOCK_TYPE_MASTER_PASSWORD) LockTypeManager.LOCK_TYPE_PIN_CODE else lockType

        preferenceManager.isPinCodeOn = lock == LockTypeManager.LOCK_TYPE_PIN_CODE

        val hardwareAuthShouldBeEnable = lock == LockTypeManager.LOCK_TYPE_BIOMETRIC
        if (hardwareAuthShouldBeEnable != biometricAuthModule.isFeatureEnabled()) {
            
            if (hardwareAuthShouldBeEnable) {
                biometricAuthModule.enableFeature()
            } else {
                biometricAuthModule.disableFeature()
            }
        }
    }

    private fun isPinSettingValid(): Boolean {
        val session = sessionManager.session ?: return false
        val pin = userSecureStorageManager.readPin(session)
        return pin.isNotSemanticallyNull()
    }

    override fun isItemUnlockableByPinOrFingerprint(): Boolean {
        return preferenceManager.getBoolean(
            ConstantsPrefs.UNLOCK_ITEMS_WITH_PIN_OR_FP,
            true
        )
    }

    override fun shouldEnterMasterPassword(unlockReason: UnlockEvent.Reason?): Boolean {
        val askedAccountRecovery =
            unlockReason is UnlockEvent.Reason.WithCode && unlockReason.requestCode == LoginPasswordPresenter.UNLOCK_FOR_BIOMETRIC_RECOVERY

        val accountType = sessionManager.session?.username?.let { userAccountStorage[it]?.accountType }

        return when {
            accountType is UserAccountInfo.AccountType.InvisibleMasterPassword -> false
            askedAccountRecovery || getLockType() == LockTypeManager.LOCK_TYPE_MASTER_PASSWORD -> false
            else -> {
                val loginDate = preferenceManager.credentialsSaveDate
                loginDate.plus(Duration.ofDays(lockoutDays)) < Instant.now()
            }
        }
    }

    override fun resetLockoutTime() {
        preferenceManager.credentialsSaveDate = Instant.now()
    }

    private fun isSSO(): Boolean {
        return sessionManager.session?.let { userAccountStorage[it.username]?.sso } == true
    }

    private fun isMPLess(): Boolean {
        return sessionManager.session?.let { userAccountStorage[it.username]?.accountType } is UserAccountInfo.AccountType.InvisibleMasterPassword
    }
}
