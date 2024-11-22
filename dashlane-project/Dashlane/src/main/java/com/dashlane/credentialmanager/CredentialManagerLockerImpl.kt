package com.dashlane.credentialmanager

import android.content.Context
import com.dashlane.lock.LockManager
import com.dashlane.session.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import javax.inject.Inject

class CredentialManagerLockerImpl @Inject constructor(
    private val lockManager: LockManager,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : CredentialManagerLocker {
    override fun isLoggedIn(): Boolean = sessionManager.session != null

    override fun isAccountLocked(): Boolean {
        val lastUnlock = lockManager.lastUnlock
        return lockManager.isLocked ||
            (lastUnlock != null && lockManager.elapsedDuration(lastUnlock).abs() > Duration.ofSeconds(5))
    }

    override fun unlockDashlane() {
        lockManager.showLockActivityForAutofillApi(context)
    }
}