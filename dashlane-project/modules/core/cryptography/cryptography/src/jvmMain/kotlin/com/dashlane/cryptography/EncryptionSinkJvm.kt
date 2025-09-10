package com.dashlane.cryptography

import okio.BufferedSink
import okio.Sink
import okio.buffer
import okio.sink
import java.io.File
import java.io.FileDescriptor
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.channels.WritableByteChannel

fun EncryptedFile.asEncryptionSink(fsyncOnClose: Boolean = false): EncryptionSink =
    FileEncryptionSink(value, fsyncOnClose)

private class FileEncryptionSink private constructor(
    private val randomAccessFile: RandomAccessFile,
    private val fsyncOnClose: Boolean,
) : EncryptionSink {
    private var closed: Boolean = false

    private val fileChannel: FileChannel
        get() = randomAccessFile.channel

    private val fileDescriptor: FileDescriptor
        get() = randomAccessFile.fd

    constructor(file: File, fsyncOnClose: Boolean) : this(RandomAccessFile(file, "rw"), fsyncOnClose = fsyncOnClose)

    override fun open(): BufferedSink {
        check(!closed) { "Closed" }
        randomAccessFile.seek(0)
        return createBufferedSink()
    }

    private fun createBufferedSink() =
        fileChannel.outputStream().sink().ignoreClose().buffer()

    override fun close() {
        if (closed) return
        closed = true

        if (fsyncOnClose) {
            fileChannel.use {
                fileDescriptor.sync()
            }
        } else {
            fileChannel.close()
        }
    }
}

private fun Sink.ignoreClose(): Sink =
    object : Sink by this {
        override fun close() {
            
        }
    }

private fun WritableByteChannel.outputStream(): OutputStream =
    Channels.newOutputStream(this)