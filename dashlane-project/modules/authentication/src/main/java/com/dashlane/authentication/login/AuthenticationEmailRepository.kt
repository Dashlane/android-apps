package com.dashlane.authentication.login

import com.dashlane.authentication.AuthenticationAccountNotFoundException
import com.dashlane.authentication.AuthenticationContactSsoAdministratorException
import com.dashlane.authentication.AuthenticationEmptyEmailException
import com.dashlane.authentication.AuthenticationExpiredVersionException
import com.dashlane.authentication.AuthenticationInvalidEmailException
import com.dashlane.authentication.AuthenticationNetworkException
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.AuthenticationUnknownException
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.UnauthenticatedUser
import com.dashlane.server.api.endpoints.AccountType

interface AuthenticationEmailRepository {
    @Throws(
        AuthenticationEmptyEmailException::class,
        AuthenticationInvalidEmailException::class,
        AuthenticationAccountNotFoundException::class,
        AuthenticationContactSsoAdministratorException::class,
        AuthenticationOfflineException::class,
        AuthenticationNetworkException::class,
        AuthenticationUnknownException::class,
        AuthenticationExpiredVersionException::class
    )
    suspend fun getUserStatus(unauthenticatedUser: UnauthenticatedUser): Result

    sealed class Result {
        abstract val ssoInfo: SsoInfo?

        data class RequiresPassword(
            val registeredUserDevice: RegisteredUserDevice,
            override val ssoInfo: SsoInfo? = null
        ) : Result()

        data class RequiresServerKey(
            val secondFactor: AuthenticationSecondFactor.Totp,
            override val ssoInfo: SsoInfo?
        ) : Result()

        data class RequiresSso(
            val login: String,
            override val ssoInfo: SsoInfo
        ) : Result()

        sealed class RequiresDeviceRegistration : Result() {
            data class SecondFactor(
                val secondFactor: AuthenticationSecondFactor,
                val accountType: AccountType,
                override val ssoInfo: SsoInfo?
            ) : RequiresDeviceRegistration()

            data class Sso(
                val login: String,
                override val ssoInfo: SsoInfo
            ) : RequiresDeviceRegistration()
        }
    }
}