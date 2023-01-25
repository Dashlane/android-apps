package com.dashlane.authentication

import com.dashlane.cryptography.CryptographyKeyGenerator
import javax.inject.Inject

interface SsoServerKeyFactory {
    fun generateSsoServerKey(): ByteArray
}

class SsoServerKeyFactoryImpl @Inject constructor(
    private val cryptographyKeyGenerator: CryptographyKeyGenerator
) : SsoServerKeyFactory {
    override fun generateSsoServerKey() = cryptographyKeyGenerator.generateRaw64().use { it.toByteArray() }
}