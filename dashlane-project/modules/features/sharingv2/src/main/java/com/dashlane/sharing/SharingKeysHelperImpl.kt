package com.dashlane.sharing

import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.storage.securestorage.UserSecureStorageManager
import javax.inject.Inject

class SharingKeysHelperImpl @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val sessionManager: SessionManager
) : SharingKeysHelper {

    override var publicKey: String?
        get() = preferencesManager[sessionManager.session?.username].publicKey
        set(value) {
            preferencesManager[sessionManager.session?.username].publicKey = value
        }

    override var privateKey: String?
        get() = sessionManager.session?.let { userSecureStorageManager.readRsaPrivateKey(it.localKey, it.username) }
        set(value) {
            value?.let { sessionManager.session?.let { session -> userSecureStorageManager.storeRsaPrivateKey(session.localKey, session.username, it) } }
        }
}