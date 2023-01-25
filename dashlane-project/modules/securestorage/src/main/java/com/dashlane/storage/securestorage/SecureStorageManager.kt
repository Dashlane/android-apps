package com.dashlane.storage.securestorage

import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.DecryptionEngine
import com.dashlane.cryptography.EncryptionEngine
import com.dashlane.session.LocalKey
import com.dashlane.session.Username
import com.dashlane.storage.securestorage.cryptography.SecureDataStoreCryptographyMpProtectedImpl
import com.dashlane.storage.securestorage.cryptography.SecureDataStoreCryptographyRawKeyProtectedImpl
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class SecureStorageManager @Inject constructor(
    private val cryptography: Cryptography,
    private val secureDataStorageFactory: SecureDataStorage.Factory
) {
    

    fun isKeyDataStored(username: Username, @SecureDataKey.Key keyIdentifier: String): Boolean {
        val mpSecureDataStore = getSecureDataStorage(username, SecureDataStorage.Type.MASTER_PASSWORD_PROTECTED)
        val lkSecureDataStore = getSecureDataStorage(username, SecureDataStorage.Type.LOCAL_KEY_PROTECTED)
        return mpSecureDataStore.exists(SecureDataKey.LOCAL_KEY) && lkSecureDataStore.exists(keyIdentifier)
    }

    fun getKeyData(
        @SecureDataKey.Key
        keyIdentifier: String,
        username: Username,
        localKey: LocalKey
    ): ByteArray? {
        localKey.cryptographyKey.use { cryptographyKey ->
            val cryptographyEngineFactory: CryptographyEngineFactory = CryptographyEngineFactoryLocalKeyImpl(cryptography, cryptographyKey)
            val secureDataStorage = getSecureDataStorage(username, SecureDataStorage.Type.LOCAL_KEY_PROTECTED)
            val secureDataStore = getSecureDataStore(secureDataStorage, cryptographyEngineFactory)
            return secureDataStore.retrieveData(keyIdentifier)
        }
    }

    fun storeKeyData(
        keyData: ByteArray,
        @SecureDataKey.Key
        keyIdentifier: String,
        username: Username,
        localKey: LocalKey
    ) {
        localKey.cryptographyKey.use { cryptographyKey ->
            val cryptographyEngineFactory: CryptographyEngineFactory = CryptographyEngineFactoryLocalKeyImpl(cryptography, cryptographyKey)
            val secureDataStorage = getSecureDataStorage(username, SecureDataStorage.Type.LOCAL_KEY_PROTECTED)
            val secureDataStore = getSecureDataStore(secureDataStorage, cryptographyEngineFactory)
            secureDataStore.storeData(keyIdentifier, keyData)
        }
    }

    fun removeKeyData(secureDataStorage: SecureDataStorage, @SecureDataKey.Key keyIdentifier: String) {
        secureDataStorage.remove(keyIdentifier)
    }

    

    fun wipeUserData(username: Username) {
        
        getSecureDataStorage(username, SecureDataStorage.Type.MASTER_PASSWORD_PROTECTED).remove(SecureDataKey.LOCAL_KEY)
        
        getSecureDataStorage(username, SecureDataStorage.Type.LOCAL_KEY_PROTECTED)
            .apply {
                remove(SecureDataKey.SECRET_KEY)
                remove(SecureDataKey.SETTINGS)
                remove(SecureDataKey.REMOTE_KEY)
            }

        
        getSecureDataStorage(username, SecureDataStorage.Type.ANDROID_KEYSTORE_PROTECTED).remove(SecureDataKey.LOCAL_KEY)

        
        getSecureDataStorage(username, SecureDataStorage.Type.RECOVERY_KEY_PROTECTED).remove(SecureDataKey.LOCAL_KEY)
    }

    fun getSecureDataStorage(username: Username, secureDataStorageType: SecureDataStorage.Type): SecureDataStorage {
        return secureDataStorageFactory.create(username, secureDataStorageType)
    }

    fun getSecureDataStore(
        secureDataStorage: SecureDataStorage,
        cryptographyEngineFactory: CryptographyEngineFactory
    ): SecureDataStore {
        val secureDataStoreCryptography = when (secureDataStorage.type) {
            SecureDataStorage.Type.LOCAL_KEY_PROTECTED -> SecureDataStoreCryptographyRawKeyProtectedImpl(cryptographyEngineFactory)
            SecureDataStorage.Type.MASTER_PASSWORD_PROTECTED -> SecureDataStoreCryptographyMpProtectedImpl(cryptographyEngineFactory)
            SecureDataStorage.Type.RECOVERY_KEY_PROTECTED -> SecureDataStoreCryptographyRawKeyProtectedImpl(cryptographyEngineFactory)
            SecureDataStorage.Type.ANDROID_KEYSTORE_PROTECTED -> throw IllegalArgumentException("No SecureDataStore for this SecureDataStorage type")
        }
        return SecureDataStore(secureDataStorage, secureDataStoreCryptography)
    }

    private class CryptographyEngineFactoryLocalKeyImpl(
        private val cryptography: Cryptography,
        private val localKey: CryptographyKey.Raw32
    ) : CryptographyEngineFactory {

        override fun createDecryptionEngine(): DecryptionEngine {
            return cryptography.createDecryptionEngine(localKey)
        }

        override fun createEncryptionEngine(): EncryptionEngine {
            return cryptography.createFlexibleNoDerivationEncryptionEngine(localKey)
        }
    }
}
