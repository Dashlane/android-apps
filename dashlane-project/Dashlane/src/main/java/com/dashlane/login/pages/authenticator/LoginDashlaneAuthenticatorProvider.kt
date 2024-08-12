package com.dashlane.login.pages.authenticator

import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.login.AuthenticationSecondFactoryRepository
import com.dashlane.login.pages.LoginBaseDataProvider
import javax.inject.Inject

class LoginDashlaneAuthenticatorProvider @Inject constructor(
    private val secondFactoryRepository: AuthenticationSecondFactoryRepository
) : LoginBaseDataProvider<LoginDashlaneAuthenticatorContract.Presenter>(),
    LoginDashlaneAuthenticatorContract.DataProvider {
    override lateinit var secondFactor: AuthenticationSecondFactor

    override val username: String
        get() = secondFactor.login

    override suspend fun executeAuthenticatorAuthentication(): Pair<RegisteredUserDevice, String?> {
        val result = secondFactoryRepository.validate(
            checkNotNull(secondFactor.authenticator) {
                "AuthenticationSecondFactor's authenticator must not be null here"
            }
        )
        return result.registeredUserDevice to result.authTicket
    }

    override suspend fun sendEmailToken(emailToken: AuthenticationSecondFactor.EmailToken) {
        secondFactoryRepository.resendToken(emailToken)
    }
}
