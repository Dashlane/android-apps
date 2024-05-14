package com.dashlane.login.pages.password

import com.dashlane.account.UserAccountInfo
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.login.AuthenticationPasswordRepository
import com.dashlane.sync.DataSync
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.limitations.DeviceLimitActivityListener
import com.dashlane.limitations.Enforce2faLimiter
import com.dashlane.login.LoginMode
import com.dashlane.login.LoginNewUserInitialization
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionResult
import com.dashlane.session.Username
import javax.inject.Inject

class LoginPasswordRepository @Inject constructor(
    private val sessionManager: SessionManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val deviceLimitActivityListener: DeviceLimitActivityListener,
    private val enforce2faLimiter: Enforce2faLimiter,
    private val dataSync: DataSync,
    private val loginNewUserInitialization: LoginNewUserInitialization,
) {

    suspend fun createSessionForLocalPassword(
        registeredUserDevice: RegisteredUserDevice,
        result: AuthenticationPasswordRepository.Result.Local
    ): Session {
        val sessionResult = sessionManager.loadSession(
            Username.ofEmail(registeredUserDevice.login),
            result.password,
            result.secretKey,
            result.localKey,
            result.accessKey.takeIf { result.isAccessKeyRefreshed },
            LoginMode.MasterPassword(verification = registeredUserDevice.toVerification())
        )
        if (sessionResult is SessionResult.Error) {
            throw IllegalStateException(
                "Failed to load session ${sessionResult.errorCode} ${sessionResult.errorReason}",
                sessionResult.cause
            )
        }
        val shouldLaunchInitialSync = userPreferencesManager.getInt(ConstantsPrefs.TIMESTAMP_LABEL, 0) == 0
        
        
        
        
        
        
        deviceLimitActivityListener.isFirstLogin = shouldLaunchInitialSync
        enforce2faLimiter.isFirstLogin = shouldLaunchInitialSync

        if (!shouldLaunchInitialSync) dataSync.sync(Trigger.LOGIN)

        return (sessionResult as SessionResult.Success).session
    }

    suspend fun createSessionForRemotePassword(
        result: AuthenticationPasswordRepository.Result.Remote,
        accountType: UserAccountInfo.AccountType,
    ): Session {
        val sessionResult = loginNewUserInitialization.initializeSession(
            username = result.registeredUserDevice.login,
            appKey = result.password,
            accessKey = result.accessKey,
            secretKey = result.secretKey,
            localKey = result.localKey,
            userSettings = result.settings,
            sharingPublicKey = result.sharingKeys?.public?.value,
            sharingPrivateKey = result.sharingKeys?.private?.value,
            remoteKey = result.remoteKey,
            deviceAnalyticsId = result.deviceAnalyticsId,
            userAnalyticsId = result.userAnalyticsId,
            loginMode = LoginMode.MasterPassword(verification = result.registeredUserDevice.toVerification()),
            accountType = accountType
        )

        
        
        
        
        
        
        
        userPreferencesManager.userSettingsBackupTimeMillis = result.settingsDate.toEpochMilli()

        if (sessionResult is SessionResult.Error) throw sessionResult.cause ?: IllegalStateException("Session can't be created")

        sessionResult as SessionResult.Success

        deviceLimitActivityListener.isFirstLogin = true
        enforce2faLimiter.isFirstLogin = true

        return sessionResult.session
    }
}