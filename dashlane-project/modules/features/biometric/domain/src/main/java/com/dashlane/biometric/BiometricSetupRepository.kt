package com.dashlane.biometric

import com.dashlane.lock.LockType
import com.dashlane.lock.LockTypeManager
import com.dashlane.session.Session
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.user.Username
import com.dashlane.util.keychain.KeyChainHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricSetupRepository @Inject constructor(
    private val lockTypeManager: LockTypeManager,
    private val keyChainHelper: KeyChainHelper,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
) {
    private val biometricStatusMutableFlow: MutableSharedFlow<Boolean> = MutableSharedFlow()
    val biometricStatusFlow: SharedFlow<Boolean> = biometricStatusMutableFlow.asSharedFlow()

    suspend fun enableBiometric(session: Session) {
        keyChainHelper.initializeKeyStoreIfNeeded(session.userId)
        
        runCatching { sessionCredentialsSaver.saveCredentials(session) }

        
        lockTypeManager.addLock(session.username, LockType.Biometric)

        biometricStatusMutableFlow.emit(true)
    }

    suspend fun disableBiometric(username: Username, deleteSavedCredentials: Boolean) {
        lockTypeManager.removeLock(username, LockType.Biometric)
        if (deleteSavedCredentials) {
            sessionCredentialsSaver.deleteSavedCredentials(username)
        }
        biometricStatusMutableFlow.emit(false)
    }
}