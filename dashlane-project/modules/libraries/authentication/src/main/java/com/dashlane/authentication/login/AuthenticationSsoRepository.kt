package com.dashlane.authentication.login

import com.dashlane.authentication.AuthenticationInvalidSsoException
import com.dashlane.authentication.AuthenticationNetworkException
import com.dashlane.authentication.AuthenticationUnknownException
import com.dashlane.cryptography.SharingKeys
import com.dashlane.crypto.keys.VaultKey
import com.dashlane.crypto.keys.AppKey
import com.dashlane.crypto.keys.LocalKey
import com.dashlane.xml.domain.SyncObject
import java.time.Instant

interface AuthenticationSsoRepository {
    @Throws(
        AuthenticationNetworkException::class,
        AuthenticationUnknownException::class
    )
    suspend fun getSsoInfo(login: String, accessKey: String): SsoInfo

    @Throws(
        AuthenticationInvalidSsoException::class,
        AuthenticationNetworkException::class,
        AuthenticationUnknownException::class
    )
    suspend fun validate(
        login: String,
        ssoToken: String,
        serviceProviderKey: String,
        accessKey: String? = null
    ): ValidateResult

    sealed class ValidateResult {
        data class Local(
            val login: String,
            val secretKey: String,
            val localKey: LocalKey,
            val ssoKey: AppKey.SsoKey,
            val authTicket: String
        ) : ValidateResult()

        data class Remote(
            val login: String,
            val accessKey: String,
            val secretKey: String,
            val localKey: LocalKey,
            val ssoKey: AppKey.SsoKey,
            val authTicket: String,
            val remoteKey: VaultKey.RemoteKey,
            val settings: SyncObject.Settings,
            val settingsDate: Instant,
            val sharingKeys: SharingKeys?,
            val deviceAnalyticsId: String,
            val userAnalyticsId: String
        ) : ValidateResult()
    }
}