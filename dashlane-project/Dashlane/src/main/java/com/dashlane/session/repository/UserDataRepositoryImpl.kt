package com.dashlane.session.repository

import com.dashlane.user.UserAccountInfo
import com.dashlane.account.UserAccountStorage
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.Session
import com.dashlane.session.UserDataRepository
import com.dashlane.crypto.keys.isServerKeyNotNull
import com.dashlane.settings.SettingsManager
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.xml.domain.SyncObject
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
open class UserDataRepositoryImpl @Inject constructor(
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val sessionCoroutineScopeRepository: SessionCoroutineScopeRepository,
    private val userAccountStorage: Lazy<UserAccountStorage>,
    private val userSecureStorageManager: Lazy<UserSecureStorageManager>,
    private val userPreferencesManager: UserPreferencesManager,
) : UserDataRepository {
    private val settingsManagerPerSession = mutableMapOf<Session, SettingsManager>()

    override fun getSettingsManager(session: Session) =
        settingsManagerPerSession.getOrPut(
            key = session,
            defaultValue = {
                SettingsManager(
                    userSecureStorageManager.get(),
                    userPreferencesManager,
                    session
                )
            }
        )

    override fun get(session: Session?): SettingsManager? = session?.let { getSettingsManager(it) }

    override suspend fun sessionInitializing(
        session: Session,
        userSettings: SyncObject.Settings?,
        accountType: UserAccountInfo.AccountType,
        allowOverwriteAccessKey: Boolean
    ) {
        sessionCoroutineScopeRepository.getCoroutineScope(session)?.let {
            withContext(it.coroutineContext) {
                globalPreferencesManager.isUserLoggedOut = false
                globalPreferencesManager.setLastLoggedInUser(session.userId)

                
                val userAccountInfo = UserAccountInfo(
                    username = session.userId,
                    accessKey = session.accessKey,
                    otp2 = session.appKey.isServerKeyNotNull,
                    accountType = accountType
                )
                userAccountStorage.get().saveUserAccountInfo(
                    userAccountInfo,
                    session.localKey,
                    session.secretKey,
                    allowOverwriteAccessKey
                )

                
                userSettings?.run {
                    getSettingsManager(session).updateSettings(this, triggerSync = false)
                }

                runCatching {
                    
                    getSettingsManager(session).loadSettings()
                }
            }
        }
    }

    override suspend fun sessionCleanup(session: Session, forceLogout: Boolean) {
        if (forceLogout) globalPreferencesManager.isUserLoggedOut = true
        getSettingsManager(session).unloadSettings()
        settingsManagerPerSession.remove(session)
    }
}