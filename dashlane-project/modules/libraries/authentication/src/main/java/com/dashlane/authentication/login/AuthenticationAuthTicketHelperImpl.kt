package com.dashlane.authentication.login

import com.dashlane.authentication.DeviceRegistrationInfo
import com.dashlane.server.api.Response
import com.dashlane.server.api.endpoints.authentication.AuthVerificationResult
import com.dashlane.server.api.endpoints.authentication.CompleteDeviceRegistrationWithAuthTicketService
import com.dashlane.server.api.endpoints.authentication.CompleteLoginWithAuthTicketService
import com.dashlane.server.api.endpoints.authentication.PerformDuoPushVerificationService
import com.dashlane.server.api.endpoints.authentication.PerformEmailTokenVerificationService
import com.dashlane.server.api.endpoints.authentication.PerformSsoVerificationService
import com.dashlane.server.api.endpoints.authentication.PerformTotpVerificationService
import com.dashlane.server.api.endpoints.authentication.PerformU2FVerificationService

class AuthenticationAuthTicketHelperImpl(
    private val deviceRegistrationInfo: DeviceRegistrationInfo,
    private val performEmailTokenVerificationService: PerformEmailTokenVerificationService,
    private val performTotpVerificationService: PerformTotpVerificationService,
    private val performU2FVerificationService: PerformU2FVerificationService,
    private val performDuoPushVerificationService: PerformDuoPushVerificationService,
    private val performSsoVerificationService: PerformSsoVerificationService,
    private val completeDeviceRegistrationWithAuthTicketService: CompleteDeviceRegistrationWithAuthTicketService,
    private val completeLoginWithAuthTicketService: CompleteLoginWithAuthTicketService,
) : AuthenticationAuthTicketHelper {
    override suspend fun verifyEmailToken(
        login: String,
        token: PerformEmailTokenVerificationService.Request.Token,
    ): AuthenticationAuthTicketHelper.VerificationResult = VerificationResult(
        login = login,
        verificationResponse = performEmailTokenVerificationService.execute(
            request = PerformEmailTokenVerificationService.Request(
                login = login,
                token = token,
            ),
        ),
    )

    override suspend fun verifyTotp(
        login: String,
        otp: String,
    ): AuthenticationAuthTicketHelper.VerificationResult = VerificationResult(
        login = login,
        verificationResponse = performTotpVerificationService.execute(
            request = PerformTotpVerificationService.Request(
                login = login,
                otp = otp,
            ),
        ),
    )

    override suspend fun verifyU2f(
        login: String,
        challengeAnswer: PerformU2FVerificationService.Request.ChallengeAnswer,
    ): AuthenticationAuthTicketHelper.VerificationResult = VerificationResult(
        login = login,
        verificationResponse = performU2FVerificationService.execute(
            request = PerformU2FVerificationService.Request(
                login = login,
                challengeAnswer = challengeAnswer,
            ),
        ),
    )

    override suspend fun verifyDuoPush(
        login: String,
    ): AuthenticationAuthTicketHelper.VerificationResult = VerificationResult(
        login = login,
        verificationResponse = performDuoPushVerificationService.execute(
            request = PerformDuoPushVerificationService.Request(
                login = login,
            ),
        ),
    )

    override suspend fun verifySso(
        login: String,
        ssoToken: String,
    ): AuthenticationAuthTicketHelper.VerificationResult = VerificationResult(
        login = login,
        verificationResponse = performSsoVerificationService.execute(
            request = PerformSsoVerificationService.Request(
                login = login,
                ssoToken = ssoToken,
            ),
        ),
    )

    private inner class VerificationResult(
        private val login: String,
        verificationResponse: Response<AuthVerificationResult>,
    ) : AuthenticationAuthTicketHelper.VerificationResult {
        override val authTicket = verificationResponse.data.authTicket

        override suspend fun registerDevice() = completeDeviceRegistrationWithAuthTicketService.execute(
            request = CompleteDeviceRegistrationWithAuthTicketService.Request(
                login = login,
                authTicket = CompleteDeviceRegistrationWithAuthTicketService.Request.AuthTicket(authTicket),
                device = deviceRegistrationInfo.toAuthRegistrationDevice(),
            ),
        ).data

        override suspend fun login(accessKey: String) = completeLoginWithAuthTicketService.execute(
            request = CompleteLoginWithAuthTicketService.Request(
                deviceAccessKey = accessKey,
                login = login,
                authTicket = CompleteLoginWithAuthTicketService.Request.AuthTicket(authTicket),
            ),
        ).data
    }
}