package com.dashlane.storage.securestorage

import com.dashlane.cryptography.decodeUtf8ToString
import com.dashlane.cryptography.encodeUtf8ToByteArray
import com.dashlane.crypto.keys.LocalKey
import com.dashlane.user.Username

class UserSecureStorageManager(
    val secureStorageManager: SecureStorageManager
) {
    fun isSecretKeyStored(username: Username): Boolean =
        secureStorageManager.isKeyDataStored(username, SecureDataKey.SECRET_KEY)

    fun storeSecretKey(
        localKey: LocalKey,
        username: Username,
        data: String
    ) {
        localKey.use {
            secureStorageManager.storeKeyData(
                data.encodeToByteArray(),
                SecureDataKey.SECRET_KEY,
                username,
                it
            )
        }
    }

    fun storeSettings(localKey: LocalKey, username: Username, data: String) {
        localKey.use {
            secureStorageManager.storeKeyData(
                data.encodeToByteArray(),
                SecureDataKey.SETTINGS,
                username,
                it
            )
        }
    }

    fun readSettings(localKey: LocalKey, username: Username): String? =
        localKey.use {
            secureStorageManager.getKeyData(SecureDataKey.SETTINGS, username, it)?.decodeUtf8ToString()
        }

    fun storeRsaPrivateKey(localKey: LocalKey, username: Username, data: String) {
        localKey.use {
            secureStorageManager.storeKeyData(
                data.encodeToByteArray(),
                SecureDataKey.RSA_PRIVATE_KEY,
                username,
                it
            )
        }
    }

    fun readRsaPrivateKey(localKey: LocalKey, username: Username): String? =
        localKey.use {
            secureStorageManager.getKeyData(SecureDataKey.RSA_PRIVATE_KEY, username, it)?.decodeUtf8ToString()
        }

    fun storePin(localKey: LocalKey, username: Username, data: String) {
        localKey.use {
            secureStorageManager.storeKeyData(
                data.encodeToByteArray(),
                SecureDataKey.PIN_CODE,
                username,
                it
            )
        }
    }

    fun readPin(localKey: LocalKey, username: Username): String? =
        localKey.use {
            secureStorageManager.getKeyData(SecureDataKey.PIN_CODE, username, it)
                ?.decodeUtf8ToString()
        }

    fun wipePin(username: Username) {
        val secureDataStorage = secureStorageManager.getSecureDataStorage(username, SecureDataStorage.Type.LOCAL_KEY_PROTECTED)
        secureStorageManager.removeKeyData(secureDataStorage, SecureDataKey.PIN_CODE)
    }

    fun storeUserFeature(localKey: LocalKey, username: Username, data: String) =
        localKey.use { secureStorageManager.storeKeyData(data.encodeToByteArray(), SecureDataKey.USER_FEATURE_FLIPS, username, it) }

    fun readUserFeature(localKey: LocalKey, username: Username): String? =
        localKey.use {
            secureStorageManager.getKeyData(SecureDataKey.USER_FEATURE_FLIPS, username, it)
                ?.decodeUtf8ToString()
        }

    fun wipeUserData(username: Username) {
        secureStorageManager.wipeUserData(username)
    }

    fun storeAccountStatus(
        localKey: LocalKey,
        username: Username,
        jsonStatus: String
    ) {
        localKey.use { lk ->
            secureStorageManager.storeKeyData(
                keyData = jsonStatus.encodeToByteArray(),
                keyIdentifier = SecureDataKey.ACCOUNT_STATUS_CACHE,
                username = username,
                localKey = lk
            )
        }
    }

    fun readAccountStatus(localKey: LocalKey, username: Username): String? =
        localKey.use { lk ->
            secureStorageManager.getKeyData(
                keyIdentifier = SecureDataKey.ACCOUNT_STATUS_CACHE,
                username = username,
                localKey = lk
            )
        }?.decodeUtf8ToString()

    fun storeDeviceAnalyticsId(localKey: LocalKey, username: Username, id: String?) {
        if (id == null) {
            val secureDataStorage = secureStorageManager.getSecureDataStorage(username, SecureDataStorage.Type.LOCAL_KEY_PROTECTED)
            secureStorageManager.removeKeyData(secureDataStorage, SecureDataKey.DEVICE_ANALYTICS_ID)
        } else {
            secureStorageManager.storeKeyData(
                keyData = id.encodeUtf8ToByteArray(),
                keyIdentifier = SecureDataKey.DEVICE_ANALYTICS_ID,
                username = username,
                localKey = localKey
            )
        }
    }

    fun readDeviceAnalyticsId(localKey: LocalKey, username: Username): String? {
        return secureStorageManager.getKeyData(
            SecureDataKey.DEVICE_ANALYTICS_ID,
            username,
            localKey
        )?.decodeUtf8ToString()
    }

    fun storeUserAnalyticsId(localKey: LocalKey, username: Username, id: String?) {
        if (id == null) {
            val secureDataStorage = secureStorageManager.getSecureDataStorage(username, SecureDataStorage.Type.LOCAL_KEY_PROTECTED)
            secureStorageManager.removeKeyData(secureDataStorage, SecureDataKey.USER_ANALYTICS_ID)
        } else {
            secureStorageManager.storeKeyData(
                keyData = id.encodeUtf8ToByteArray(),
                keyIdentifier = SecureDataKey.USER_ANALYTICS_ID,
                username = username,
                localKey = localKey
            )
        }
    }

    fun readUserAnalyticsId(localKey: LocalKey, username: Username): String? {
        return secureStorageManager.getKeyData(
            SecureDataKey.USER_ANALYTICS_ID,
            username,
            localKey
        )?.decodeUtf8ToString()
    }

    fun storeUserActivity(localKey: LocalKey, username: Username, id: String?) {
        if (id == null) {
            val secureDataStorage = secureStorageManager.getSecureDataStorage(username, SecureDataStorage.Type.LOCAL_KEY_PROTECTED)
            secureStorageManager.removeKeyData(secureDataStorage, SecureDataKey.USER_ACTIVITY)
        } else {
            secureStorageManager.storeKeyData(
                id.encodeUtf8ToByteArray(),
                SecureDataKey.USER_ACTIVITY,
                username,
                localKey
            )
        }
    }

    fun readUserActivity(localKey: LocalKey, username: Username): String? {
        return secureStorageManager.getKeyData(
            keyIdentifier = SecureDataKey.USER_ACTIVITY,
            username = username,
            localKey = localKey
        )?.decodeUtf8ToString()
    }

    fun storeCurrentSpaceFilter(localKey: LocalKey, username: Username, filterId: String) {
        secureStorageManager.storeKeyData(
            keyData = filterId.encodeUtf8ToByteArray(),
            keyIdentifier = SecureDataKey.CURRENT_SPACE_ID_FILTER,
            username = username,
            localKey = localKey
        )
    }

    fun readCurrentSpaceFilter(localKey: LocalKey, username: Username): String? {
        return secureStorageManager.getKeyData(
            keyIdentifier = SecureDataKey.CURRENT_SPACE_ID_FILTER,
            username = username,
            localKey = localKey
        )?.decodeUtf8ToString()
    }
}
