package com.dashlane.cryptography

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import okio.ByteString.Companion.encodeUtf8
import java.io.File
import java.io.IOException

interface CryptographyAppKeyStore {
    operator fun get(alias: String): CryptographyKey.Raw32?

    operator fun set(alias: String, cryptographyKey: CryptographyKey.Raw32?)
}

@Suppress("FunctionName")
fun CryptographyAppKeyStore(
    context: Context,
    directory: File,
): CryptographyAppKeyStore = CryptographyAppKeyStoreImpl(
    context,
    directory,
)

private class CryptographyAppKeyStoreImpl(
    private val context: Context,
    private val directory: File,
) : CryptographyAppKeyStore {
    override operator fun get(alias: String): CryptographyKey.Raw32? {
        val file = getFile(alias)
        if (!file.exists()) return null

        val encryptedFile = createEncryptedFile(file)
        return try {
            encryptedFile.openFileInput().use {
                it.readBytes().use(CryptographyKey.Companion::ofBytes32)
            }
        } catch (e: IOException) {
            null
        }
    }

    @Suppress(
        
        "kotlin:S899",
    )
    override operator fun set(alias: String, cryptographyKey: CryptographyKey.Raw32?) {
        val file = getFile(alias)
        file.delete()
        if (cryptographyKey != null) {
            val encryptedFile = createEncryptedFile(file)
            encryptedFile.openFileOutput().use {
                cryptographyKey.toByteArray().use { bytes ->
                    it.write(bytes)
                }
            }
        }
    }

    private fun getFile(alias: String): File =
        File(directory, alias.encodeUtf8().base64Url() + ".key.aes")

    private fun createEncryptedFile(file: File): EncryptedFile {
        val masterKey = createMasterKey()
        return EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB,
        )
            .build()
    }

    private fun createMasterKey(): MasterKey =
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
}