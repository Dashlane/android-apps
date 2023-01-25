package com.dashlane.authentication.login

import com.dashlane.authentication.DeviceRegistrationInfo
import com.dashlane.server.api.Response
import com.dashlane.server.api.endpoints.authentication.AuthLoginAuthTicketService
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationAuthTicketService
import com.dashlane.server.api.endpoints.authentication.AuthVerificationDashlaneAuthenticatorService
import com.dashlane.server.api.endpoints.authentication.AuthVerificationDuoPushService
import com.dashlane.server.api.endpoints.authentication.AuthVerificationEmailTokenService
import com.dashlane.server.api.endpoints.authentication.AuthVerificationResult
import com.dashlane.server.api.endpoints.authentication.AuthVerificationSsoService
import com.dashlane.server.api.endpoints.authentication.AuthVerificationTotpService
import com.dashlane.server.api.endpoints.authentication.AuthVerificationU2fService

class AuthenticationAuthTicketHelperImpl(
    private val deviceRegistrationInfo: DeviceRegistrationInfo,
    private val verificationEmailTokenService: AuthVerificationEmailTokenService,
    private val verificationTotpService: AuthVerificationTotpService,
    private val verificationU2fService: AuthVerificationU2fService,
    private val verificationDuoPushService: AuthVerificationDuoPushService,
    private val verificationSsoService: AuthVerificationSsoService,
    private val verificationAuthenticatorService: AuthVerificationDashlaneAuthenticatorService,
    private val registrationAuthTicketService: AuthRegistrationAuthTicketService,
    private val loginAuthTicketService: AuthLoginAuthTicketService
) : AuthenticationAuthTicketHelper {
    override suspend fun verifyEmailToken(
        login: String,
        token: AuthVerificationEmailTokenService.Request.Token
    ): AuthenticationAuthTicketHelper.VerificationResult = VerificationResult(
        login = login,
        verificationResponse = verificationEmailTokenService.execute(
            request = AuthVerificationEmailTokenService.Request(
                login = login,
                token = token
            )
        )
    )

    override suspend fun verifyTotp(
        login: String,
        otp: String
    ): AuthenticationAuthTicketHelper.VerificationResult = VerificationResult(
        login = login,
        verificationResponse = verificationTotpService.execute(
            request = AuthVerificationTotpService.Request(
                login = login,
                otp = otp
            )
        )
    )

    override suspend fun verifyU2f(
        login: String,
        challengeAnswer: AuthVerificationU2fService.Request.ChallengeAnswer
    ): AuthenticationAuthTicketHelper.VerificationResult = VerificationResult(
        login = login,
        verificationResponse = verificationU2fService.execute(
            request = AuthVerificationU2fService.Request(
                login = login,
                challengeAnswer = challengeAnswer
            )
        )
    )

    override suspend fun verifyDuoPush(
        login: String
    ): AuthenticationAuthTicketHelper.VerificationResult = VerificationResult(
        login = login,
        verificationResponse = verificationDuoPushService.execute(
            request = AuthVerificationDuoPushService.Request(
                login = login
            )
        )
    )

    override suspend fun verifySso(
        login: String,
        ssoToken: String
    ): AuthenticationAuthTicketHelper.VerificationResult = VerificationResult(
        login = login,
        verificationResponse = verificationSsoService.execute(
            request = AuthVerificationSsoService.Request(
                login = login,
                ssoToken = ssoToken
            )
        )
    )

    override suspend fun verifyDashlaneAuthenticator(
        login: String
    ): AuthenticationAuthTicketHelper.VerificationResult = VerificationResult(
        login = login,
        verificationResponse = verificationAuthenticatorService.execute(
            request = AuthVerificationDashlaneAuthenticatorService.Request(
                login = login,
                deviceName = deviceRegistrationInfo.deviceName
            )
        )
    )

    private inner class VerificationResult(
        private val login: String,
        verificationResponse: Response<AuthVerificationResult>
    ) : AuthenticationAuthTicketHelper.VerificationResult {
        override val authTicket = verificationResponse.data.authTicket

        override suspend fun registerDevice() = registrationAuthTicketService.execute(
            request = AuthRegistrationAuthTicketService.Request(
                login = login,
                authTicket = AuthRegistrationAuthTicketService.Request.AuthTicket(authTicket),
                device = deviceRegistrationInfo.toAuthRegistrationDevice()
            )
        ).data

        override suspend fun login(accessKey: String) = loginAuthTicketService.execute(
            request = AuthLoginAuthTicketService.Request(
                deviceAccessKey = accessKey,
                login = login,
                authTicket = AuthLoginAuthTicketService.Request.AuthTicket(authTicket)
            )
        ).data
    }
}