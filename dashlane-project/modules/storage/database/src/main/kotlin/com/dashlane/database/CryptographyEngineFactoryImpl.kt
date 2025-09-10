package com.dashlane.database

import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.DecryptionEngine
import com.dashlane.cryptography.EncryptionEngine
import com.dashlane.cryptography.createEncryptionEngine

@Suppress("FunctionName")
internal fun CryptographyEngineFactory(
    cryptography: Cryptography,
    key: CryptographyKey.Raw32,
): CryptographyEngineFactoryImpl = CryptographyEngineFactoryImpl(cryptography, key)

internal class CryptographyEngineFactoryImpl(
    private val cryptography: Cryptography,
    key: CryptographyKey.Raw32,
) : CryptographyEngineFactory {
    val key: CryptographyKey.Raw32 = key.clone()

    override fun createDecryptionEngine(): DecryptionEngine =
        cryptography.createDecryptionEngine(key)

    override fun createEncryptionEngine(): EncryptionEngine {
        return cryptography.createEncryptionEngine(DatabaseIndex.DEFAULT_CRYPTOGRAPHY, key)
    }

    fun close() = key.close()
}