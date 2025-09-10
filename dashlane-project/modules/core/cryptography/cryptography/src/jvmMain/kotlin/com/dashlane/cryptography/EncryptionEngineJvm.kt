package com.dashlane.cryptography

import okio.Sink
import okio.buffer
import okio.source
import java.io.File
import java.io.IOException

@Throws(IOException::class)
fun EncryptionEngine.encryptUtf8ToFile(
    string: String,
    outputFile: EncryptedFile,
    compressed: Boolean = false,
    fsync: Boolean = false,
) {
    encryptToFile(outputFile, compressed, fsync) { sink -> sink.buffer().use { it.writeUtf8(string) } }
}

@Throws(IOException::class)
fun EncryptionEngine.encryptFileToFile(
    inputFile: File,
    outputFile: EncryptedFile,
    compressed: Boolean = false,
    fsync: Boolean = false,
) {
    encryptToFile(outputFile, compressed, fsync) { sink -> inputFile.source().buffer().use { source -> source.readAll(sink) } }
}

@Throws(IOException::class)
inline fun EncryptionEngine.encryptToFile(
    outputFile: EncryptedFile,
    compressed: Boolean = false,
    fsync: Boolean = false,
    block: (Sink) -> Unit,
) {
    outputFile.asEncryptionSink(fsync).use { encryptionSink ->
        encrypt(encryptionSink, compressed).use(block)
    }
}