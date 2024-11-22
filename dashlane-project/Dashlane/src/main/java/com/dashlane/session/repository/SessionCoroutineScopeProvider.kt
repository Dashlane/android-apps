package com.dashlane.session.repository

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.util.inject.OptionalProvider
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionCoroutineScopeProvider @Inject constructor(
    val sessionManager: SessionManager,
    val sessionCoroutineScopeRepository: BySessionRepository<CoroutineScope>
) : OptionalProvider<CoroutineScope> {
    override fun get(): CoroutineScope? = sessionCoroutineScopeRepository[sessionManager.session]
}