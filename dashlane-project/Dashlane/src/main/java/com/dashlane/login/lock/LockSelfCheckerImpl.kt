package com.dashlane.login.lock

import com.dashlane.lock.LockSelfChecker
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

        val lockType = lockTypeManager.getLockType()

        
        if (!isValidStatus(lockType)) {
            sessionCredentialsSaver.saveCredentials(session)

            
            verification(session)
        }
        
        resetIsCredentialsEmpty()
    }

    private fun isValidStatus(@LockTypeManager.LockType lockType: Int): Boolean {
        if (lockType != LockTypeManager.LOCK_TYPE_PIN_CODE && lockType != LockTypeManager.LOCK_TYPE_BIOMETRIC) {
            return true
        }

        
        return !isCredentialsEmpty
    }

    private fun verification(session: Session) {
        if (sessionCredentialsSaver.areCredentialsSaved(session.username)) {
            logSuccess()
        } else {
            logFailure()
            lockTypeManager.setLockType(LockTypeManager.LOCK_TYPE_MASTER_PASSWORD)
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
