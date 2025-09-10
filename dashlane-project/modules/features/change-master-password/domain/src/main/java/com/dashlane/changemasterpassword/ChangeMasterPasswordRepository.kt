package com.dashlane.changemasterpassword

import com.dashlane.cryptography.ObfuscatedByteArray
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@ActivityRetainedScoped
class ChangeMasterPasswordRepository @Inject constructor() : Mutex by Mutex() {
    private var obfuscatedMasterPassword: ObfuscatedByteArray? = null

    suspend fun getObfuscatedMasterPassword() = withLock { obfuscatedMasterPassword }

    suspend fun updateNewMasterPassword(obfuscatedMasterPassword: ObfuscatedByteArray) {
        withLock { this.obfuscatedMasterPassword = obfuscatedMasterPassword }
    }

    suspend fun clearData(): Unit = withLock { obfuscatedMasterPassword = null }
}