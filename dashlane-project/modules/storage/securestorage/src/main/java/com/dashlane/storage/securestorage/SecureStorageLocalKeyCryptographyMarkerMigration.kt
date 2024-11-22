package com.dashlane.storage.securestorage

import com.dashlane.crypto.keys.AppKey
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyChanger
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.DecryptionEngine
import com.dashlane.cryptography.SaltGenerator
import com.dashlane.cryptography.changeCryptographyForBase64
import com.dashlane.cryptography.createEncryptionEngine
import com.dashlane.cryptography.generateFixedSalt
import com.dashlane.cryptography.getCryptographyMarker
import com.dashlane.storage.securestorage.cryptography.FlexibleDecryptionEngineFactory
import com.dashlane.user.Username
import javax.inject.Inject

class SecureStorageLocalKeyCryptographyMarkerMigration @Inject constructor(
    private val secureDataStorageFactory: SecureDataStorage.Factory,
    private val cryptography: Cryptography,
    private val saltGenerator: SaltGenerator
) {
    fun migrateLocalKeyIfNeeded(
        appKey: AppKey,
        username: Username,
        cryptographyMarker: CryptographyMarker
    ) {
        val secureDataStorage =
            secureDataStorageFactory.create(username, SecureDataStorage.Type.MASTER_PASSWORD_PROTECTED)
        val cipheredLocalKey = secureDataStorage.readLegacy(SecureDataKey.LOCAL_KEY) ?: return

        val currentMarker = cipheredLocalKey.getCryptographyMarker()

        
        if (currentMarker == cryptographyMarker) return

        val cipheredLocalKeyUpdatedMarker = appKey.use { key ->
            createEncryptionEngine(key, cryptographyMarker).use { encryptionEngine ->
                CryptographyChanger(createDecryptionEngine(key), encryptionEngine).use { cryptographyChanger ->
                    cryptographyChanger.changeCryptographyForBase64(cipheredLocalKey)
                }
            }
        }
        secureDataStorage.writeLegacy(SecureDataKey.LOCAL_KEY, cipheredLocalKeyUpdatedMarker)
    }

    private fun createEncryptionEngine(
        key: AppKey,
        marker: CryptographyMarker
    ) = key.cryptographyKey.use {
        cryptography.createEncryptionEngine(
            marker,
            it,
            saltGenerator.generateFixedSalt(marker)
        )
    }

    private fun createDecryptionEngine(key: AppKey): DecryptionEngine =
        FlexibleDecryptionEngineFactory(cryptography, key).createDecryptionEngine()
}
