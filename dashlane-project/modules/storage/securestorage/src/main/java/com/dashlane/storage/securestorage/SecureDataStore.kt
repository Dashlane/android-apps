package com.dashlane.storage.securestorage

import com.dashlane.storage.securestorage.cryptography.SecureDataStoreCryptography

class SecureDataStore(
    private val secureDataStorage: SecureDataStorage,
    private val secureDataStoreCryptography: SecureDataStoreCryptography
) {
    fun isDataStored(identifier: String): Boolean =
        secureDataStorage.exists(identifier)

    fun storeData(identifier: String, data: ByteArray) {
        val encryptedData = secureDataStoreCryptography.encrypt(data)
        secureDataStorage.write(identifier, encryptedData)
    }

    fun retrieveData(identifier: String): ByteArray? {
        val encryptedData = secureDataStorage.read(identifier) ?: return null
        return secureDataStoreCryptography.decrypt(encryptedData)
    }

    fun removeData(identifier: String) {
        secureDataStorage.remove(identifier)
    }
}