package com.dashlane.authentication.create

import com.dashlane.authentication.AuthenticationAccountAlreadyExistsException
import com.dashlane.authentication.AuthenticationContactSsoAdministratorException
import com.dashlane.authentication.AuthenticationEmptyEmailException
import com.dashlane.authentication.AuthenticationExpiredVersionException
import com.dashlane.authentication.AuthenticationInvalidEmailException
import com.dashlane.authentication.AuthenticationNetworkException
import com.dashlane.authentication.AuthenticationUnknownException



interface AccountCreationEmailRepository {
    @Throws(
        AuthenticationEmptyEmailException::class,
        AuthenticationInvalidEmailException::class,
        AuthenticationAccountAlreadyExistsException::class,
        AuthenticationContactSsoAdministratorException::class,
        AuthenticationNetworkException::class,
        AuthenticationUnknownException::class,
        AuthenticationExpiredVersionException::class
    )
    suspend fun validate(email: String): Result

    sealed class Result {
        data class Success(
            val login: String,
            val country: String?,
            val isEuropeanUnion: Boolean,
            val ssoServiceProviderUrl: String?,
            val ssoIsNitroProvider: Boolean
        ) : Result()

        data class Warning(
            val success: Success
        ) : Result()
    }
}