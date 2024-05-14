package com.dashlane.storage.securestorage

import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.CryptographyKeyGenerator
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.DecryptionEngine
import com.dashlane.cryptography.EncryptionEngine
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.SaltGenerator
import com.dashlane.cryptography.createEncryptionEngine
import com.dashlane.cryptography.generateFixedSalt
import com.dashlane.cryptography.use
import com.dashlane.session.AppKey
import com.dashlane.session.LocalKey
import com.dashlane.session.Username
import com.dashlane.storage.securestorage.cryptography.FlexibleDecryptionEngineFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalKeyRepository @Inject constructor(
    private val secureStorageManager: SecureStorageManager,
    private val cryptography: Cryptography,
    private val saltGenerator: SaltGenerator,
    private val keyGenerator: CryptographyKeyGenerator
) {
    fun createLocalKey(
        username: Username,
        appKey: AppKey,
        cryptographyMarker: CryptographyMarker
    ): LocalKey {
        val localKey = keyGenerator.generateRaw32().use(::LocalKey)
        storeLocalKey(localKey, username, appKey, cryptographyMarker)
        return localKey
    }

    fun getLocalKey(username: Username, appKey: AppKey): LocalKey? {
        val secureDataStore = getMPSecureDataStore(
            username,
            FlexibleDecryptionEngineFactory(cryptography, appKey)
        )
        val localKeyBytes = secureDataStore.retrieveData(SecureDataKey.LOCAL_KEY) ?: return null
        if (localKeyBytes.size != 32) return null
        return CryptographyKey.ofBytes32(localKeyBytes).use(::LocalKey)
    }

    fun updateLocalKey(
        username: Username,
        appKey: AppKey,
        newAppKey: AppKey,
        cryptographyMarker: CryptographyMarker
    ) {
        val localKey = getLocalKey(username, appKey)
        checkNotNull(localKey)
        storeLocalKey(localKey, username, newAppKey, cryptographyMarker)
    }

    fun isLocalKeyCreated(username: Username): Boolean {
        return getMPSecureDataStorage(username).exists(SecureDataKey.LOCAL_KEY)
    }

    private fun storeLocalKey(
        localKey: LocalKey,
        username: Username,
        appKey: AppKey,
        cryptographyMarker: CryptographyMarker
    ) =
        appKey.cryptographyKey.use { cryptographyKey ->
            val cryptographyEngineFactory = CryptographyEngineFactoryImpl(
                cryptography,
                saltGenerator,
                cryptographyKey,
                cryptographyMarker
            )
            val secureDataStore = getMPSecureDataStore(username, cryptographyEngineFactory)
            localKey.cryptographyKeyBytes.use(ObfuscatedByteArray::toByteArray).use {
                secureDataStore.storeData(SecureDataKey.LOCAL_KEY, it)
            }
        }

    private fun getMPSecureDataStore(
        username: Username,
        cryptographyEngineFactory: CryptographyEngineFactory
    ): SecureDataStore {
        return secureStorageManager.getSecureDataStore(getMPSecureDataStorage(username), cryptographyEngineFactory)
    }

    private fun getMPSecureDataStorage(username: Username) =
        secureStorageManager.getSecureDataStorage(username, SecureDataStorage.Type.MASTER_PASSWORD_PROTECTED)
}

private class CryptographyEngineFactoryImpl(
    private val cryptography: Cryptography,
    private val saltGenerator: SaltGenerator,
    private val cryptographyKey: CryptographyKey,
    private val cryptographyMarker: CryptographyMarker
) : CryptographyEngineFactory {
    override fun createDecryptionEngine(): DecryptionEngine =
        cryptography.createDecryptionEngine(cryptographyKey)

    override fun createEncryptionEngine(): EncryptionEngine {
        
        val fixedSalt = saltGenerator.generateFixedSalt(cryptographyMarker)
        return cryptography.createEncryptionEngine(cryptographyMarker, cryptographyKey, fixedSalt)
    }
}
