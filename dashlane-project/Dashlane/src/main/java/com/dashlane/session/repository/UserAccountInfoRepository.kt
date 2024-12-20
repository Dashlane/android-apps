package com.dashlane.session.repository

import com.dashlane.braze.BrazeWrapper
import com.dashlane.hermes.LogRepository
import com.dashlane.login.LoginInfo
import com.dashlane.login.LoginLogger
import com.dashlane.session.authorization
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.endpoints.account.AccountInfoService
import com.dashlane.server.api.time.toInstant
import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.storage.securestorage.UserSecureStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAccountInfoRepository @Inject constructor(
    private val sessionCoroutineScopeRepository: SessionCoroutineScopeRepository,
    private val accountInfoService: AccountInfoService,
    private val preferencesManager: PreferencesManager,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val brazeWrapper: BrazeWrapper,
    private val logRepository: LogRepository
) : SessionObserver, BySessionRepository<UserAccountInfo> {

    override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        brazeWrapper.setUserId(null)
        logRepository.resetAnalyticsInfo()
    }

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        sessionCoroutineScopeRepository.getCoroutineScope(session)?.launch(Dispatchers.Default) {
            val refreshUserAccountInfo = getUserAccountInfo(session).let {
                
                it.publicUserId == null || it.creationDate == Instant.EPOCH || it.deviceAnalyticsId == null || it.userAnalyticsId == null
            }

            if (refreshUserAccountInfo) {
                refreshUserAccountInfo(session)
            }

            val userAccountInfo = getUserAccountInfo(session)

            userAccountInfo.deviceAnalyticsId?.let { deviceAnalyticsId ->
                userAccountInfo.userAnalyticsId?.let { userAnalyticsId ->
                    logRepository.setUserAnalyticsInfo(
                        deviceId = UUID.fromString(deviceAnalyticsId),
                        userId = UUID.fromString(userAnalyticsId)
                    )
                }
            }

            if (loginInfo?.loginMode != null) {
                LoginLogger(logRepository).logSuccess(
                    isFirstLogin = loginInfo.isFirstLogin,
                    loginMode = loginInfo.loginMode
                )
            }

            userAccountInfo.publicUserId?.let(brazeWrapper::setUserId)
        }
    }

    suspend fun refreshUserAccountInfo(session: Session) {
        runCatching {
            val accountInfo = accountInfoService.execute(session.authorization).data

            val prefs = preferencesManager[session.username]

            prefs.publicUserId = accountInfo.publicUserId
            prefs.accountCreationDate = accountInfo.creationDate.toInstant()
            userSecureStorageManager.storeDeviceAnalyticsId(
                session.localKey,
                session.username,
                accountInfo.deviceAnalyticsId
            )
            userSecureStorageManager.storeUserAnalyticsId(
                session.localKey,
                session.username,
                accountInfo.userAnalyticsId
            )
            userSecureStorageManager.storeUserContactEmail(
                session.localKey,
                session.username,
                accountInfo.contactEmail
            )
        }
    }

    private fun getUserAccountInfo(session: Session): UserAccountInfo {
        val prefs = preferencesManager[session.username]

        return UserAccountInfo(
            publicUserId = prefs.publicUserId,
            creationDate = prefs.accountCreationDate,
            deviceAnalyticsId = userSecureStorageManager.readDeviceAnalyticsId(session.localKey, session.username),
            userAnalyticsId = userSecureStorageManager.readUserAnalyticsId(session.localKey, session.username),
            contactEmail = userSecureStorageManager.readUserContactEmail(session.localKey, session.username)
        )
    }

    override fun get(session: Session?): UserAccountInfo? = session?.let { getUserAccountInfo(it) }
}

data class UserAccountInfo(
    val publicUserId: String?,
    val creationDate: Instant,
    val deviceAnalyticsId: String?,
    val userAnalyticsId: String?,
    val contactEmail: String?
)