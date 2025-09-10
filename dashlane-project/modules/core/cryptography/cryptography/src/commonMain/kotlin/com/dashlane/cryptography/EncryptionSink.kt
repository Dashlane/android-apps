package com.dashlane.cryptography

import okio.Buffer
import okio.BufferedSink
import okio.Closeable
import kotlin.math.min

interface EncryptionSink : Closeable {
    fun open(): BufferedSink

    override fun close()
}

inline fun runEncryptionToBase64(block: (EncryptionSink) -> Unit): EncryptedBase64String =
    runEncryptionToBuffer(block).readByteString().base64().asEncryptedBase64()

inline fun runEncryptionToByteArray(block: (EncryptionSink) -> Unit): EncryptedByteArray =
    runEncryptionToBuffer(block).readByteArray().asEncryptedByteArray()

inline fun runEncryptionToBuffer(block: (EncryptionSink) -> Unit) =
    BufferEncryptionSink().also(block).getBuffer()

class BufferEncryptionSink : EncryptionSink {
    private val buffers = mutableListOf<Buffer>()

    override fun open(): BufferedSink =
        Buffer().also { buffers += it }

    override fun close() {
        
    }

    fun getBuffer(): Buffer {
        val buffer = Buffer()
        buffers.asReversed().forEach {
            it.skip(min(buffer.size, it.size))
            buffer.writeAll(it)
        }
        return buffer
    }
}