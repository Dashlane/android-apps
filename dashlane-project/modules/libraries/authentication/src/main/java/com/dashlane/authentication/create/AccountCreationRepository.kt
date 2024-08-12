package com.dashlane.authentication.create

import com.dashlane.authentication.AuthenticationExpiredVersionException
import com.dashlane.authentication.AuthenticationInvalidLoginException
import com.dashlane.authentication.AuthenticationNetworkException
import com.dashlane.authentication.AuthenticationUnknownException
import com.dashlane.authentication.TermsOfService
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.SharingKeys
import com.dashlane.server.api.endpoints.AccountType
import com.dashlane.crypto.keys.AppKey
import com.dashlane.crypto.keys.VaultKey
import com.dashlane.xml.domain.SyncObject

interface AccountCreationRepository {
    @Throws(
        AuthenticationInvalidLoginException::class,
        AuthenticationNetworkException::class,
        AuthenticationExpiredVersionException::class,
        AuthenticationUnknownException::class
    )
    suspend fun createAccount(
        login: String,
        passwordUtf8Bytes: ObfuscatedByteArray,
        accountType: AccountType,
        termsOfService: TermsOfService,
        withRemoteKey: Boolean = false,
        withLegacyCrypto: Boolean = false,
        country: String?
    ): Result

    @Throws(
        AuthenticationInvalidLoginException::class,
        AuthenticationNetworkException::class,
        AuthenticationExpiredVersionException::class,
        AuthenticationUnknownException::class
    )
    suspend fun createSsoAccount(
        login: String,
        ssoToken: String,
        serviceProviderKey: String,
        termsOfService: TermsOfService
    ): Result

    data class Result(
        val login: String,
        val settings: SyncObject.Settings,
        val accessKey: String,
        val secretKey: String,
        val sharingKeys: SharingKeys,
        val isAccountReset: Boolean,
        val origin: String,
        val remoteKey: VaultKey.RemoteKey?,
        val appKey: AppKey,
        val userAnalyticsId: String,
        val deviceAnalyticsId: String
    )
}