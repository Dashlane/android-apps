package com.dashlane.session

import android.content.Context
import com.dashlane.CipherDatabaseUtils
import com.dashlane.database.DatabaseProvider
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.usersupportreporter.UserSupportFileLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject



class SessionTrasher @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val userSupportFileLogger: UserSupportFileLogger,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val databaseProvider: DatabaseProvider,
    private val sessionCredentialsSaver: SessionCredentialsSaver
) {
    suspend fun trash(username: Username, deletePreferences: Boolean = true) {
        userSupportFileLogger.add("SessionTrasher -> trash()")
        username.let {
            userSupportFileLogger.add("SessionTrasher -> trash() for $it")
            userSecureStorageManager.wipeUserData(it)
            if (deletePreferences) {
                userPreferencesManager.preferencesFor(it).clear()
                globalPreferencesManager.deleteBackupToken(it)
            }
            context.deleteDatabase(CipherDatabaseUtils.getDatabaseName(it.email))
            databaseProvider.delete(it.email)
        }
        sessionCredentialsSaver.deleteSavedCredentials(username)
    }
}
