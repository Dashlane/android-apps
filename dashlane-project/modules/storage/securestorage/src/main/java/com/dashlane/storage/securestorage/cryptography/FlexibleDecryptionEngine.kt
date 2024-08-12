package com.dashlane.storage.securestorage.cryptography

import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.cryptography.CryptographyIllegalKeyException
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.DecryptionEngine
import com.dashlane.cryptography.DecryptionSource
import com.dashlane.cryptography.EncryptionEngine
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.crypto.keys.AppKey
import okio.Source

internal class FlexibleDecryptionEngine(
    private val cryptography: Cryptography,
    private val key: ObfuscatedByteArray
) : DecryptionEngine {

    override fun <T> decrypt(decryptionSource: DecryptionSource, compressed: Boolean, block: (Source) -> T): T {
        val exceptions = enumValues<CryptographyKey.Type>().map { keyType ->
            try {
                return createDecryptionEngine(keyType).use { decryptionEngine ->
                    decryptionEngine.decrypt(
                        decryptionSource,
                        compressed,
                        block
                    )
                }
            } catch (e: CryptographyIllegalKeyException) {
                e
            } catch (e: IllegalArgumentException) {
                e
            }
        }

        throw exceptions.reduce { acc, e -> acc.addSuppressed(e); acc }
    }

    private fun createDecryptionEngine(keyType: CryptographyKey.Type) =
        CryptographyKey.ofBytes(key, keyType).use { cryptographyKey -> cryptography.createDecryptionEngine(cryptographyKey) }

    override fun close() {
        key.close()
    }
}

internal class FlexibleDecryptionEngineFactory(
    private val cryptography: Cryptography,
    private val key: AppKey
) : CryptographyEngineFactory {
    override fun createDecryptionEngine(): DecryptionEngine =
        FlexibleDecryptionEngine(cryptography, key.cryptographyKeyBytes)

    override fun createEncryptionEngine(): EncryptionEngine {
        throw UnsupportedOperationException()
    }
}