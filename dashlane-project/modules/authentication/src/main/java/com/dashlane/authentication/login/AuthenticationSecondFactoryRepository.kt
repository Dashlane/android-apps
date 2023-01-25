package com.dashlane.authentication.login

import com.dashlane.authentication.AuthenticationAccountConfigurationChangedException
import com.dashlane.authentication.AuthenticationInvalidTokenException
import com.dashlane.authentication.AuthenticationLockedOutException
import com.dashlane.authentication.AuthenticationNetworkException
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.AuthenticationSecondFactorFailedException
import com.dashlane.authentication.AuthenticationTimeoutException
import com.dashlane.authentication.AuthenticationUnknownException
import com.dashlane.authentication.RegisteredUserDevice



interface AuthenticationSecondFactoryRepository {
    @Throws(
        AuthenticationInvalidTokenException::class,
        AuthenticationLockedOutException::class,
        AuthenticationAccountConfigurationChangedException::class,
        AuthenticationNetworkException::class,
        AuthenticationUnknownException::class
    )
    suspend fun validate(
        emailToken: AuthenticationSecondFactor.EmailToken,
        value: String
    ): ValidationResult

    @Throws(
        AuthenticationSecondFactorFailedException::class,
        AuthenticationLockedOutException::class,
        AuthenticationAccountConfigurationChangedException::class,
        AuthenticationNetworkException::class,
        AuthenticationUnknownException::class
    )
    suspend fun validate(
        totp: AuthenticationSecondFactor.Totp,
        value: String
    ): ValidationResult

    @Throws(
        AuthenticationSecondFactorFailedException::class,
        AuthenticationLockedOutException::class,
        AuthenticationAccountConfigurationChangedException::class,
        AuthenticationNetworkException::class,
        AuthenticationUnknownException::class
    )
    suspend fun validate(
        u2f: AuthenticationSecondFactor.U2f,
        challenge: String,
        signature: String,
        clientData: String
    ): ValidationResult

    @Throws(
        AuthenticationSecondFactorFailedException::class,
        AuthenticationLockedOutException::class,
        AuthenticationAccountConfigurationChangedException::class,
        AuthenticationTimeoutException::class,
        AuthenticationNetworkException::class,
        AuthenticationUnknownException::class
    )
    suspend fun validate(
        duoPush: AuthenticationSecondFactor.DuoPush
    ): ValidationResult

    @Throws(
        AuthenticationAccountConfigurationChangedException::class,
        AuthenticationNetworkException::class,
        AuthenticationUnknownException::class
    )
    suspend fun resendToken(emailToken: AuthenticationSecondFactor.EmailToken)

    @Throws(
        AuthenticationSecondFactorFailedException::class,
        AuthenticationTimeoutException::class,
        AuthenticationNetworkException::class,
        AuthenticationUnknownException::class
    )
    suspend fun validate(
        dashlaneAuthenticator: AuthenticationSecondFactor.Authenticator
    ): ValidationResult

    data class ValidationResult(
        val registeredUserDevice: RegisteredUserDevice,
        val authTicket: String?
    )
}