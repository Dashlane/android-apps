package com.dashlane.session

import com.dashlane.database.DatabaseProvider
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.user.Username
import javax.inject.Inject

fun interface SessionTrasher {
    suspend fun trash(username: Username, deletePreferences: Boolean)
}

class SessionTrasherImpl @Inject constructor(
    private val userSecureStorageManager: UserSecureStorageManager,
    private val preferencesManager: PreferencesManager,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val databaseProvider: DatabaseProvider,
    private val sessionCredentialsSaver: SessionCredentialsSaver
) : SessionTrasher {
    override suspend fun trash(username: Username, deletePreferences: Boolean) {
        username.let {
            userSecureStorageManager.wipeUserData(it)
            if (deletePreferences) {
                preferencesManager[it].clear()
                globalPreferencesManager.deleteBackupToken(it)
            }
            databaseProvider.delete(it.email)
        }
        sessionCredentialsSaver.deleteSavedCredentials(username)
    }
}
