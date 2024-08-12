package com.dashlane.session

import com.dashlane.database.DatabaseProvider
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.user.Username
import javax.inject.Inject

class SessionTrasher @Inject constructor(
    private val userSecureStorageManager: UserSecureStorageManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val databaseProvider: DatabaseProvider,
    private val sessionCredentialsSaver: SessionCredentialsSaver
) {
    suspend fun trash(username: Username, deletePreferences: Boolean = true) {
        username.let {
            userSecureStorageManager.wipeUserData(it)
            if (deletePreferences) {
                userPreferencesManager.preferencesFor(it).clear()
                globalPreferencesManager.deleteBackupToken(it)
            }
            databaseProvider.delete(it.email)
        }
        sessionCredentialsSaver.deleteSavedCredentials(username)
    }
}
