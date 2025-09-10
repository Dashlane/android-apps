package com.dashlane.pin

import com.dashlane.lock.LockType
import com.dashlane.lock.LockTypeManager
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.Session
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.util.keychain.KeyChainHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

const val PIN_LENGTH = 6

@Singleton
class PinSetupRepository @Inject constructor(
    private val lockTypeManager: LockTypeManager,
    private val preferencesManager: PreferencesManager,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val keyChainHelper: KeyChainHelper,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
) {
    private val pinStatusMutableFlow: MutableSharedFlow<Boolean> = MutableSharedFlow()
    val pinStatusFlow: SharedFlow<Boolean> = pinStatusMutableFlow.asSharedFlow()

    suspend fun savePinValue(session: Session, pin: String) {
        keyChainHelper.initializeKeyStoreIfNeeded(session.userId)
        lockTypeManager.addLock(session.username, LockType.PinCode)
        preferencesManager[session.username].apply {
            putBoolean(ConstantsPrefs.HOME_PAGE_GETTING_STARTED_PIN_IGNORE, true)
            pinCodeLength = pin.length
        }
        userSecureStorageManager.storePin(session.localKey, session.username, pin)
        sessionCredentialsSaver.saveCredentials(session)

        pinStatusMutableFlow.emit(true)
    }

    suspend fun disablePin(session: Session) {
        lockTypeManager.removeLock(session.username, LockType.PinCode)
        pinStatusMutableFlow.emit(false)
    }
}