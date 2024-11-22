package com.dashlane.util.keychain

import com.dashlane.cryptography.ObfuscatedByteArray

interface KeyChainHelper {

    fun initializeKeyStoreIfNeeded(login: String)

    fun decryptPasswordWithUserCertificate(username: String, encrypted: String): ObfuscatedByteArray?
}
