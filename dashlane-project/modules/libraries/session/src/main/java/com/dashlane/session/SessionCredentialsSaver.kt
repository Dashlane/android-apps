package com.dashlane.session

import com.dashlane.crypto.keys.LocalKey
import com.dashlane.crypto.keys.serverKeyUtf8Bytes
import com.dashlane.crypto.keys.userKeyBytes
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.asEncryptedBase64
import com.dashlane.cryptography.encodeBase64ToString
import com.dashlane.cryptography.use
import com.dashlane.logger.Log
import com.dashlane.preference.PreferencesManager
import com.dashlane.storage.securestorage.SecureDataKey
import com.dashlane.storage.securestorage.SecureDataStorage
import com.dashlane.storage.securestorage.SecureStorageManager
import com.dashlane.user.Username
import com.dashlane.hardwaresecurity.CryptoObjectHelper
import javax.inject.Inject

private const val TAG = "LockCredentialSaver"

class SessionCredentialsSaver @Inject constructor(
    private val secureStorageManager: SecureStorageManager,
    private val preferencesManager: PreferencesManager,
    private val cryptoObjectHelper: CryptoObjectHelper
) {
    fun saveCredentials(session: Session) {
        session.appKey.use { appKey ->
            
            
            
            appKey.userKeyBytes.use(ObfuscatedByteArray::toByteArray).use {
                secureStorageManager.storeKeyData(
                    keyData = it,
                    keyIdentifier = SecureDataKey.MASTER_PASSWORD,
                    username = session.username,
                    localKey = session.localKey
                )
            }

            saveLocalKey(session.localKey, session.username)

            
            
            if (preferencesManager[session.username].is2FADisabled) {
                appKey.serverKeyUtf8Bytes?.let { saveServerKey(it.use(ObfuscatedByteArray::toByteArray), session.localKey, session.username) }
            }
        }
    }

    fun saveLocalKey(localKey: LocalKey, username: Username) {
        try {
            val keystoreKey = CryptoObjectHelper.LocalKeyLock(username = username.email)
            cryptoObjectHelper.createEncryptionKey(keyStoreKey = keystoreKey, isUserAuthenticationRequired = false)
            val encrypted = cryptoObjectHelper.encrypt(keystoreKey, localKey.cryptographyKeyBytes.use(ObfuscatedByteArray::toByteArray)) ?: return
            val storage = secureStorageManager.getSecureDataStorage(username, SecureDataStorage.Type.ANDROID_KEYSTORE_PROTECTED)
            storage.writeLegacy(SecureDataKey.LOCAL_KEY, encrypted.encodeBase64ToString().asEncryptedBase64())
        } catch (e: Exception) {
            Log.w(TAG, "Exception raised when saving the LK", e)
        }
    }

    fun saveServerKey(serverKey: ByteArray?, localKey: LocalKey, username: Username) {
        preferencesManager[username].is2FADisabled = serverKey != null
        serverKey?.use {
            secureStorageManager.storeKeyData(
                keyData = it,
                keyIdentifier = SecureDataKey.SERVER_KEY,
                username = username,
                localKey = localKey
            )
        }
    }

    fun removeServerKey(username: Username) {
        preferencesManager[username].is2FADisabled = false
        val secureDataStorage = secureStorageManager.getSecureDataStorage(username, SecureDataStorage.Type.LOCAL_KEY_PROTECTED)
        secureStorageManager.removeKeyData(secureDataStorage, SecureDataKey.SERVER_KEY)
    }

    fun hasServerKey(username: Username) = secureStorageManager.isKeyDataStored(username, SecureDataKey.SERVER_KEY)

    fun areCredentialsSaved(username: Username): Boolean {
        val secureDataStorage = secureStorageManager.getSecureDataStorage(username, SecureDataStorage.Type.ANDROID_KEYSTORE_PROTECTED)
        return secureDataStorage.readLegacy(SecureDataKey.LOCAL_KEY) != null
    }

    fun deleteSavedCredentials(username: Username?) {
        username?.let {
            var secureDataStorage = secureStorageManager.getSecureDataStorage(username, SecureDataStorage.Type.LOCAL_KEY_PROTECTED)
            secureStorageManager.removeKeyData(secureDataStorage, SecureDataKey.MASTER_PASSWORD)
            secureDataStorage = secureStorageManager.getSecureDataStorage(username, SecureDataStorage.Type.ANDROID_KEYSTORE_PROTECTED)
            secureStorageManager.removeKeyData(secureDataStorage, SecureDataKey.LOCAL_KEY)
            cryptoObjectHelper.deleteEncryptionKey(CryptoObjectHelper.LocalKeyLock(username.email))
            removeServerKey(username)
        }
    }
}
