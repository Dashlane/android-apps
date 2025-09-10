package com.dashlane.cryptography

import okio.Closeable
import okio.buffer

interface CryptographyChanger : Closeable {
    @Throws(CryptographyException::class)
    fun changeCryptography(
        decryptionSource: DecryptionSource,
        encryptionSink: EncryptionSink,
    )
}

@Throws(CryptographyException::class)
fun CryptographyChanger.changeCryptographyForByteArray(encryptedContent: EncryptedByteArray): EncryptedByteArray =
    runEncryptionToByteArray { encryptionSink -> changeCryptography(encryptedContent.asDecryptionSource(), encryptionSink) }

@Throws(CryptographyException::class)
fun CryptographyChanger.changeCryptographyForBase64(encryptedContent: EncryptedBase64String): EncryptedBase64String =
    runEncryptionToBase64 { encryptionSink -> changeCryptography(encryptedContent.asBase64DecryptionSource(), encryptionSink) }

@Suppress("FunctionName")
fun CryptographyChanger(
    decryptionEngine: DecryptionEngine,
    encryptionEngine: EncryptionEngine,
): CryptographyChanger = CryptographyChangerImpl(
    decryptionEngine,
    encryptionEngine,
)

private class CryptographyChangerImpl(
    private val decryptionEngine: DecryptionEngine,
    private val encryptionEngine: EncryptionEngine,
) : CryptographyChanger {
    override fun changeCryptography(
        decryptionSource: DecryptionSource,
        encryptionSink: EncryptionSink,
    ) {
        decryptionEngine.decrypt(decryptionSource) { source ->
            encryptionEngine.encrypt(encryptionSink).buffer().use { sink ->
                sink.writeAll(source)
            }
        }
    }

    override fun close() {
        decryptionEngine.close()
        encryptionEngine.close()
    }
}