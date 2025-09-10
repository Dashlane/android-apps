package com.dashlane.cryptography

fun interface CryptographyAppKeyProvider {
    fun getOrCreate(alias: String): CryptographyKey.Raw32?
}

@Suppress("FunctionName")
fun CryptographyAppKeyProvider(
    keyStore: CryptographyAppKeyStore,
    keyGenerator: CryptographyKeyGenerator,
): CryptographyAppKeyProvider = CryptographyAppKeyProviderImpl(
    keyStore,
    keyGenerator,
)

private class CryptographyAppKeyProviderImpl(
    private val keyStore: CryptographyAppKeyStore,
    private val keyGenerator: CryptographyKeyGenerator,
) : CryptographyAppKeyProvider {
    override fun getOrCreate(alias: String): CryptographyKey.Raw32 =
        keyStore[alias] ?: create(alias)

    private fun create(alias: String): CryptographyKey.Raw32 {
        val cryptographyKey = keyGenerator.generateRaw32()
        keyStore[alias] = cryptographyKey
        return cryptographyKey
    }
}