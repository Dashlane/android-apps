package com.dashlane.storage.securestorage

import com.dashlane.cryptography.decodeUtf8ToString
import com.dashlane.cryptography.encodeUtf8ToByteArray
import com.dashlane.session.Session
import com.dashlane.session.Username

class UserSecureStorageManager(
    val secureStorageManager: SecureStorageManager
) {
    fun isSecretKeyStored(username: Username): Boolean =
        secureStorageManager.isKeyDataStored(username, SecureDataKey.SECRET_KEY)

    fun storeSecretKey(
        session: Session,
        data: String
    ) {
        session.localKey.use {
            secureStorageManager.storeKeyData(
                data.encodeToByteArray(),
                SecureDataKey.SECRET_KEY,
                session.username,
                it
            )
        }
    }

    fun storeSettings(session: Session, data: String) {
        session.localKey.use {
            secureStorageManager.storeKeyData(
                data.encodeToByteArray(),
                SecureDataKey.SETTINGS,
                session.username,
                it
            )
        }
    }

    fun readSettings(session: Session): String? =
        session.localKey.use {
            secureStorageManager.getKeyData(SecureDataKey.SETTINGS, session.username, it)?.decodeUtf8ToString()
        }

    fun storeRsaPrivateKey(session: Session, data: String) {
        session.localKey.use {
            secureStorageManager.storeKeyData(
                data.encodeToByteArray(),
                SecureDataKey.RSA_PRIVATE_KEY,
                session.username,
                it
            )
        }
    }

    fun readRsaPrivateKey(session: Session): String? =
        session.localKey.use {
            secureStorageManager.getKeyData(SecureDataKey.RSA_PRIVATE_KEY, session.username, it)?.decodeUtf8ToString()
        }

    fun storePin(session: Session?, data: String) {
        session ?: return
        session.localKey.use {
            secureStorageManager.storeKeyData(
                data.encodeToByteArray(),
                SecureDataKey.PIN_CODE,
                session.username,
                it
            )
        }
    }

    fun readPin(session: Session): String? =
        session.localKey.use {
            secureStorageManager.getKeyData(SecureDataKey.PIN_CODE, session.username, it)
            ?.decodeUtf8ToString()
        }

    fun wipePin(username: Username) {
        val secureDataStorage = secureStorageManager.getSecureDataStorage(username, SecureDataStorage.Type.LOCAL_KEY_PROTECTED)
        secureStorageManager.removeKeyData(secureDataStorage, SecureDataKey.PIN_CODE)
    }

    fun storeUserFeature(session: Session, data: String) =
        session.localKey.use { secureStorageManager.storeKeyData(data.encodeToByteArray(), SecureDataKey.USER_FEATURE_FLIPS, session.username, it) }

    fun readUserFeature(session: Session): String? =
        session.localKey.use {
            secureStorageManager.getKeyData(SecureDataKey.USER_FEATURE_FLIPS, session.username, it)
            ?.decodeUtf8ToString()
        }

    fun wipeUserData(username: Username) {
        secureStorageManager.wipeUserData(username)
    }

    fun storePremiumServerStatus(session: Session, data: String) {
        session.localKey.use {
            secureStorageManager.storeKeyData(data.encodeToByteArray(), SecureDataKey.PREMIUM_SERVER_RESPONSE, session.username, it)
        }
    }

    fun readPremiumServerStatus(session: Session): String? =
        session.localKey.use { secureStorageManager.getKeyData(SecureDataKey.PREMIUM_SERVER_RESPONSE, session.username, it) }
            ?.decodeUtf8ToString()

    fun storeDeviceAnalyticsId(session: Session, id: String?) {
        if (id == null) {
            val secureDataStorage = secureStorageManager.getSecureDataStorage(session.username, SecureDataStorage.Type.LOCAL_KEY_PROTECTED)
            secureStorageManager.removeKeyData(secureDataStorage, SecureDataKey.DEVICE_ANALYTICS_ID)
        } else {
            secureStorageManager.storeKeyData(
                id.encodeUtf8ToByteArray(),
                SecureDataKey.DEVICE_ANALYTICS_ID,
                session.username,
                session.localKey
            )
        }
    }

    fun readDeviceAnalyticsId(session: Session): String? {
        return secureStorageManager.getKeyData(
            SecureDataKey.DEVICE_ANALYTICS_ID,
            session.username,
            session.localKey
        )?.decodeUtf8ToString()
    }

    fun storeUserAnalyticsId(session: Session, id: String?) {
        if (id == null) {
            val secureDataStorage = secureStorageManager.getSecureDataStorage(session.username, SecureDataStorage.Type.LOCAL_KEY_PROTECTED)
            secureStorageManager.removeKeyData(secureDataStorage, SecureDataKey.USER_ANALYTICS_ID)
        } else {
            secureStorageManager.storeKeyData(
                id.encodeUtf8ToByteArray(),
                SecureDataKey.USER_ANALYTICS_ID,
                session.username,
                session.localKey
            )
        }
    }

    fun readUserAnalyticsId(session: Session): String? {
        return secureStorageManager.getKeyData(
            SecureDataKey.USER_ANALYTICS_ID,
            session.username,
            session.localKey
        )?.decodeUtf8ToString()
    }

    fun storeUserActivity(session: Session, id: String?) {
        if (id == null) {
            val secureDataStorage = secureStorageManager.getSecureDataStorage(session.username, SecureDataStorage.Type.LOCAL_KEY_PROTECTED)
            secureStorageManager.removeKeyData(secureDataStorage, SecureDataKey.USER_ACTIVITY)
        } else {
            secureStorageManager.storeKeyData(
                id.encodeUtf8ToByteArray(),
                SecureDataKey.USER_ACTIVITY,
                session.username,
                session.localKey
            )
        }
    }

    fun readUserActivity(session: Session): String? {
        return secureStorageManager.getKeyData(
            SecureDataKey.USER_ACTIVITY,
            session.username,
            session.localKey
        )?.decodeUtf8ToString()
    }
}
