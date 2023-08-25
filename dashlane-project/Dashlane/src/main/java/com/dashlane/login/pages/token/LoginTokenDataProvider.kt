package com.dashlane.login.pages.token

import com.dashlane.authentication.AuthenticationAccountConfigurationChangedException
import com.dashlane.authentication.AuthenticationException
import com.dashlane.authentication.AuthenticationInvalidTokenException
import com.dashlane.authentication.AuthenticationLockedOutException
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.login.AuthenticationSecondFactoryRepository
import com.dashlane.login.pages.LoginBaseContract
import com.dashlane.login.pages.LoginBaseDataProvider
import javax.inject.Inject

class LoginTokenDataProvider @Inject constructor(
    private val secondFactoryRepository: AuthenticationSecondFactoryRepository
) : LoginBaseDataProvider<LoginTokenContract.Presenter>(), LoginTokenContract.DataProvider {

    companion object {
        const val VALID_TOKEN_LENGTH = 6
    }

    lateinit var emailSecondFactor: AuthenticationSecondFactor.EmailToken

    override val username: String
        get() = emailSecondFactor.login

    override suspend fun validateToken(token: String, auto: Boolean): Pair<RegisteredUserDevice, String?> = try {
        val result = secondFactoryRepository.validate(
            emailSecondFactor,
            token
        )
        result.registeredUserDevice to result.authTicket
    } catch (e: AuthenticationException) {
        when (e) {
            is AuthenticationInvalidTokenException,
                
            is AuthenticationAccountConfigurationChangedException,
                
            is AuthenticationLockedOutException -> {
                throw LoginTokenContract.InvalidTokenException(false, e)
            }
            is AuthenticationOfflineException -> {
                throw LoginBaseContract.OfflineException(e)
            }
            else -> {
                throw LoginBaseContract.NetworkException(e)
            }
        }
    }

    override suspend fun resendToken() {
        try {
            secondFactoryRepository.resendToken(AuthenticationSecondFactor.EmailToken(username))
        } catch (_: AuthenticationException) {
            
        }
    }
}
