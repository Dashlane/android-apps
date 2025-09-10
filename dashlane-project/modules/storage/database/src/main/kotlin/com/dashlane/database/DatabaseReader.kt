package com.dashlane.database

import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.cryptography.CryptographyException
import com.dashlane.cryptography.asDecryptionSource
import com.dashlane.cryptography.asEncryptedFile
import okio.FileNotFoundException
import okio.Source
import java.io.File

internal interface DatabaseReader {
    @Throws(DatabaseCryptographyException::class)
    fun <T> read(file: File, block: (Source) -> T): T?
}

@Suppress("FunctionName")
internal fun DatabaseReader(cryptographyEngineFactory: CryptographyEngineFactory): DatabaseReader =
    DatabaseReaderImpl(cryptographyEngineFactory)

private class DatabaseReaderImpl(
    private val cryptographyEngineFactory: CryptographyEngineFactory,
) : DatabaseReader {
    override fun <T> read(file: File, block: (Source) -> T): T? {
        if (!file.exists()) return null
        return try {
            cryptographyEngineFactory.createDecryptionEngine().use { engine ->
                engine.decrypt(
                    decryptionSource = file.asEncryptedFile().asDecryptionSource(),
                    compressed = true,
                    block = block,
                )
            }
        } catch (e: CryptographyException) {
            throw DatabaseCryptographyException(e)
        } catch (e: FileNotFoundException) {
            
            
            if (file.exists()) {
                throw e
            }
            null
        }
    }
}