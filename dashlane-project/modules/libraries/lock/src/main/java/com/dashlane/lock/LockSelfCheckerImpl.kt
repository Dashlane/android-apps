package com.dashlane.lock

import com.dashlane.session.Session
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import javax.inject.Inject

class LockSelfCheckerImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val lockTypeManager: LockTypeManager,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
) : LockSelfChecker {

    var isCredentialsEmpty = false
        private set

    override fun markCredentialsEmpty() {
        isCredentialsEmpty = true
    }

    override fun selfCheck() {
        val session = sessionManager.session ?: return

        val lockType = lockTypeManager.getLocks(session.username)

        
        if (!isValidStatus(lockType)) {
            sessionCredentialsSaver.saveCredentials(session)

            
            verification(session)
        }
        
        resetIsCredentialsEmpty()
    }

    private fun isValidStatus(locks: List<LockType>): Boolean {
        if (LockType.PinCode !in locks && LockType.Biometric !in locks) {
            return true
        }

        
        return !isCredentialsEmpty
    }

    private fun verification(session: Session) {
        if (sessionCredentialsSaver.areCredentialsSaved(session.username)) {
            logSuccess()
        } else {
            logFailure()
            lockTypeManager.removeLock(session.username, LockType.PinCode)
            lockTypeManager.removeLock(session.username, LockType.Biometric)
        }
    }

    private fun resetIsCredentialsEmpty() {
        isCredentialsEmpty = false
    }

    private fun logSuccess() {
    }

    private fun logFailure() {
    }
}
