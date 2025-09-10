package com.dashlane.credentialmanager

import android.content.Context
import androidx.biometric.BiometricPrompt.CryptoObject
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPass
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import javax.inject.Inject

class CredentialManagerLocker @Inject constructor(
    private val lockManager: LockManager,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context,
) {
    private val session: Session?
        get() = sessionManager.session

    fun isLoggedIn(): Boolean = session != null

    fun isAccountLocked(): Boolean {
        val currentSession = session ?: return true

        if (lockManager.getLocks(currentSession.username).isEmpty() == true) {
            return false
        }

        val lastUnlock = lockManager.lastUnlock
        return lockManager.isLocked ||
            (
                lastUnlock != null && lockManager.elapsedDuration(lastUnlock)
                    .abs() > Duration.ofSeconds(5)
            )
    }

    fun unlockDashlane() {
        lockManager.showLockActivityForAutofillApi(context)
    }

    fun silentUnlock(cryptoObject: CryptoObject?): Boolean {
        val currentSession = session ?: return false

        val lockPass = cryptoObject?.let { LockPass.ofBiometric(it) } ?: LockPass.ofWeakBiometric()
        return lockManager.unlock(currentSession, lockPass)
    }
}