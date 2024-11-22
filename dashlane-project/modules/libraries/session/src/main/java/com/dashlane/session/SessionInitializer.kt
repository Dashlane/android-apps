package com.dashlane.session

import com.dashlane.user.UserAccountInfo
import com.dashlane.crypto.keys.AppKey
import com.dashlane.crypto.keys.LocalKey
import com.dashlane.crypto.keys.VaultKey
import com.dashlane.login.LoginMode
import com.dashlane.user.Username
import com.dashlane.xml.domain.SyncObject

interface SessionInitializer {

    @Suppress("kotlin:S107") 
    suspend fun createSession(
        username: Username,
        accessKey: String,
        secretKey: String,
        localKey: LocalKey,
        appKey: AppKey,
        userSettings: SyncObject.Settings,
        sharingPublicKey: String? = null,
        sharingPrivateKey: String? = null,
        remoteKey: VaultKey.RemoteKey? = null,
        deviceAnalyticsId: String,
        userAnalyticsId: String,
        loginMode: LoginMode,
        accountType: UserAccountInfo.AccountType
    ): SessionResult
}