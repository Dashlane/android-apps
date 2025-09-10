package com.dashlane.cryptography

import okio.Buffer
import okio.BufferedSource
import okio.ByteString
import okio.ByteString.Companion.toByteString
import okio.Closeable
import okio.EOFException
import okio.HashingSource
import okio.Source
import okio.Timeout
import okio.buffer

interface DecryptionEngine : Closeable {
    @Throws(CryptographyException::class)
    fun <T> decrypt(
        decryptionSource: DecryptionSource,
        compressed: Boolean = false,
        block: (Source) -> T,
    ): T

    override fun close()
}

fun DecryptionEngine.decryptBase64ToByteArrayOrNull(string: EncryptedBase64String, compressed: Boolean = false): ByteArray? =
    try {
        decryptBase64ToByteArray(string, compressed)
    } catch (e: CryptographyException) {
        null
    }

@Throws(CryptographyException::class)
fun DecryptionEngine.decryptBase64ToByteArray(string: EncryptedBase64String, compressed: Boolean = false): ByteArray =
    decryptBase64(string, compressed) { it.buffer().readByteArray() }

fun DecryptionEngine.decryptBase64ToUtf8StringOrNull(string: EncryptedBase64String, compressed: Boolean = false): String? =
    try {
        decryptBase64ToUtf8String(string, compressed)
    } catch (e: CryptographyException) {
        null
    }

@Throws(CryptographyException::class)
fun DecryptionEngine.decryptBase64ToUtf8String(string: EncryptedBase64String, compressed: Boolean = false): String =
    decryptBase64(string, compressed) { it.buffer().readUtf8() }

@Throws(CryptographyException::class)
fun <T> DecryptionEngine.decryptBase64(string: EncryptedBase64String, compressed: Boolean = false, block: (Source) -> T): T =
    decrypt(string.asBase64DecryptionSource(), compressed, block)

fun DecryptionEngine.decryptByteArrayToByteArrayOrNull(byteArray: EncryptedByteArray, compressed: Boolean = false): ByteArray? =
    try {
        decryptByteArrayToByteArray(byteArray, compressed)
    } catch (e: CryptographyException) {
        null
    }

@Throws(CryptographyException::class)
fun DecryptionEngine.decryptByteArrayToByteArray(byteArray: EncryptedByteArray, compressed: Boolean = false): ByteArray =
    decryptByteArray(byteArray, compressed) { it.buffer().readByteArray() }

fun DecryptionEngine.decryptByteArrayToUtf8StringOrNull(byteArray: EncryptedByteArray, compressed: Boolean = false): String? =
    try {
        decryptByteArrayToUtf8String(byteArray, compressed)
    } catch (e: CryptographyException) {
        null
    }

@Throws(CryptographyException::class)
fun DecryptionEngine.decryptByteArrayToUtf8String(byteArray: EncryptedByteArray, compressed: Boolean = false): String =
    decryptByteArray(byteArray, compressed) { it.buffer().readUtf8() }

@Throws(CryptographyException::class)
fun <T> DecryptionEngine.decryptByteArray(byteArray: EncryptedByteArray, compressed: Boolean = false, block: (Source) -> T): T =
    decrypt(byteArray.asDecryptionSource(), compressed, block)

internal class DecryptionEngineImpl private constructor(
    private val cipherFactory: CipherFactory,
    private val macFactory: MacFactory,
    private val keyDerivationEngine: KeyDerivationEngineCache,
    private val key: ByteArray,
    private val keyType: CryptographyKey.Type,
) : DecryptionEngine {
    private var closed = false

    override fun <T> decrypt(decryptionSource: DecryptionSource, compressed: Boolean, block: (Source) -> T): T =
        checkNotClosed { decrypt(decryptionSource) { it.decompressIf(compressed).use(block) } }

    private fun <T> decrypt(decryptionSource: DecryptionSource, block: (Source) -> T): T =
        decryptionSource.open().use { source ->
            decrypt(source.selectCryptographyMarker(), source, block, decryptionSource)
        }

    private fun <T> decrypt(
        marker: CryptographyMarker,
        source: BufferedSource,
        block: (Source) -> T,
        decryptionSource: DecryptionSource,
    ): T =
        tryCryptographyEngine(marker) {
            marker.keyType.requireEquals(keyType)
            when (marker) {
                CryptographyMarker.Kwc3 ->
                    decryptKwc3(source, block, decryptionSource)
                CryptographyMarker.Kwc5 ->
                    decryptKwc5(source).use(block)
                is CryptographyMarker.Flexible ->
                    decryptFlexible(source, marker).use(block)
            }
        }

    private fun <T> decryptKwc3(
        source: BufferedSource,
        block: (Source) -> T,
        decryptionSource: DecryptionSource,
    ): T =
        try {
            decryptKwc3(source, mutate = true).use(block)
        } catch (e1: CryptographyCipherException) {
            try {
                decryptionSource.open().use { decryptKwc3(it, mutate = false).use(block) }
            } catch (e2: CryptographyCipherException) {
                e1.addSuppressed(e2)
                throw e1
            }
        }

    private fun decryptKwc3(source: BufferedSource, mutate: Boolean): Source {
        val salt = source.readByteArray(LEGACY_MARKER_HEADER_DATA_LENGTH.toLong()) 
        source.skip(CryptographyMarker.Kwc3.length) 
        val cipherKey = keyDerivationEngine.deriveKeyKwc3(key, salt, mutate)
        val iv = deriveKwc3iv(cipherKey, salt)
        val cipher = cipherFactory.createAesCbcCipher(CipherFactory.Mode.DECRYPT, cipherKey, iv)
        return source.cipherSource(cipher)
    }

    private fun decryptKwc5(source: BufferedSource): Source {
        val iv = source.readByteArray(AES_IV_SIZE.toLong())
        source.skip((LEGACY_MARKER_HEADER_DATA_LENGTH - AES_IV_SIZE).toLong()) 
        source.skip(CryptographyMarker.Kwc5.length) 
        val macValue = source.readHeaderData(HMAC_VALUE_SIZE)
        return expandCipherMacKeyPair(key).use { cipherKey, macKey ->
            val mac = macFactory.createHmacSha256(macKey, iv)
            val cipher = cipherFactory.createAesCbcCipher(CipherFactory.Mode.DECRYPT, cipherKey, iv)
            source.macValueCheckSource(mac, macValue).cipherSource(cipher)
        }
    }

    private fun decryptFlexible(
        source: BufferedSource,
        marker: CryptographyMarker.Flexible,
    ) = when (val cipher = marker.cipher) {
        is CryptographyMarker.Flexible.Cipher.Aes256 -> {
            when (val cipherMode = cipher.mode) {
                CryptographyMarker.Flexible.Cipher.Aes256.Mode.GCM ->
                    throw CryptographyUnsupportedException("Unsupported cipher mode $cipherMode.")
                CryptographyMarker.Flexible.Cipher.Aes256.Mode.CBC_HMAC -> {
                    val derivedKey = when (val keyDerivation = marker.keyDerivation) {
                        is CryptographyMarker.Flexible.KeyDerivation.Argon2d -> {
                            val salt = source.readHeaderData(keyDerivation.saltLength)
                            keyDerivationEngine.deriveKeyArgon2d(key, keyDerivation, salt)
                        }
                        is CryptographyMarker.Flexible.KeyDerivation.Argon2i -> {
                            val salt = source.readHeaderData(keyDerivation.saltLength)
                            keyDerivationEngine.deriveKeyArgon2i(key, keyDerivation, salt)
                        }
                        is CryptographyMarker.Flexible.KeyDerivation.Pbkdf2 -> {
                            val salt = source.readHeaderData(keyDerivation.saltLength)
                            keyDerivationEngine.deriveKeyPbkdf2(key, keyDerivation, salt)
                        }
                        is CryptographyMarker.Flexible.KeyDerivation.None ->
                            key
                    }
                    expandCipherMacKeyPair(derivedKey).use { cipherKey, macKey ->
                        decryptFlexibleAesCbcHmac(source, cipherKey, macKey)
                    }
                }
                CryptographyMarker.Flexible.Cipher.Aes256.Mode.CBC_HMAC_64 -> {
                    when (marker.keyDerivation) {
                        CryptographyMarker.Flexible.KeyDerivation.None ->
                            key.copyOfRange(0, AES_KEY_SIZE).use { cipherKey ->
                                key.copyOfRange(AES_KEY_SIZE, AES_KEY_SIZE + HMAC_KEY_SIZE).use { macKey ->
                                    decryptFlexibleAesCbcHmac(source, cipherKey, macKey)
                                }
                            }
                        else ->
                            throw CryptographyUnsupportedException("Unsupported key derivation with CBC-HMAC-64 cipher mode $marker.")
                    }
                }
            }
        }
    }

    private fun decryptFlexibleAesCbcHmac(
        source: BufferedSource,
        cipherKey: ByteArray,
        macKey: ByteArray,
    ): Source {
        val iv = source.readHeaderData(AES_IV_SIZE)
        val macValue = source.readHeaderData(HMAC_VALUE_SIZE)
        val cipher = cipherFactory.createAesCbcCipher(CipherFactory.Mode.DECRYPT, cipherKey, iv)
        val mac = macFactory.createHmacSha256(macKey, iv)
        return source.macValueCheckSource(mac, macValue).cipherSource(cipher)
    }

    override fun close() {
        closed = true
        keyDerivationEngine.close()
        key.close()
    }

    private inline fun <T> checkNotClosed(block: () -> T): T {
        check(!closed) { "Closed" }
        return block()
    }

    class Factory(
        private val cipherFactory: CipherFactory,
        private val macFactory: MacFactory,
        private val keyDerivationEngine: KeyDerivationEngine,
    ) {
        fun create(
            key: CryptographyKey,
        ): DecryptionEngine = DecryptionEngineImpl(
            cipherFactory,
            macFactory,
            keyDerivationEngine.cache(),
            key.toByteArray(),
            key.type,
        )
    }
}

private fun BufferedSource.readHeaderData(length: Int) =
    try {
        readByteArray(length.toLong())
    } catch (e: EOFException) {
        throw CryptographyHeaderException("Unable to read salt.", e)
    }

private fun Source.macValueCheckSource(mac: Mac, value: ByteArray): Source =
    hashingSource(mac).checkHash(value.toByteString())

private fun HashingSource.checkHash(expected: ByteString): Source =
    HashCheckSource(this, expected)

private class HashCheckSource(
    private val hashingSource: HashingSource,
    private val expectedHash: ByteString,
) : Source {
    private var isCheckPending = true

    override fun read(sink: Buffer, byteCount: Long): Long {
        val result = hashingSource.read(sink, byteCount)

        if (result == -1L && isCheckPending) {
            isCheckPending = false

            val hash = hashingSource.hash
            if (expectedHash != hash) {
                throw CryptographyChecksumException("Hash doesn't match expected value.")
            }
        }

        return result
    }

    override fun timeout(): Timeout =
        hashingSource.timeout()

    override fun close() =
        hashingSource.close()
}

private fun Source.decompressIf(compressed: Boolean): Source =
    if (compressed) buffer().decompress() else this

private fun BufferedSource.decompress(): Source =
    DecompressionHeaderSource(this).inflate()

private class DecompressionHeaderSource(private val source: BufferedSource) : Source {
    private var skipHeader = true

    override fun read(sink: Buffer, byteCount: Long): Long {
        if (skipHeader) {
            skipHeader = false
            try {
                source.skip(4)
            } catch (e: EOFException) {
                throw CryptographyCompressionHeaderException("Not enough bytes to read compression header. ", e)
            }
        }
        return source.read(sink, byteCount)
    }

    override fun timeout(): Timeout =
        source.timeout()

    override fun close() =
        source.close()
}

internal inline fun <T> Pair<ByteArray, ByteArray>.use(block: (ByteArray, ByteArray) -> T): T =
    first.use { x ->
        second.use { y ->
            block(x, y)
        }
    }