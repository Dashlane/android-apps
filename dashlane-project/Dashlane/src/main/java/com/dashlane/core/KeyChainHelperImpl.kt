package com.dashlane.core

import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.toObfuscated
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.util.keychain.KeyChainManager
import com.dashlane.util.tryOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class KeyChainHelperImpl @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope
) : KeyChainHelper {

    private val keyChainManager: KeyChainManager? = tryOrNull { KeyChainManager.createInstance() }

    override fun initializeKeyStoreIfNeeded(login: String) {
        applicationCoroutineScope.launch {
            runCatching {
                keyChainManager?.loadKeyForUser(login)
            }
        }
    }

    override fun decryptPasswordWithUserCertificate(username: String, encrypted: String): ObfuscatedByteArray? {
        keyChainManager ?: return null 

        return tryOrNull {
            keyChainManager.decryptString(username, encrypted)?.toObfuscated()
        }
    }
}
