package com.dashlane.cryptography

interface CryptographyEngineFactory {
    fun createDecryptionEngine(): DecryptionEngine

    fun createEncryptionEngine(): EncryptionEngine
}

@Suppress("FunctionName")
fun CryptographyEngineFactory(
    cryptography: Cryptography,
    key: CryptographyKey,
    settings: CryptographySettings,
): CryptographyEngineFactory = CryptographyEngineFactoryImpl(
    cryptography,
    key,
    settings,
)

private class CryptographyEngineFactoryImpl(
    private val cryptography: Cryptography,
    private val key: CryptographyKey,
    private val settings: CryptographySettings,
) : CryptographyEngineFactory {
    override fun createDecryptionEngine(): DecryptionEngine =
        cryptography.createDecryptionEngine(key)

    override fun createEncryptionEngine(): EncryptionEngine {
        val marker = settings.getOrCreateMarker(key.type)
        val salt = if (marker.isFixedSalt) settings.getOrCreateFixedSalt(marker.saltLength) else null
        return cryptography.createEncryptionEngine(marker, key, salt)
    }
}