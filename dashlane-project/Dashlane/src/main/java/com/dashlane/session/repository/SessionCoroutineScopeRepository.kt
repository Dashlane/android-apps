package com.dashlane.session.repository

import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class SessionCoroutineScopeRepository @Inject constructor() : SessionObserver,
    BySessionRepository<CoroutineScope> {

    private val coroutineScopePerSession = mutableMapOf<Session, CoroutineScope>()

    

    fun sessionInitializing(session: Session) {
        coroutineScopePerSession[session] = InternalCoroutineScope(SupervisorJob())
    }

    override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        coroutineScopePerSession.remove(session)?.cancel()
    }

    override fun get(session: Session?): CoroutineScope? = session?.let { getCoroutineScope(it) }

    fun getCoroutineScope(session: Session) = coroutineScopePerSession[session]!!

    private class InternalCoroutineScope(job: Job) : CoroutineScope {
        override val coroutineContext = job
    }
}