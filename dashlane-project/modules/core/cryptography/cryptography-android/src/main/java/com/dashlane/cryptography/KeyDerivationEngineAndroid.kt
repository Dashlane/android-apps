package com.dashlane.cryptography

import com.dashlane.cryptography.jni.JniCryptography

@Suppress("FunctionName")
fun KeyDerivationEngine(
    jniCryptography: JniCryptography = JniCryptography(),
): KeyDerivationEngine = KeyDerivationEngineImpl(jniCryptography)

private class KeyDerivationEngineImpl(
    private val jniCryptography: JniCryptography,
) : KeyDerivationEngine {
    override fun deriveKeyArgon2d(
        key: ByteArray,
        salt: ByteArray,
        timeCost: Int,
        memoryCost: Int,
        parallelism: Int,
        derivedKeySizeInByte: Int,
    ): ByteArray =
        jniCryptography.deriveKeyArgon2d(key, salt, timeCost, memoryCost, parallelism, derivedKeySizeInByte)

    override fun deriveKeyArgon2i(
        key: ByteArray,
        salt: ByteArray,
        timeCost: Int,
        memoryCost: Int,
        parallelism: Int,
        derivedKeySizeInByte: Int,
    ): ByteArray =
        jniCryptography.deriveKeyArgon2i(key, salt, timeCost, memoryCost, parallelism, derivedKeySizeInByte)

    override fun deriveKeyPbkdf2Sha256(
        key: ByteArray,
        salt: ByteArray,
        iterationCount: Int,
        derivedKeySizeInByte: Int,
    ): ByteArray =
        jniCryptography.deriveKeyPBKDF2sha256(key, salt, iterationCount, derivedKeySizeInByte)

    override fun deriveKeyPbkdf2Sha1(
        key: ByteArray,
        salt: ByteArray,
        iterationCount: Int,
        derivedKeySizeInByte: Int,
    ): ByteArray =
        jniCryptography.deriveKeyPBKDF2sha1(key, salt, iterationCount, derivedKeySizeInByte)

    @Suppress("DEPRECATION")
    override fun mutateKey(key: ByteArray) =
        jniCryptography.mutateKeyKWC3(key)
}