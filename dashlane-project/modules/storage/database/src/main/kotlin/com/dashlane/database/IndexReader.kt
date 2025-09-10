package com.dashlane.database

import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.cryptography.CryptographyException
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.DecryptionEngine
import com.dashlane.cryptography.EncryptionEngine
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonEncodingException
import okio.buffer

internal class IndexReader(
    private val files: DatabaseFiles,
    private val cryptography: Cryptography,
) {
    private val jsonAdapter: JsonAdapter<DatabaseIndex> =
        DatabaseIndex.JSON_ADAPTER

    fun read(key: CryptographyKey.Raw32): DatabaseIndex? {
        val factory = CryptographyEngineFactoryIndexImpl(cryptography, key)
        val reader = DatabaseReader(factory)
        val file = files.index
        return try {
            reader.read(file) { source ->
                jsonAdapter.fromJson(source.buffer())
            }
        } catch (e: JsonEncodingException) {
            throw CryptographyException(message = "Wrong key")
        }
    }

    private class CryptographyEngineFactoryIndexImpl(
        private val cryptography: Cryptography,
        key: CryptographyKey.Raw32,
    ) : CryptographyEngineFactory {
        private val key: CryptographyKey.Raw32 = key.clone()

        override fun createDecryptionEngine(): DecryptionEngine {
            return cryptography.createDecryptionEngine(key)
        }

        override fun createEncryptionEngine(): EncryptionEngine {
            throw UnsupportedOperationException()
        }
    }
}