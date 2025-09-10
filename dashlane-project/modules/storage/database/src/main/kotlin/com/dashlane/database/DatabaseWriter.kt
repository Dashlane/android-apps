package com.dashlane.database

import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.cryptography.CryptographyException
import com.dashlane.cryptography.asEncryptedFile
import com.dashlane.cryptography.asEncryptionSink
import okio.BufferedSink
import okio.buffer
import java.io.File

interface DatabaseWriter {
    @Throws(DatabaseCryptographyException::class)
    fun write(file: File, block: (BufferedSink) -> Unit)

    fun delete(file: File)
}

@Suppress("FunctionName")
fun DatabaseWriter(cryptographyEngineFactory: CryptographyEngineFactory): DatabaseWriter =
    DatabaseWriterImpl(cryptographyEngineFactory)

private class DatabaseWriterImpl(
    private val cryptography: CryptographyEngineFactory,
) : DatabaseWriter {
    override fun write(file: File, block: (BufferedSink) -> Unit) {
        file.parentFile?.mkdirs()
        try {
            cryptography.createEncryptionEngine()
                .use { engine ->
                    file.asEncryptedFile().asEncryptionSink(fsyncOnClose = true).use {
                        engine.encrypt(encryptionSink = it, compressed = true).buffer().use(block)
                    }
                }
        } catch (e: CryptographyException) {
            throw DatabaseCryptographyException(e)
        }
    }

    override fun delete(file: File) {
        file.delete()
    }
}