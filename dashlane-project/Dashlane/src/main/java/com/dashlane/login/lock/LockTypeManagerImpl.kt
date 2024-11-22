package com.dashlane.login.lock

import com.dashlane.account.UserAccountStorage
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.hardwaresecurity.BiometricAuthModule
import com.dashlane.lock.LockType
import com.dashlane.lock.LockTypeManager
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.user.UserAccountInfo
import com.dashlane.user.Username
import com.dashlane.util.isNotSemanticallyNull
import java.time.Instant
import javax.inject.Inject

class LockTypeManagerImpl @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val biometricAuthModule: BiometricAuthModule,
    private val sessionManager: SessionManager,
    private val biometricRecovery: BiometricRecovery,
    private val userAccountStorage: UserAccountStorage
) : LockTypeManager {

    override fun getLocks(username: Username): List<LockType> {
        val locks = mutableListOf<LockType>()
        if (!isMPLess(username)) {
            locks.add(LockType.MasterPassword)
        }
        if (biometricAuthModule.isHardwareSupported() &&
            biometricAuthModule.isHardwareSetUp() &&
            preferencesManager[username].getBoolean(ConstantsPrefs.USE_GOOGLE_FINGERPRINT)
        ) {
            locks.add(LockType.Biometric)
        }
        val isPin = preferencesManager[username].isPinCodeOn
        if (isPin && isPinSettingValid()) {
            locks.add(LockType.PinCode)
        }
        return locks
    }

    override fun addLock(username: Username, lockType: LockType) {
        val userPrefManager = preferencesManager[username]
        when (lockType) {
            LockType.MasterPassword -> Unit 
            LockType.PinCode -> {
                userPrefManager.credentialsSaveDate = Instant.now()
                userPrefManager.isPinCodeOn = true
            }
            LockType.Biometric -> {
                userPrefManager.credentialsSaveDate = Instant.now()
                biometricAuthModule.enableFeature(username)
            }
        }
    }

    override fun removeLock(username: Username, lockType: LockType) {
        val userPrefManager = preferencesManager[username]
        when (lockType) {
            LockType.MasterPassword -> Unit 
            LockType.PinCode -> {
                userSecureStorageManager.wipePin(username)
            }
            LockType.Biometric -> {
                biometricAuthModule.disableFeature(username)
                biometricRecovery.setBiometricRecoveryFeatureEnabled(false)
            }
        }

        val locks = getLocks(username)
        if (LockType.PinCode !in locks && LockType.Biometric !in locks) {
            sessionCredentialsSaver.deleteSavedCredentials(username)
            
            userPrefManager.credentialsSaveDate = Instant.EPOCH
        }
    }

    private fun isPinSettingValid(): Boolean {
        val session = sessionManager.session ?: return false
        val pin = userSecureStorageManager.readPin(session.localKey, session.username)
        return pin.isNotSemanticallyNull()
    }

    private fun isMPLess(username: Username): Boolean {
        return userAccountStorage[username]?.accountType is UserAccountInfo.AccountType.InvisibleMasterPassword
    }
}
