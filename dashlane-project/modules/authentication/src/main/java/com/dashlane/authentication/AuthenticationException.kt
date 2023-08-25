package com.dashlane.authentication

import com.dashlane.server.api.endpoints.authentication.exceptions.ExpiredVersionException
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.server.api.exceptions.DashlaneApiIoException
import com.dashlane.server.api.exceptions.DashlaneApiOfflineException

open class AuthenticationException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)

open class AuthenticationEmptyEmailException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

open class AuthenticationInvalidEmailException(
    val remoteCheck: Boolean = false,
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

open class AuthenticationAccountAlreadyExistsException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

open class AuthenticationContactSsoAdministratorException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

open class AuthenticationNetworkException(
    message: String? = null,
    cause: Throwable? = null,
    val endpoint: Endpoint? = null
) : AuthenticationException(message, cause) {
    enum class Endpoint {
        REGISTRATION,

        LOGIN
    }
}

open class AuthenticationOfflineException(
    message: String? = null,
    cause: Throwable? = null,
    endpoint: Endpoint? = null
) : AuthenticationNetworkException(message, cause, endpoint)

open class AuthenticationUnknownException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

open class AuthenticationInvalidLoginException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

open class AuthenticationAccountNotFoundException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

open class AuthenticationEmptyPasswordException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

open class AuthenticationInvalidPasswordException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

open class AuthenticationDeviceCredentialsInvalidException(
    val isValidPassword: Boolean,
    val isDataCorruption: Boolean = false,
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

open class AuthenticationInvalidTokenException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

open class AuthenticationLockedOutException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

open class AuthenticationAccountConfigurationChangedException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

open class AuthenticationSecondFactorFailedException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

open class AuthenticationTimeoutException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

open class AuthenticationInvalidSsoException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

open class AuthenticationExpiredVersionException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

open class AuthenticationTeamException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

internal fun DashlaneApiException.toAuthenticationException(
    message: String? = null,
    endpoint: AuthenticationNetworkException.Endpoint? = null
) = when (this) {
    is DashlaneApiOfflineException -> AuthenticationOfflineException(message, this, endpoint)
    is DashlaneApiIoException -> AuthenticationNetworkException(message, this, endpoint)
    is ExpiredVersionException -> AuthenticationExpiredVersionException(message, this)
    else -> AuthenticationUnknownException(message, this)
}