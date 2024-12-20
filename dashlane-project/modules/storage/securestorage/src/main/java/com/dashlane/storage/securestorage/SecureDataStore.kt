package com.dashlane.storage.securestorage

import com.dashlane.storage.securestorage.cryptography.SecureDataStoreCryptography

class SecureDataStore(
    private val secureDataStorage: SecureDataStorage,
    private val secureDataStoreCryptography: SecureDataStoreCryptography
) {

    fun storeData(identifier: String, data: ByteArray) {
        val encryptedData = secureDataStoreCryptography.encrypt(data)
        secureDataStorage.writeLegacy(identifier, encryptedData)
    }

    fun retrieveData(identifier: String): ByteArray? {
        val encryptedData = secureDataStorage.readLegacy(identifier) ?: return null
        return secureDataStoreCryptography.decrypt(encryptedData)
    }
}