package com.dashlane.cryptography

import com.dashlane.cryptography.CipherFactory.Mode
import okio.Buffer
import okio.BufferedSink
import okio.Closeable
import okio.Sink
import okio.Timeout
import okio.buffer

interface EncryptionEngine : Closeable {
    fun encrypt(
        encryptionSink: EncryptionSink,
        compressed: Boolean = false,
    ): Sink

    override fun close()
}

fun EncryptionEngine.encryptByteArrayToBase64String(
    bytes: ByteArray,
    compressed: Boolean = false,
): EncryptedBase64String =
    encryptToBase64String(compressed) { sink -> sink.buffer().use { it.write(bytes) } }

fun EncryptionEngine.encryptUtf8ToBase64String(
    string: String,
    compressed: Boolean = false,
): EncryptedBase64String =
    encryptToBase64String(compressed) { sink -> sink.buffer().use { it.writeUtf8(string) } }

inline fun EncryptionEngine.encryptToBase64String(
    compressed: Boolean = false,
    block: (Sink) -> Unit,
): EncryptedBase64String =
    runEncryptionToBase64 { encrypt(it, compressed).use(block) }

fun EncryptionEngine.encryptByteArrayToByteArray(
    bytes: ByteArray,
    compressed: Boolean = false,
): EncryptedByteArray =
    encryptToByteArray(compressed) { sink -> sink.buffer().use { it.write(bytes) } }

fun EncryptionEngine.encryptUtf8ToByteArray(
    string: String,
    compressed: Boolean = false,
): EncryptedByteArray =
    encryptToByteArray(compressed) { sink -> sink.buffer().use { it.writeUtf8(string) } }

inline fun EncryptionEngine.encryptToByteArray(
    compressed: Boolean = false,
    block: (Sink) -> Unit,
): EncryptedByteArray =
    runEncryptionToByteArray { encrypt(it, compressed).use(block) }

internal class EncryptionEngineKwc3Impl private constructor(
    private val cipherFactory: CipherFactory,
    private val keyDerivationEngine: KeyDerivationEngine,
    private val saltGenerator: SaltGenerator,
    private val key: ByteArray,
) : EncryptionEngine {
    private var closed = false

    override fun encrypt(encryptionSink: EncryptionSink, compressed: Boolean): Sink =
        checkNotClosed { encryptKwc3(encryptionSink, compressed) }

    private fun encryptKwc3(encryptionSink: EncryptionSink, compressed: Boolean): Sink {
        val salt = saltGenerator.generateRandomSalt(LEGACY_MARKER_HEADER_DATA_LENGTH)
        val cipherKey = keyDerivationEngine.deriveKeyKwc3(key, salt, mutate = true)
        val iv = deriveKwc3iv(cipherKey, salt)
        val cipher = cipherFactory.createAesCbcCipher(Mode.ENCRYPT, cipherKey, iv)
        return encryptionSink.open()
            .writeKwc3Header(salt)
            .cipherSink(cipher)
            .compressIf(compressed)
    }

    override fun close() {
        closed = true
        key.close()
    }

    private inline fun <T> checkNotClosed(block: () -> T): T {
        check(!closed) { "Closed" }
        return block()
    }

    class Factory(
        private val cipherFactory: CipherFactory,
        private val keyDerivationEngine: KeyDerivationEngine,
        private val saltGenerator: SaltGenerator,
    ) {
        fun create(
            key: CryptographyKey,
        ): EncryptionEngine = EncryptionEngineKwc3Impl(
            cipherFactory,
            keyDerivationEngine,
            saltGenerator,
            key.toByteArray(),
        )
    }
}

internal class EncryptionEngineKwc5Impl private constructor(
    private val cipherFactory: CipherFactory,
    private val macFactory: MacFactory,
    private val ivGenerator: IvGenerator,
    private val paddingGenerator: PaddingGenerator,
    private val cipherKey: ByteArray,
    private val macKey: ByteArray,
) : EncryptionEngine {
    private var closed = false

    override fun encrypt(encryptionSink: EncryptionSink, compressed: Boolean): Sink =
        checkNotClosed { encryptKwc5(encryptionSink, compressed) }

    private fun encryptKwc5(encryptionSink: EncryptionSink, compressed: Boolean): Sink {
        val iv = ivGenerator.generateRandomIv(AES_IV_SIZE)
        val padding =
            paddingGenerator.generateRandomPadding(LEGACY_MARKER_HEADER_DATA_LENGTH - AES_IV_SIZE)
        val mac = macFactory.createHmacSha256(macKey, iv)
        val cipher = cipherFactory.createAesCbcCipher(Mode.ENCRYPT, cipherKey, iv)
        return encryptionSink.open()
            .writeKwc5Header(encryptionSink, mac, iv, padding)
            .hashingSink(mac)
            .cipherSink(cipher)
            .compressIf(compressed)
    }

    override fun close() {
        closed = true
        cipherKey.close()
        macKey.close()
    }

    private inline fun <T> checkNotClosed(block: () -> T): T {
        check(!closed) { "Closed" }
        return block()
    }

    class Factory(
        private val cipherFactory: CipherFactory,
        private val macFactory: MacFactory,
        private val ivGenerator: IvGenerator,
        private val paddingGenerator: PaddingGenerator,
    ) {
        fun create(
            key: CryptographyKey,
        ): EncryptionEngine {
            val (cipherKey, macKey) = key.toByteArray().use(::expandCipherMacKeyPair)
            return EncryptionEngineKwc5Impl(
                cipherFactory,
                macFactory,
                ivGenerator,
                paddingGenerator,
                cipherKey,
                macKey,
            )
        }
    }
}

internal class EncryptionEngineFlexibleAesCbcHmacImpl private constructor(
    private val cipherFactory: CipherFactory,
    private val macFactory: MacFactory,
    private val ivGenerator: IvGenerator,
    private val marker: CryptographyMarker.Flexible,
    private val cipherKey: ByteArray,
    private val macKey: ByteArray,
    private val salt: ByteArray,
) : EncryptionEngine {
    private var closed = false

    override fun encrypt(encryptionSink: EncryptionSink, compressed: Boolean): Sink =
        checkNotClosed { encryptFlexible(encryptionSink, compressed) }

    private fun encryptFlexible(
        encryptionSink: EncryptionSink,
        compressed: Boolean,
    ): Sink {
        val iv = ivGenerator.generateRandomIv(AES_IV_SIZE)
        val mac = macFactory.createHmacSha256(macKey, iv)
        val cipher = cipherFactory.createAesCbcCipher(Mode.ENCRYPT, cipherKey, iv)
        return encryptionSink.open()
            .writeFlexibleHeader(encryptionSink, marker, salt, iv, mac)
            .hashingSink(mac)
            .cipherSink(cipher)
            .compressIf(compressed)
    }

    override fun close() {
        closed = true
        cipherKey.close()
        macKey.close()
    }

    private inline fun <T> checkNotClosed(block: () -> T): T {
        check(!closed) { "Closed" }
        return block()
    }

    class Factory(
        private val cipherFactory: CipherFactory,
        private val macFactory: MacFactory,
        private val keyDerivationEngine: KeyDerivationEngine,
        private val ivGenerator: IvGenerator,
    ) {
        fun createArgon2d(
            key: CryptographyKey.Password,
            salt: CryptographyFixedSalt,
            keyDerivation: CryptographyMarker.Flexible.KeyDerivation.Argon2d = CryptographyMarker.Flexible.KeyDerivation.Default.argon2d,
        ): EncryptionEngine {
            val derivedKey = key.toByteArray().use {
                keyDerivationEngine.deriveKeyArgon2d(
                    it,
                    keyDerivation,
                    salt.requireSize(keyDerivation.saltLength),
                )
            }
            return createPasswordDerivation(
                derivedKey,
                keyDerivation,
                salt.requireSize(keyDerivation.saltLength),
            )
        }

        fun createArgon2i(
            key: CryptographyKey.Password,
            salt: CryptographyFixedSalt,
            keyDerivation: CryptographyMarker.Flexible.KeyDerivation.Argon2i = CryptographyMarker.Flexible.KeyDerivation.Default.argon2i,
        ): EncryptionEngine {
            val derivedKey = key.toByteArray().use {
                keyDerivationEngine.deriveKeyArgon2i(
                    it,
                    keyDerivation,
                    salt.requireSize(keyDerivation.saltLength),
                )
            }
            return createPasswordDerivation(
                derivedKey,
                keyDerivation,
                salt.requireSize(keyDerivation.saltLength),
            )
        }

        fun createPbkdf2(
            key: CryptographyKey.Password,
            salt: CryptographyFixedSalt,
            keyDerivation: CryptographyMarker.Flexible.KeyDerivation.Pbkdf2 = CryptographyMarker.Flexible.KeyDerivation.Default.pbkdf2,
        ): EncryptionEngine {
            val derivedKey = key.toByteArray().use {
                keyDerivationEngine.deriveKeyPbkdf2(
                    it,
                    keyDerivation,
                    salt.requireSize(keyDerivation.saltLength),
                )
            }
            return createPasswordDerivation(
                derivedKey,
                keyDerivation,
                salt.requireSize(keyDerivation.saltLength),
            )
        }

        private fun createPasswordDerivation(
            derivedKey: ByteArray,
            keyDerivation: CryptographyMarker.Flexible.KeyDerivation,
            salt: ByteArray,
        ): EncryptionEngineFlexibleAesCbcHmacImpl {
            val (cipherKey, macKey) = expandCipherMacKeyPair(derivedKey)
            val marker = CryptographyMarker.Flexible(keyDerivation)
            return create(marker, cipherKey, macKey, salt)
        }

        fun createNoDerivation(
            key: CryptographyKey.Raw32,
        ): EncryptionEngine {
            val (cipherKey, macKey) = key.toByteArray().use(::expandCipherMacKeyPair)
            val marker = CryptographyMarker.Flexible.Defaults.noDerivation
            return create(marker, cipherKey, macKey)
        }

        fun createNoDerivation64(
            key: CryptographyKey.Raw64,
        ): EncryptionEngine {
            val cipherKey = key.toByteArray(offset = 0, byteCount = AES_KEY_SIZE)
            val macKey = key.toByteArray(offset = AES_KEY_SIZE, byteCount = HMAC_VALUE_SIZE)
            return create(
                CryptographyMarker.Flexible(
                    CryptographyMarker.Flexible.KeyDerivation.None,
                    CryptographyMarker.Flexible.Cipher.Aes256.Default.cbcHmac64,
                ),
                cipherKey,
                macKey,
            )
        }

        private fun create(
            marker: CryptographyMarker.Flexible,
            cipherKey: ByteArray,
            macKey: ByteArray,
            salt: ByteArray = ByteArray(0),
        ) = EncryptionEngineFlexibleAesCbcHmacImpl(
            cipherFactory,
            macFactory,
            ivGenerator,
            marker,
            cipherKey,
            macKey,
            salt,
        )
    }
}

private fun BufferedSink.writeKwc3Header(
    salt: ByteArray,
): Sink = Kwc3HeaderSink(
    this,
    salt,
)

private class Kwc3HeaderSink(
    private val sink: BufferedSink,
    private val salt: ByteArray,
) : Sink {
    private var writeHeader = true
    private var closed = false

    override fun write(source: Buffer, byteCount: Long) {
        check(!closed) { "Closed" }
        if (writeHeader) {
            sink.writeHeader()
            writeHeader = false
        }
        sink.write(source, byteCount)
    }

    override fun flush() {
        check(!closed) { "Closed" }
        sink.flush()
    }

    override fun close() {
        if (closed) return
        closed = true

        sink.close()
    }

    override fun timeout(): Timeout =
        sink.timeout()

    private fun BufferedSink.writeHeader() {
        write(salt)
        writeUtf8(CryptographyMarker.Kwc3.value)
    }
}

private fun BufferedSink.writeKwc5Header(
    encryptionSink: EncryptionSink,
    mac: Mac,
    iv: ByteArray,
    padding: ByteArray,
): Sink = Kwc5HeaderSink(
    this,
    encryptionSink,
    mac,
    iv,
    padding,
)

private abstract class HeaderSink(
    private val sink: BufferedSink,
    private val encryptionSink: EncryptionSink,
    private val mac: Mac,
) : Sink {
    private var writeHeader = true
    private var closed = false

    final override fun write(source: Buffer, byteCount: Long) {
        check(!closed) { "Closed" }
        if (writeHeader) {
            val macValuePlaceholder = ByteArray(HMAC_VALUE_SIZE)
            sink.writeHeader(macValuePlaceholder)
            writeHeader = false
        }
        sink.write(source, byteCount)
    }

    final override fun flush() {
        check(!closed) { "Closed" }
        sink.flush()
    }

    final override fun close() {
        if (closed) return
        closed = true

        sink.close()
        val macValue = mac.doFinal()
        encryptionSink.open().use {
            it.writeHeader(macValue)
        }
    }

    override fun timeout(): Timeout =
        sink.timeout()

    abstract fun BufferedSink.writeHeader(macValue: ByteArray)
}

private class Kwc5HeaderSink(
    sink: BufferedSink,
    encryptionSink: EncryptionSink,
    mac: Mac,
    private val iv: ByteArray,
    private val padding: ByteArray,
) : HeaderSink(
        sink = sink,
        encryptionSink = encryptionSink,
        mac = mac,
    ) {
    override fun BufferedSink.writeHeader(macValue: ByteArray) {
        write(iv)
        write(padding)
        writeUtf8(CryptographyMarker.Kwc5.value)
        write(macValue)
    }
}

private fun BufferedSink.writeFlexibleHeader(
    encryptionSink: EncryptionSink,
    marker: CryptographyMarker.Flexible,
    salt: ByteArray,
    iv: ByteArray,
    mac: Mac,
): Sink = FlexibleHeaderSink(
    sink = this,
    encryptionSink = encryptionSink,
    mac = mac,
    marker = marker,
    salt = salt,
    iv = iv,
)

private class FlexibleHeaderSink(
    sink: BufferedSink,
    encryptionSink: EncryptionSink,
    mac: Mac,
    private val marker: CryptographyMarker.Flexible,
    private val salt: ByteArray,
    private val iv: ByteArray,
) : HeaderSink(
        sink = sink,
        encryptionSink = encryptionSink,
        mac = mac,
    ) {
    override fun BufferedSink.writeHeader(macValue: ByteArray) {
        writeUtf8(marker.value)
        write(salt)
        write(iv)
        write(macValue)
    }
}

private fun Sink.compressIf(compressed: Boolean): Sink =
    if (compressed) buffer().compress() else this

private fun BufferedSink.compress(): Sink =
    CompressionSink(this)

private class CompressionSink(private val sink: BufferedSink) : Sink {
    private val buffer = Buffer()
    private var closed = false

    override fun write(source: Buffer, byteCount: Long) {
        check(!closed) { "Closed" }
        buffer.write(source, byteCount)
    }

    override fun flush() {
        check(!closed) { "Closed" }
        buffer.flush()
    }

    override fun timeout(): Timeout =
        buffer.timeout()

    override fun close() {
        if (closed) return
        closed = true

        try {
            sink.writeInt(buffer.size.toInt())
            sink.deflate().use {
                it.write(buffer, buffer.size)
            }
        } finally {
            sink.close()
        }
    }
}