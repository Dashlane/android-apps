package com.dashlane.sharing

import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.storage.securestorage.UserSecureStorageManager
import javax.inject.Inject

class SharingKeysHelperImpl @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val sessionManager: SessionManager
) : SharingKeysHelper {

    override var publicKey: String?
        get() = userPreferencesManager.publicKey
        set(value) {
            userPreferencesManager.publicKey = value
        }

    override var privateKey: String?
        get() = sessionManager.session?.let { userSecureStorageManager.readRsaPrivateKey(it.localKey, it.username) }
        set(value) {
            value?.let { sessionManager.session?.let { session -> userSecureStorageManager.storeRsaPrivateKey(session.localKey, session.username, it) } }
        }
}