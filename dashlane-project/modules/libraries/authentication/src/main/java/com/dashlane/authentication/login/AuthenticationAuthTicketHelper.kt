package com.dashlane.authentication.login

import com.dashlane.server.api.endpoints.authentication.CompleteDeviceRegistrationWithAuthTicketService
import com.dashlane.server.api.endpoints.authentication.CompleteLoginWithAuthTicketService
import com.dashlane.server.api.endpoints.authentication.PerformEmailTokenVerificationService
import com.dashlane.server.api.endpoints.authentication.PerformU2FVerificationService

interface AuthenticationAuthTicketHelper {
    suspend fun verifyEmailToken(
        login: String,
        token: PerformEmailTokenVerificationService.Request.Token,
    ): VerificationResult

    suspend fun verifyTotp(
        login: String,
        otp: String,
    ): VerificationResult

    suspend fun verifyU2f(
        login: String,
        challengeAnswer: PerformU2FVerificationService.Request.ChallengeAnswer,
    ): VerificationResult

    suspend fun verifyDuoPush(
        login: String,
    ): VerificationResult

    suspend fun verifySso(
        login: String,
        ssoToken: String,
    ): VerificationResult

    interface VerificationResult {
        val authTicket: String

        suspend fun registerDevice(): CompleteDeviceRegistrationWithAuthTicketService.Data

        suspend fun login(accessKey: String): CompleteLoginWithAuthTicketService.Data
    }
}