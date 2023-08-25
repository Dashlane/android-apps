package com.dashlane.storage.securestorage.cryptography

import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.cryptography.EncryptedBase64String
import com.dashlane.cryptography.decodeBase64ToByteArrayOrNull
import com.dashlane.cryptography.decryptBase64ToByteArrayOrNull
import com.dashlane.cryptography.decryptBase64ToUtf8StringOrNull
import com.dashlane.cryptography.encodeBase64ToString
import com.dashlane.cryptography.encryptByteArrayToBase64String
import com.dashlane.cryptography.encryptUtf8ToBase64String

interface SecureDataStoreCryptography {
    fun encrypt(byteArray: ByteArray): EncryptedBase64String
    fun decrypt(encryptedData: EncryptedBase64String): ByteArray?
}

class SecureDataStoreCryptographyMpProtectedImpl(
    private val cryptographyEngineFactory: CryptographyEngineFactory
) : SecureDataStoreCryptography {
    override fun encrypt(byteArray: ByteArray): EncryptedBase64String {
        val base64Data = byteArray.encodeBase64ToString()
        return cryptographyEngineFactory.createEncryptionEngine().use { encryptionEngine ->
            encryptionEngine.encryptUtf8ToBase64String(base64Data, compressed = true)
        }
    }

    override fun decrypt(encryptedData: EncryptedBase64String): ByteArray? {
        val base64Data = cryptographyEngineFactory.createDecryptionEngine().use { decryptionEngine ->
            decryptionEngine.decryptBase64ToUtf8StringOrNull(encryptedData, compressed = true)
        }
        return base64Data?.decodeBase64ToByteArrayOrNull()
    }
}

class SecureDataStoreCryptographyRawKeyProtectedImpl(
    private val cryptographyEngineFactory: CryptographyEngineFactory
) : SecureDataStoreCryptography {
    override fun encrypt(byteArray: ByteArray): EncryptedBase64String =
        cryptographyEngineFactory.createEncryptionEngine().use { encryptionEngine ->
            encryptionEngine.encryptByteArrayToBase64String(byteArray, compressed = false)
        }

    override fun decrypt(encryptedData: EncryptedBase64String): ByteArray? =
        cryptographyEngineFactory.createDecryptionEngine().use { decryptionEngine ->
            decryptionEngine.decryptBase64ToByteArrayOrNull(encryptedData, compressed = false)
        }
}