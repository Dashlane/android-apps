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
    override val logger: LoginTokenLogger,
    private val secondFactoryRepository: AuthenticationSecondFactoryRepository
) : LoginBaseDataProvider<LoginTokenContract.Presenter>(), LoginTokenContract.DataProvider {

    companion object {
        const val VALID_TOKEN_LENGTH = 6
    }

    lateinit var emailSecondFactor: AuthenticationSecondFactor.EmailToken

    override val username: String
        get() = emailSecondFactor.login

    override fun onShow() = logger.logLand()
    override fun onBack() = logger.logBack()

    override suspend fun validateToken(token: String, auto: Boolean): RegisteredUserDevice = try {
        val result = secondFactoryRepository.validate(
            emailSecondFactor,
            token
        )
        logger.logTokenSuccess(auto)
        result.registeredUserDevice
    } catch (e: AuthenticationException) {
        when (e) {
            is AuthenticationInvalidTokenException,
                
            is AuthenticationAccountConfigurationChangedException,
                
            is AuthenticationLockedOutException -> {
                logger.logInvalidToken(auto)
                throw LoginTokenContract.InvalidTokenException(false, e)
            }
            is AuthenticationOfflineException -> {
                logger.logNetworkError(auto)
                throw LoginBaseContract.OfflineException(e)
            }
            else -> {
                logger.logNetworkError(auto)
                throw LoginBaseContract.NetworkException(e)
            }
        }
    }

    override fun resendTokenClicked() = logger.logWhereIsMyCodeClick()
    override fun resendTokenPopupOpened() = logger.logWhereIsMyCodeAppear()
    override fun resendTokenPopupClosed() = logger.logCloseWhereIsMyCode()
    override fun resendTokenPopupDismissed() = logger.logDismissWhereIsMyCode()

    override suspend fun resendToken() {
        logger.logResendCode()
        try {
            secondFactoryRepository.resendToken(AuthenticationSecondFactor.EmailToken(username))
        } catch (_: AuthenticationException) {
            
        }
    }
}
