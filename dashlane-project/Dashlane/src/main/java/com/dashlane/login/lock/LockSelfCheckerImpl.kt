package com.dashlane.login.lock

import com.dashlane.lock.LockSelfChecker
import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.usersupportreporter.UserSupportFileLogger
import javax.inject.Inject

class LockSelfCheckerImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val lockTypeManager: LockTypeManager,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val userSupportFileLogger: UserSupportFileLogger,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
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

            
            verification(session, lockType)
        }
        
        resetIsCredentialsEmpty()
    }

    

    private fun isValidStatus(@LockTypeManager.LockType lockType: Int): Boolean {
        if (lockType != LockTypeManager.LOCK_TYPE_PIN_CODE && lockType != LockTypeManager.LOCK_TYPE_BIOMETRIC)
            return true

        
        return !isCredentialsEmpty
    }

    

    private fun verification(session: Session, @LockTypeManager.LockType lockType: Int) {
        val type = when (lockType) {
            LockTypeManager.LOCK_TYPE_BIOMETRIC -> UsageLogConstant.LockType.fingerPrint
            LockTypeManager.LOCK_TYPE_PIN_CODE -> UsageLogConstant.LockType.pin
            else -> UsageLogConstant.LockType.master
        }

        if (sessionCredentialsSaver.areCredentialsSaved(session.username)) {
            logSuccess(session, type)
        } else {
            logFailure(session, type)
            lockTypeManager.setLockType(LockTypeManager.LOCK_TYPE_MASTER_PASSWORD)
        }
    }

    private fun resetIsCredentialsEmpty() {
        isCredentialsEmpty = false
    }

    private fun logSuccess(session: Session, lockType: String) {
        userSupportFileLogger.add("LockSelfChecker re-save credentials success")
        log(session, lockType, "reSaveSuccess")
    }

    private fun logFailure(session: Session, lockType: String) {
        userSupportFileLogger.add("LockSelfChecker re-save fails, reset lockType to MP")
        log(session, lockType, "reSaveFailed")
    }

    private fun log(session: Session, lockType: String, action: String) {
        bySessionUsageLogRepository[session]
            ?.enqueue(
                UsageLogCode35(
                    type = lockType,
                    action = action
                )
            )
    }
}
