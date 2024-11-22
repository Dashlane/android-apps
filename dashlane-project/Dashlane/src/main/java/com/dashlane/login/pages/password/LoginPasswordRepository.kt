package com.dashlane.login.pages.password

import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.SecurityFeature
import com.dashlane.authentication.login.AuthenticationPasswordRepository
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.limitations.DeviceLimitActivityListener
import com.dashlane.limitations.Enforce2faLimiter
import com.dashlane.login.LoginMode
import com.dashlane.login.LoginStrategy
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.Session
import com.dashlane.session.SessionInitializer
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionResult
import com.dashlane.sync.DataSync
import com.dashlane.user.UserAccountInfo
import com.dashlane.user.Username
import javax.inject.Inject

class LoginPasswordRepository @Inject constructor(
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    private val deviceLimitActivityListener: DeviceLimitActivityListener,
    private val enforce2faLimiter: Enforce2faLimiter,
    private val dataSync: DataSync,
    private val sessionInitializer: SessionInitializer,
    private val loginStrategy: LoginStrategy
) {

    suspend fun createSessionForLocalPassword(
        registeredUserDevice: RegisteredUserDevice,
        result: AuthenticationPasswordRepository.Result.Local
    ): Session {
        val username = Username.ofEmail(registeredUserDevice.login)
        val sessionResult = sessionManager.loadSession(
            username,
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
        val shouldLaunchInitialSync = preferencesManager[username].getInt(ConstantsPrefs.TIMESTAMP_LABEL, 0) == 0
        
        
        
        
        
        
        deviceLimitActivityListener.isFirstLogin = shouldLaunchInitialSync
        enforce2faLimiter.isFirstLogin = shouldLaunchInitialSync

        if (!shouldLaunchInitialSync) dataSync.sync(Trigger.LOGIN)

        return (sessionResult as SessionResult.Success).session
    }

    suspend fun createSessionForRemotePassword(
        result: AuthenticationPasswordRepository.Result.Remote,
        accountType: UserAccountInfo.AccountType,
    ): Session {
        val username = result.registeredUserDevice.login
        val sessionResult = sessionInitializer.createSession(
            username = Username.ofEmail(username),
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

        
        
        
        
        
        
        
        preferencesManager[username].userSettingsBackupTimeMillis = result.settingsDate.toEpochMilli()

        if (sessionResult is SessionResult.Error) throw sessionResult.cause ?: IllegalStateException("Session can't be created")

        sessionResult as SessionResult.Success

        deviceLimitActivityListener.isFirstLogin = true
        enforce2faLimiter.isFirstLogin = true

        return sessionResult.session
    }

    suspend fun getLocalStrategy(session: Session): LoginStrategy.Strategy {
        val shouldLaunchInitialSync = preferencesManager[session.username].getInt(ConstantsPrefs.TIMESTAMP_LABEL, 0) == 0
        val strategy = when {
            
            shouldLaunchInitialSync -> loginStrategy.getStrategy(session)
            else -> LoginStrategy.Strategy.Unlock
        }
        return strategy
    }

    suspend fun getRemoteStrategy(session: Session, securityFeatures: Set<SecurityFeature>): LoginStrategy.Strategy {
        val strategy = loginStrategy.getStrategy(session, securityFeatures)
        if (strategy is LoginStrategy.Strategy.Monobucket) {
            preferencesManager[session.username].ukiRequiresMonobucketConfirmation = true
        }
        return strategy
    }
}

fun RegisteredUserDevice.toVerification() = when (this) {
    is RegisteredUserDevice.Local -> if (isServerKeyRequired) {
        VerificationMode.OTP2
    } else {
        VerificationMode.NONE
    }

    is RegisteredUserDevice.ToRestore -> VerificationMode.EMAIL_TOKEN
    is RegisteredUserDevice.Remote -> when {
        SecurityFeature.TOTP in securityFeatures -> VerificationMode.OTP1
        SecurityFeature.EMAIL_TOKEN in securityFeatures -> VerificationMode.EMAIL_TOKEN
        else -> VerificationMode.NONE
    }
}
