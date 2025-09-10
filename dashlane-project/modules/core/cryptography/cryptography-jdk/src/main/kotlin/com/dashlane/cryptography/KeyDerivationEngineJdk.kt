package com.dashlane.cryptography

import org.bouncycastle.crypto.Digest
import org.bouncycastle.crypto.digests.SHA1Digest
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import org.bouncycastle.crypto.params.Argon2Parameters.ARGON2_d
import org.bouncycastle.crypto.params.Argon2Parameters.ARGON2_i
import org.bouncycastle.crypto.params.KeyParameter
import java.nio.ByteBuffer
import java.nio.CharBuffer

@Suppress("FunctionName")
fun KeyDerivationEngine(): KeyDerivationEngine =
    KeyDerivationEngineImpl()

private class KeyDerivationEngineImpl : KeyDerivationEngine {
    override fun deriveKeyArgon2d(
        key: ByteArray,
        salt: ByteArray,
        timeCost: Int,
        memoryCost: Int,
        parallelism: Int,
        derivedKeySizeInByte: Int,
    ): ByteArray =
        deriveArgon(ARGON2_d, memoryCost, timeCost, parallelism, salt, derivedKeySizeInByte, key)

    override fun deriveKeyArgon2i(
        key: ByteArray,
        salt: ByteArray,
        timeCost: Int,
        memoryCost: Int,
        parallelism: Int,
        derivedKeySizeInByte: Int,
    ): ByteArray =
        deriveArgon(ARGON2_i, memoryCost, timeCost, parallelism, salt, derivedKeySizeInByte, key)

    private fun deriveArgon(
        type: Int,
        memoryCost: Int,
        timeCost: Int,
        parallelism: Int,
        salt: ByteArray,
        derivedKeySizeInByte: Int,
        key: ByteArray,
    ): ByteArray {
        val argon2Parameters = Argon2Parameters.Builder(type)
            .withMemoryAsKB(memoryCost)
            .withIterations(timeCost)
            .withParallelism(parallelism)
            .withSalt(salt)
            .build()
        val derivedKey = ByteArray(derivedKeySizeInByte)
        Argon2BytesGenerator().apply { init(argon2Parameters) }
            .generateBytes(key, derivedKey)
        return derivedKey
    }

    override fun deriveKeyPbkdf2Sha256(
        key: ByteArray,
        salt: ByteArray,
        iterationCount: Int,
        derivedKeySizeInByte: Int,
    ): ByteArray =
        deriveKeyPbkdf2(SHA256Digest(), key, salt, iterationCount, derivedKeySizeInByte)

    override fun deriveKeyPbkdf2Sha1(
        key: ByteArray,
        salt: ByteArray,
        iterationCount: Int,
        derivedKeySizeInByte: Int,
    ): ByteArray =
        deriveKeyPbkdf2(SHA1Digest(), key, salt, iterationCount, derivedKeySizeInByte)

    private fun deriveKeyPbkdf2(
        digest: Digest,
        key: ByteArray,
        salt: ByteArray,
        iterationCount: Int,
        derivedKeySize: Int,
    ): ByteArray {
        val generator = PKCS5S2ParametersGenerator(digest).apply {
            init(key, salt, iterationCount)
        }
        val derivedKeySizeInBits = derivedKeySize * 8
        val keyParameter = generator.generateDerivedParameters(derivedKeySizeInBits) as KeyParameter
        return keyParameter.key
    }

    override fun mutateKey(key: ByteArray): ByteArray =
        Charsets.UTF_8.decode(ByteBuffer.wrap(key)).use { decoded ->
            Charsets.UTF_16LE.encode(decoded).use { encoded ->
                ByteArray(encoded.remaining() / 2) { encoded[it * 2] }
            }
        }
}

private inline fun <T> CharBuffer.use(block: (CharBuffer) -> T): T =
    try {
        block(this)
    } finally {
        array().fill('\u0000')
    }

private inline fun <T> ByteBuffer.use(block: (ByteBuffer) -> T): T =
    try {
        block(this)
    } finally {
        array().fill(0)
    }