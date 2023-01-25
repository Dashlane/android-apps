package com.dashlane.core

import com.dashlane.cryptography.ObfuscatedByteArray

interface KeyChainHelper {

    

    fun initializeKeyStoreIfNeeded(login: String)

    fun decryptPasswordWithUserCertificate(username: String, encrypted: String): ObfuscatedByteArray?
}
