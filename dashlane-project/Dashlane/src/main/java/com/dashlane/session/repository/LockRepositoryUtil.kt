package com.dashlane.session.repository

import com.dashlane.login.lock.LockManager
import com.dashlane.session.SessionManager

fun LockRepository.getLockManager(sessionManager: SessionManager): LockManager? {
    return sessionManager.session?.let { getLockManager(it) }
}