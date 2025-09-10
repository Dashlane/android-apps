package com.dashlane.cryptography

import okio.Buffer
import okio.CipherSource
import okio.Sink
import okio.Source
import okio.Timeout
import okio.cipherSink
import okio.cipherSource
import java.security.GeneralSecurityException

actual typealias Cipher = javax.crypto.Cipher

internal actual fun Source.cipherSource(cipher: Cipher): Source =
    cipherSource(cipher).adaptExceptions()

internal actual fun Sink.cipherSink(cipher: Cipher): Sink =
    cipherSink(cipher)

private fun CipherSource.adaptExceptions(): Source =
    CryptographyCipherExceptionAdapterSource(this)

private class CryptographyCipherExceptionAdapterSource(private val source: CipherSource) : Source {
    override fun read(sink: Buffer, byteCount: Long): Long =
        try {
            source.read(sink, byteCount)
        } catch (e: GeneralSecurityException) {
            throw CryptographyCipherException("Unable to decipher contents.", e)
        }

    override fun timeout(): Timeout =
        source.timeout()

    override fun close() =
        source.close()
}