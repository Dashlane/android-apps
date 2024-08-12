package com.dashlane.pin

import com.dashlane.user.UserAccountInfo
import com.dashlane.account.UserAccountStorage
import com.dashlane.core.KeyChainHelper
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.LockRepository
import com.dashlane.storage.securestorage.UserSecureStorageManager
import javax.inject.Inject

class PinSetupRepository @Inject constructor(
    private val sessionManager: SessionManager,
    private val lockRepository: LockRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val keyChainHelper: KeyChainHelper,
    private val userAccountStorage: UserAccountStorage,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
) {

    fun savePinValue(pin: String) {
        val session = sessionManager.session ?: return
        keyChainHelper.initializeKeyStoreIfNeeded(session.userId)
        if (canUseMasterPassword(session.userId)) {
            lockRepository.getLockManager(session).setLockType(LockTypeManager.LOCK_TYPE_PIN_CODE)
        }
        userPreferencesManager.putBoolean(ConstantsPrefs.HOME_PAGE_GETTING_STARTED_PIN_IGNORE, true)
        userSecureStorageManager.storePin(session.localKey, session.username, pin)
        sessionCredentialsSaver.saveCredentials(session)
    }

    fun canUseMasterPassword(email: String): Boolean {
        return userAccountStorage[email]?.accountType is UserAccountInfo.AccountType.MasterPassword
    }
}