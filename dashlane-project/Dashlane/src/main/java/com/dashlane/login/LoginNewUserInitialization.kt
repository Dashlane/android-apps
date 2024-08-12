package com.dashlane.login

import com.dashlane.user.UserAccountInfo
import com.dashlane.crypto.keys.AppKey
import com.dashlane.crypto.keys.LocalKey
import com.dashlane.session.SessionInitializer
import com.dashlane.session.SessionResult
import com.dashlane.user.Username
import com.dashlane.crypto.keys.VaultKey
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class LoginNewUserInitialization @Inject constructor(
    private val sessionInitializer: SessionInitializer
) {
    @Suppress("DEPRECATION", "kotlin:S107") 
    suspend fun initializeSession(
        username: String,
        appKey: AppKey,
        accessKey: String,
        secretKey: String,
        localKey: LocalKey,
        userSettings: SyncObject.Settings,
        sharingPublicKey: String?,
        sharingPrivateKey: String?,
        remoteKey: VaultKey.RemoteKey? = null,
        deviceAnalyticsId: String,
        userAnalyticsId: String,
        loginMode: LoginMode,
        accountType: UserAccountInfo.AccountType
    ): SessionResult {
        return sessionInitializer.createSession(
            username = Username.ofEmail(username),
            accessKey = accessKey,
            secretKey = secretKey,
            localKey = localKey,
            userSettings = userSettings,
            sharingPublicKey = sharingPublicKey,
            sharingPrivateKey = sharingPrivateKey,
            appKey = appKey,
            remoteKey = remoteKey,
            deviceAnalyticsId = deviceAnalyticsId,
            userAnalyticsId = userAnalyticsId,
            loginMode = loginMode,
            accountType = accountType
        )
    }
}