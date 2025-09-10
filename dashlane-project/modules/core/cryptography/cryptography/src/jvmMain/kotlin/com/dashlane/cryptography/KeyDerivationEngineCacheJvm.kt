package com.dashlane.cryptography

import okio.Closeable
import java.util.concurrent.ConcurrentHashMap

internal actual class KeyDerivationEngineCache(
    private val keyDerivationEngine: KeyDerivationEngine,
) : KeyDerivationEngine, Closeable {
    private val argon2Cache = ConcurrentHashMap<Argon2KeyDerivationConfiguration, ByteArray>()
    private val pbkdfCache = ConcurrentHashMap<Pbkdf2KeyDerivationConfiguration, ByteArray>()

    override fun deriveKeyArgon2d(
        key: ByteArray,
        salt: ByteArray,
        timeCost: Int,
        memoryCost: Int,
        parallelism: Int,
        derivedKeySizeInByte: Int,
    ): ByteArray {
        val keyDerivationConfiguration = Argon2KeyDerivationConfiguration(
            Argon2KeyDerivationConfiguration.Mode.D,
            key,
            salt,
            timeCost,
            memoryCost,
            parallelism,
            derivedKeySizeInByte,
        )
        return argon2Cache.getOrPut(keyDerivationConfiguration) {
            keyDerivationEngine.deriveKeyArgon2d(
                key,
                salt,
                timeCost,
                memoryCost,
                parallelism,
                derivedKeySizeInByte,
            )
        }
    }

    override fun deriveKeyArgon2i(
        key: ByteArray,
        salt: ByteArray,
        timeCost: Int,
        memoryCost: Int,
        parallelism: Int,
        derivedKeySizeInByte: Int,
    ): ByteArray {
        val keyDerivationConfiguration = Argon2KeyDerivationConfiguration(
            Argon2KeyDerivationConfiguration.Mode.I,
            key,
            salt,
            timeCost,
            memoryCost,
            parallelism,
            derivedKeySizeInByte,
        )
        return argon2Cache.getOrPut(keyDerivationConfiguration) {
            keyDerivationEngine.deriveKeyArgon2i(
                key,
                salt,
                timeCost,
                memoryCost,
                parallelism,
                derivedKeySizeInByte,
            )
        }
    }

    override fun deriveKeyPbkdf2Sha256(
        key: ByteArray,
        salt: ByteArray,
        iterationCount: Int,
        derivedKeySizeInByte: Int,
    ): ByteArray {
        val keyDerivationConfiguration = Pbkdf2KeyDerivationConfiguration(
            Pbkdf2KeyDerivationConfiguration.Hash.SHA256,
            key,
            salt,
            iterationCount,
            derivedKeySizeInByte,
        )
        return pbkdfCache.getOrPut(keyDerivationConfiguration) {
            keyDerivationEngine.deriveKeyPbkdf2Sha256(
                key,
                salt,
                iterationCount,
                derivedKeySizeInByte,
            )
        }
    }

    override fun deriveKeyPbkdf2Sha1(
        key: ByteArray,
        salt: ByteArray,
        iterationCount: Int,
        derivedKeySizeInByte: Int,
    ): ByteArray {
        val keyDerivationConfiguration = Pbkdf2KeyDerivationConfiguration(
            Pbkdf2KeyDerivationConfiguration.Hash.SHA1,
            key,
            salt,
            iterationCount,
            derivedKeySizeInByte,
        )
        return pbkdfCache.getOrPut(keyDerivationConfiguration) {
            keyDerivationEngine.deriveKeyPbkdf2Sha1(
                key,
                salt,
                iterationCount,
                derivedKeySizeInByte,
            )
        }
    }

    override fun mutateKey(key: ByteArray): ByteArray =
        keyDerivationEngine.mutateKey(key)

    override fun close() {
        argon2Cache.clearAndCloseValues()
        pbkdfCache.clearAndCloseValues()
    }

    private class Argon2KeyDerivationConfiguration(
        val mode: Mode,
        val key: ByteArray,
        val salt: ByteArray,
        val timeCost: Int,
        val memoryCost: Int,
        val parallelism: Int,
        val derivedKeySizeInByte: Int,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Argon2KeyDerivationConfiguration

            if (mode != other.mode) return false
            if (!key.contentEquals(other.key)) return false
            if (!salt.contentEquals(other.salt)) return false
            if (timeCost != other.timeCost) return false
            if (memoryCost != other.memoryCost) return false
            if (parallelism != other.parallelism) return false
            if (derivedKeySizeInByte != other.derivedKeySizeInByte) return false

            return true
        }

        override fun hashCode(): Int {
            var result = mode.hashCode()
            result = 31 * result + key.contentHashCode()
            result = 31 * result + salt.contentHashCode()
            result = 31 * result + timeCost
            result = 31 * result + memoryCost
            result = 31 * result + parallelism
            result = 31 * result + derivedKeySizeInByte
            return result
        }

        enum class Mode { D, I }
    }

    private class Pbkdf2KeyDerivationConfiguration(
        val hash: Hash,
        val key: ByteArray,
        val salt: ByteArray,
        val iterationCount: Int,
        val derivedKeySizeInByte: Int,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Pbkdf2KeyDerivationConfiguration

            if (hash != other.hash) return false
            if (!key.contentEquals(other.key)) return false
            if (!salt.contentEquals(other.salt)) return false
            if (iterationCount != other.iterationCount) return false
            if (derivedKeySizeInByte != other.derivedKeySizeInByte) return false

            return true
        }

        override fun hashCode(): Int {
            var result = hash.hashCode()
            result = 31 * result + key.contentHashCode()
            result = 31 * result + salt.contentHashCode()
            result = 31 * result + iterationCount
            result = 31 * result + derivedKeySizeInByte
            return result
        }

        enum class Hash { SHA256, SHA1 }
    }
}

private fun <T> MutableMap<out T, ByteArray>.clearAndCloseValues() {
    val iterator = iterator()
    while (iterator.hasNext()) {
        iterator.next().value.close()
        iterator.remove()
    }
}

internal actual fun KeyDerivationEngine.cache(): KeyDerivationEngineCache =
    KeyDerivationEngineCache(this)