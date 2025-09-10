package com.dashlane.cryptography

import okio.Source
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException

@Throws(CryptographyException::class, IOException::class)
fun DecryptionEngine.decryptFileToUtf8String(inputFile: EncryptedFile, compressed: Boolean = false): String =
    decryptFile(inputFile, compressed) { it.buffer().readUtf8() }

@Throws(CryptographyException::class, IOException::class)
fun DecryptionEngine.decryptFileToFile(inputFile: EncryptedFile, outputFile: File, compressed: Boolean = false) {
    decryptFile(inputFile, compressed) { source -> outputFile.sink().buffer().use { sink -> sink.writeAll(source) } }
}

@Throws(CryptographyException::class, IOException::class)
fun <T> DecryptionEngine.decryptFile(inputFile: EncryptedFile, compressed: Boolean = false, process: (Source) -> T): T =
    decrypt(inputFile.asDecryptionSource(), compressed, process)