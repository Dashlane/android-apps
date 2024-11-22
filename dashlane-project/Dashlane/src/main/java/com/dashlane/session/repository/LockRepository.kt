package com.dashlane.session.repository

import com.dashlane.lock.LockManager
import com.dashlane.login.LoginInfo
import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockRepository @Inject constructor(
    private val lockManager: LockManager,
) : SessionObserver, BySessionRepository<LockManager> {

    @Suppress("UNUSED_PARAMETER")
    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        
        lockManager.onUserChanged(session.username)
    }

    @Suppress("UNUSED_PARAMETER")
    override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        
        lockManager.isLocked = true
        lockManager.resetAttemptsToUnlockCount()
    }

    
    @Suppress("UNUSED_PARAMETER")
    fun getLockManager(session: Session) = lockManager

    override fun get(session: Session?): LockManager? = session?.let { getLockManager(it) }
}