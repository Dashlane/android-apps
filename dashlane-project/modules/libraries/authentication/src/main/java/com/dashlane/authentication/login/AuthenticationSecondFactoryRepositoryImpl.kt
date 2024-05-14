package com.dashlane.authentication.login

import com.dashlane.authentication.AuthenticationAccountConfigurationChangedException
import com.dashlane.authentication.AuthenticationException
import com.dashlane.authentication.AuthenticationInvalidTokenException
import com.dashlane.authentication.AuthenticationLockedOutException
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.AuthenticationSecondFactorFailedException
import com.dashlane.authentication.AuthenticationTimeoutException
import com.dashlane.authentication.AuthenticationUnknownException
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.SecurityFeature
import com.dashlane.authentication.UserStorage
import com.dashlane.authentication.toAuthenticationException
import com.dashlane.authentication.toRegisteredUserDevice
import com.dashlane.cryptography.EncryptedBase64String
import com.dashlane.cryptography.asEncryptedBase64
import com.dashlane.server.api.ConnectivityCheck
import com.dashlane.server.api.endpoints.account.SharingKeys
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationAuthTicketService
import com.dashlane.server.api.endpoints.authentication.AuthSendEmailTokenService
import com.dashlane.server.api.endpoints.authentication.AuthVerificationEmailTokenService
import com.dashlane.server.api.endpoints.authentication.AuthVerificationU2fService
import com.dashlane.server.api.endpoints.authentication.RemoteKey
import com.dashlane.server.api.endpoints.authentication.exceptions.AccountBlockedContactSupportException
import com.dashlane.server.api.endpoints.authentication.exceptions.DeviceDeactivatedException
import com.dashlane.server.api.endpoints.authentication.exceptions.DeviceNotFoundException
import com.dashlane.server.api.endpoints.authentication.exceptions.FailedToContactAuthenticatorDeviceException
import com.dashlane.server.api.endpoints.authentication.exceptions.InvalidOtpAlreadyUsedException
import com.dashlane.server.api.endpoints.authentication.exceptions.InvalidOtpBlockedException
import com.dashlane.server.api.endpoints.authentication.exceptions.TwofaEmailTokenNotEnabledException
import com.dashlane.server.api.endpoints.authentication.exceptions.UserHasNoActiveAuthenticatorException
import com.dashlane.server.api.endpoints.authentication.exceptions.UserNotFoundException
import com.dashlane.server.api.endpoints.authentication.exceptions.VerificationFailedException
import com.dashlane.server.api.endpoints.authentication.exceptions.VerificationMethodDisabledException
import com.dashlane.server.api.endpoints.authentication.exceptions.VerificationRequiresRequestException
import com.dashlane.server.api.endpoints.authentication.exceptions.VerificationTimeoutException
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.server.api.exceptions.DashlaneApiHttp400BusinessException
import com.dashlane.server.api.time.toInstant
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthenticationSecondFactoryRepositoryImpl(
    private val userStorage: UserStorage,
    private val sendEmailTokenService: AuthSendEmailTokenService,
    private val connectivityCheck: ConnectivityCheck,
    private val authTicketHelper: AuthenticationAuthTicketHelper
) : AuthenticationSecondFactoryRepository {
    override suspend fun validate(
        emailToken: AuthenticationSecondFactor.EmailToken,
        value: String
    ): AuthenticationSecondFactoryRepository.ValidationResult =
        withContext(Dispatchers.Default) { validateImpl(value, emailToken) }

    private suspend fun validateImpl(
        value: String,
        emailToken: AuthenticationSecondFactor.EmailToken
    ): AuthenticationSecondFactoryRepository.ValidationResult {
        val token = try {
            AuthVerificationEmailTokenService.Request.Token(value)
        } catch (e: IllegalArgumentException) {
            throw AuthenticationInvalidTokenException(cause = e)
        }

        if (connectivityCheck.isOffline) {
            throw AuthenticationOfflineException()
        }

        val login = emailToken.login
        val (authTicket, responseData) = try {
            registerDeviceWithToken(login, token)
        } catch (e: DashlaneApiHttp400BusinessException) {
            throw e.toEmailTokenError(emailToken, ::resendToken)
        } catch (e: DashlaneApiException) {
            throw e.toAuthenticationException()
        }
        val encryptedSettings = responseData.settings.content
            ?: throw AuthenticationUnknownException(message = "settings.content == null")
        val settingsDate = responseData.settings.backupDate.toInstant()
        return AuthenticationSecondFactoryRepository.ValidationResult(
            responseData.toRegisteredUserDevice(
                login = login,
                securityFeatures = emailToken.securityFeatures,
                encryptedSettings = encryptedSettings,
                settingsDate = settingsDate
            ),
            authTicket = authTicket
        )
        
        
    }

    private suspend fun registerDeviceWithToken(
        login: String,
        token: AuthVerificationEmailTokenService.Request.Token
    ) = authTicketHelper.verifyEmailToken(
        login = login,
        token = token
    ).run { authTicket to registerDevice() }

    override suspend fun validate(
        totp: AuthenticationSecondFactor.Totp,
        value: String
    ): AuthenticationSecondFactoryRepository.ValidationResult =
        withContext(Dispatchers.Default) {
            TotpStrategy().validate(
                totp.login,
                totp.securityFeatures,
                value
            )
        }

    override suspend fun validate(
        u2f: AuthenticationSecondFactor.U2f,
        challenge: String,
        signature: String,
        clientData: String
    ): AuthenticationSecondFactoryRepository.ValidationResult =
        withContext(Dispatchers.Default) {
            U2fStrategy().validate(
                u2f.login,
                u2f.securityFeatures,
                AuthVerificationU2fService.Request.ChallengeAnswer(
                    challenge = challenge,
                    signatureData = signature,
                    clientData = clientData
                )
            )
        }

    override suspend fun validate(
        duoPush: AuthenticationSecondFactor.DuoPush
    ): AuthenticationSecondFactoryRepository.ValidationResult =
        withContext(Dispatchers.Default) {
            DuoPushStrategy().validate(
                duoPush.login,
                duoPush.securityFeatures,
                Unit
            )
        }

    override suspend fun validate(
        dashlaneAuthenticator: AuthenticationSecondFactor.Authenticator
    ): AuthenticationSecondFactoryRepository.ValidationResult = withContext(Dispatchers.Default) {
        DashlaneAuthenticatorStrategy().validate(
            dashlaneAuthenticator.login,
            dashlaneAuthenticator.securityFeatures,
            Unit
        )
    }

    override suspend fun resendToken(emailToken: AuthenticationSecondFactor.EmailToken) =
        withContext(Dispatchers.Default) { resendTokenImpl(emailToken) }

    private suspend fun resendTokenImpl(emailToken: AuthenticationSecondFactor.EmailToken) {
        val login = emailToken.login
        try {
            sendEmailTokenService.execute(
                AuthSendEmailTokenService.Request(
                    login = login
                )
            )
        } catch (e: TwofaEmailTokenNotEnabledException) {
            throw AuthenticationAccountConfigurationChangedException(cause = e)
        } catch (e: DashlaneApiException) {
            throw e.toAuthenticationException()
        }
    }

    private abstract inner class SecondFactorStrategy<VerificationT> {
        suspend fun validate(
            login: String,
            securityFeatures: Set<SecurityFeature>,
            verification: VerificationT
        ): AuthenticationSecondFactoryRepository.ValidationResult {
            val userDevice = userStorage.getUser(login)
            return if (userDevice == null) {
                if (connectivityCheck.isOffline) {
                    throw AuthenticationOfflineException()
                }

                val (authTicket, responseData) = try {
                    val verifyResult = verify(login, verification)
                    verifyResult.authTicket to verifyResult.registerDevice()
                } catch (e: DashlaneApiException) {
                    throw e.toError()
                }

                val encryptedSettings = responseData.settings.content
                    ?: throw AuthenticationUnknownException("settings.content == null")
                val settingsDate = responseData.settings.backupDate.toInstant()
                AuthenticationSecondFactoryRepository.ValidationResult(
                    registeredUserDevice = responseData.toRegisteredUserDevice(
                        login = login,
                        securityFeatures = securityFeatures,
                        encryptedSettings = encryptedSettings,
                        settingsDate = settingsDate
                    ),
                    authTicket = authTicket
                )
            } else {
                
                if (connectivityCheck.isOffline) {
                    throw AuthenticationOfflineException()
                }

                val (authTicket, responseData) = try {
                    val verifyResult = verify(login, verification)
                    verifyResult.authTicket to verifyResult.login(userDevice.accessKey)
                } catch (e: DashlaneApiException) {
                    throw e.toError()
                }
                AuthenticationSecondFactoryRepository.ValidationResult(
                    registeredUserDevice = userDevice.toRegisteredUserDevice(responseData.serverKey),
                    authTicket = authTicket
                )
                
                
            }
        }

        private fun DashlaneApiException.toError(): AuthenticationException =
            when (this) {
                is VerificationFailedException -> AuthenticationSecondFactorFailedException(cause = this)
                is DeviceDeactivatedException,
                is DeviceNotFoundException,
                is VerificationMethodDisabledException,
                is UserNotFoundException -> AuthenticationAccountConfigurationChangedException(cause = this)

                is DashlaneApiHttp400BusinessException -> toError()
                else -> toAuthenticationException()
            }

        protected abstract fun DashlaneApiHttp400BusinessException.toError(): AuthenticationException

        protected abstract suspend fun verify(
            login: String,
            verification: VerificationT
        ): AuthenticationAuthTicketHelper.VerificationResult
    }

    private inner class TotpStrategy : SecondFactorStrategy<String>() {

        override fun DashlaneApiHttp400BusinessException.toError() =
            toTotpError()

        override suspend fun verify(
            login: String,
            verification: String
        ) = authTicketHelper.verifyTotp(
            login = login,
            otp = verification
        )
    }

    private inner class U2fStrategy :
        SecondFactorStrategy<AuthVerificationU2fService.Request.ChallengeAnswer>() {

        override fun DashlaneApiHttp400BusinessException.toError() =
            AuthenticationUnknownException(cause = this)

        override suspend fun verify(
            login: String,
            verification: AuthVerificationU2fService.Request.ChallengeAnswer
        ) = authTicketHelper.verifyU2f(
            login = login,
            challengeAnswer = verification
        )
    }

    private inner class DuoPushStrategy : SecondFactorStrategy<Unit>() {

        override fun DashlaneApiHttp400BusinessException.toError() =
            toDuoPushError()

        override suspend fun verify(
            login: String,
            verification: Unit
        ) = authTicketHelper.verifyDuoPush(login)
    }

    private inner class DashlaneAuthenticatorStrategy : SecondFactorStrategy<Unit>() {

        override fun DashlaneApiHttp400BusinessException.toError() = when (this) {
            is FailedToContactAuthenticatorDeviceException,
            is UserHasNoActiveAuthenticatorException,
            is VerificationFailedException -> AuthenticationSecondFactorFailedException(cause = this)

            is VerificationTimeoutException -> AuthenticationTimeoutException(cause = this)
            else -> AuthenticationUnknownException(cause = this)
        }

        override suspend fun verify(
            login: String,
            verification: Unit
        ) = authTicketHelper.verifyDashlaneAuthenticator(login)
    }
}

private fun AuthRegistrationAuthTicketService.Data.toRegisteredUserDevice(
    login: String,
    securityFeatures: Set<SecurityFeature>,
    encryptedSettings: String,
    settingsDate: Instant
) = RegisteredUserDevice.Remote(
    login = login,
    securityFeatures = securityFeatures,
    serverKey = serverKey,
    accessKey = deviceAccessKey,
    secretKey = deviceSecretKey,
    encryptedSettings = encryptedSettings,
    settingsDate = settingsDate,
    sharingKeys = sharingKeys?.toRegisteredDeviceSharingKeys(),
    userId = publicUserId,
    hasDesktopDevice = hasDesktopDevices,
    registeredDeviceCount = numberOfDevices,
    deviceAnalyticsId = deviceAnalyticsId,
    userAnalyticsId = userAnalyticsId,
    encryptedRemoteKey = remoteKeys?.findByTypeOrNull(RemoteKey.Type.MASTER_PASSWORD)
)

internal fun SharingKeys.toRegisteredDeviceSharingKeys() =
    RegisteredUserDevice.Remote.SharingKeys(
        publicKey = publicKey,
        encryptedPrivateKey = privateKey
    )

private suspend fun DashlaneApiHttp400BusinessException.toEmailTokenError(
    emailToken: AuthenticationSecondFactor.EmailToken,
    resendToken: suspend (AuthenticationSecondFactor.EmailToken) -> Unit
): AuthenticationException =
    when (this) {
        is VerificationFailedException -> AuthenticationInvalidTokenException(cause = this)
        is VerificationRequiresRequestException,
        is VerificationTimeoutException -> {
            
            try {
                resendToken(emailToken)
            } catch (_: AuthenticationException) {
                
            }
            AuthenticationInvalidTokenException(cause = this)
        }

        is VerificationMethodDisabledException -> AuthenticationAccountConfigurationChangedException(
            cause = this
        )

        is AccountBlockedContactSupportException -> AuthenticationLockedOutException(cause = this)
        else -> AuthenticationUnknownException(cause = this)
    }

private fun DashlaneApiHttp400BusinessException.toTotpError(): AuthenticationException =
    when (this) {
        is InvalidOtpAlreadyUsedException -> AuthenticationSecondFactorFailedException(cause = this)
        is InvalidOtpBlockedException -> AuthenticationLockedOutException(cause = this)
        else -> AuthenticationUnknownException(cause = this)
    }

private fun DashlaneApiHttp400BusinessException.toDuoPushError(): AuthenticationException =
    when (this) {
        is VerificationTimeoutException -> AuthenticationTimeoutException(cause = this)
        else -> AuthenticationUnknownException(cause = this)
    }

fun List<RemoteKey>.findByTypeOrNull(type: RemoteKey.Type): EncryptedBase64String? =
    firstOrNull { it.type == type }?.key?.asEncryptedBase64()
