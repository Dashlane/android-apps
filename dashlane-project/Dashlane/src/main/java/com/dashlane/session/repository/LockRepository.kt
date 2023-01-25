package com.dashlane.session.repository

import com.dashlane.events.AppEvents
import com.dashlane.events.clearLastEvent
import com.dashlane.lock.UnlockEvent
import com.dashlane.login.lock.LockManager
import com.dashlane.session.BySessionRepository
import com.dashlane.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class LockRepository @Inject constructor(
    private val lockManager: LockManager,
    private val appEvents: AppEvents
) : SessionObserver, BySessionRepository<LockManager> {

    @Suppress("UNUSED_PARAMETER")
    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        
        lockManager.onUserChanged()
    }

    @Suppress("UNUSED_PARAMETER")
    override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        
        lockManager.resetAttemptsToUnlockCount()
        appEvents.clearLastEvent<UnlockEvent>()
    }

    
    @Suppress("UNUSED_PARAMETER")
    fun getLockManager(session: Session) = lockManager

    override fun get(session: Session?): LockManager? = session?.let { getLockManager(it) }
}