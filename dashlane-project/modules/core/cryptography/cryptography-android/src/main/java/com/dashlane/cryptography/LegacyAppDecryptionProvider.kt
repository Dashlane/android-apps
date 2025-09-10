package com.dashlane.cryptography

import com.dashlane.cryptography.jni.JniCryptography

fun interface LegacyAppDecryptionProvider {
    fun createAppDecryptionEngine(): DecryptionEngine
}

@Suppress("FunctionName")
fun LegacyAppDecryptionProvider(
    cryptography: Cryptography,
    jniCryptography: JniCryptography,
): LegacyAppDecryptionProvider = LegacyAppDecryptionProviderImpl(
    cryptography,
    jniCryptography,
)

private class LegacyAppDecryptionProviderImpl(
    private val cryptography: Cryptography,
    private val jniCryptography: JniCryptography,
) : LegacyAppDecryptionProvider {
    private val password
        get() = CryptographyKey.ofPassword(jniCryptography.getKeyForAppData())

    override fun createAppDecryptionEngine(): DecryptionEngine =
        password.use { cryptography.createDecryptionEngine(it) }
}