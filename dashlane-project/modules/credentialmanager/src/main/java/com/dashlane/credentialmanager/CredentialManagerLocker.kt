package com.dashlane.credentialmanager

interface CredentialManagerLocker {
    fun isLoggedIn(): Boolean
    fun isAccountLocked(): Boolean
    fun unlockDashlane()
}