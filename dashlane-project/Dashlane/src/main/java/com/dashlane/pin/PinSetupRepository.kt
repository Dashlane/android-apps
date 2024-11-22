package com.dashlane.pin

import com.dashlane.lock.LockType
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.Session
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.repository.LockRepository
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.util.keychain.KeyChainHelper
import javax.inject.Inject

class PinSetupRepository @Inject constructor(
    private val lockRepository: LockRepository,
    private val preferencesManager: PreferencesManager,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val keyChainHelper: KeyChainHelper,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
) {

    fun savePinValue(session: Session, pin: String) {
        keyChainHelper.initializeKeyStoreIfNeeded(session.userId)
        lockRepository.getLockManager(session).addLock(session.username, LockType.PinCode)
        preferencesManager[session.username].apply {
            putBoolean(ConstantsPrefs.HOME_PAGE_GETTING_STARTED_PIN_IGNORE, true)
            pinCodeLength = pin.length
        }
        userSecureStorageManager.storePin(session.localKey, session.username, pin)
        sessionCredentialsSaver.saveCredentials(session)
    }
}