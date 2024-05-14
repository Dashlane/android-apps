package com.dashlane.authentication.login

import com.dashlane.server.api.endpoints.authentication.AuthLoginAuthTicketService
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationAuthTicketService
import com.dashlane.server.api.endpoints.authentication.AuthVerificationEmailTokenService
import com.dashlane.server.api.endpoints.authentication.AuthVerificationU2fService

interface AuthenticationAuthTicketHelper {
    suspend fun verifyEmailToken(
        login: String,
        token: AuthVerificationEmailTokenService.Request.Token
    ): VerificationResult

    suspend fun verifyTotp(
        login: String,
        otp: String
    ): VerificationResult

    suspend fun verifyU2f(
        login: String,
        challengeAnswer: AuthVerificationU2fService.Request.ChallengeAnswer
    ): VerificationResult

    suspend fun verifyDuoPush(
        login: String
    ): VerificationResult

    suspend fun verifySso(
        login: String,
        ssoToken: String
    ): VerificationResult

    suspend fun verifyDashlaneAuthenticator(
        login: String
    ): VerificationResult

    interface VerificationResult {
        val authTicket: String

        suspend fun registerDevice(): AuthRegistrationAuthTicketService.Data

        suspend fun login(accessKey: String): AuthLoginAuthTicketService.Data
    }
}