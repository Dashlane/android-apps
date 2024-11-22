package com.dashlane.authentication.login

import com.dashlane.authentication.AuthenticationDeviceCredentialsInvalidException
import com.dashlane.authentication.AuthenticationEmptyPasswordException
import com.dashlane.authentication.AuthenticationInvalidPasswordException
import com.dashlane.authentication.AuthenticationNetworkException
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.SecurityFeature
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.SharingKeys
import com.dashlane.crypto.keys.AppKey
import com.dashlane.crypto.keys.LocalKey
import com.dashlane.crypto.keys.VaultKey
import com.dashlane.xml.domain.SyncObject
import java.time.Instant

interface AuthenticationPasswordRepository {
    @Throws(
        AuthenticationEmptyPasswordException::class,
        AuthenticationInvalidPasswordException::class,
        AuthenticationNetworkException::class,
        AuthenticationDeviceCredentialsInvalidException::class
    )
    suspend fun validate(
        registeredUserDevice: RegisteredUserDevice,
        passwordUtf8Bytes: ObfuscatedByteArray
    ): Result

    suspend fun validateRemoteUser(
        registeredUserDevice: RegisteredUserDevice.Remote,
        password: AppKey.Password
    ): Result.Remote

    sealed class Result {
        abstract val login: String
        abstract val securityFeatures: Set<SecurityFeature>
        abstract val accessKey: String
        abstract val secretKey: String
        abstract val password: AppKey
        abstract val localKey: LocalKey

        data class Local(
            override val login: String,
            override val securityFeatures: Set<SecurityFeature>,
            override val accessKey: String,
            override val secretKey: String,
            override val password: AppKey,
            override val localKey: LocalKey,
            val isAccessKeyRefreshed: Boolean
        ) : Result()

        data class Remote(
            override val login: String,
            override val securityFeatures: Set<SecurityFeature>,
            override val accessKey: String,
            override val secretKey: String,
            override val password: AppKey,
            override val localKey: LocalKey,
            val settings: SyncObject.Settings,
            val settingsDate: Instant,
            val registeredUserDevice: RegisteredUserDevice,
            val remoteKey: VaultKey.RemoteKey?,
            val sharingKeys: SharingKeys? = null,
            val deviceAnalyticsId: String,
            val userAnalyticsId: String
        ) : Result()
    }
}
