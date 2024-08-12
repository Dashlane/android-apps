package com.dashlane.authentication

import com.dashlane.cryptography.CryptographyKeyGenerator
import com.dashlane.crypto.keys.VaultKey
import javax.inject.Inject

interface RemoteKeyFactory {
    fun generateRemoteKey(): VaultKey.RemoteKey
}

class RemoteKeyFactoryImpl @Inject constructor(
    private val cryptographyKeyGenerator: CryptographyKeyGenerator
) : RemoteKeyFactory {
    override fun generateRemoteKey() =
        cryptographyKeyGenerator.generateRaw64().use(VaultKey::RemoteKey)
}