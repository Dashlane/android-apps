package com.dashlane.createaccount.pages.email

import com.dashlane.authentication.AuthenticationAccountAlreadyExistsException
import com.dashlane.authentication.AuthenticationContactSsoAdministratorException
import com.dashlane.authentication.AuthenticationEmptyEmailException
import com.dashlane.authentication.AuthenticationException
import com.dashlane.authentication.AuthenticationExpiredVersionException
import com.dashlane.authentication.AuthenticationInvalidEmailException
import com.dashlane.authentication.create.AccountCreationEmailRepository
import com.dashlane.createaccount.pages.CreateAccountBaseContract
import com.dashlane.login.LoginSuccessIntentFactory
import com.dashlane.preference.GlobalPreferencesManager
import com.skocken.presentation.provider.BaseDataProvider
import javax.inject.Inject

class CreateAccountEmailDataProvider @Inject constructor(
    private val accountCreationEmailRepository: AccountCreationEmailRepository,
    private val logger: CreateAccountEmailLogger,
    private val loginSuccessIntentFactory: LoginSuccessIntentFactory,
    private val preferencesManager: GlobalPreferencesManager
) : BaseDataProvider<CreateAccountEmailContract.Presenter>(),
    CreateAccountEmailContract.DataProvider {
    override fun onShow() {
        logger.logLand()
    }

    override fun onBack() = logger.logBack()
    override fun confirmEmailShown() = logger.logShowConfirmEmail()
    override fun confirmEmailCancelled() = logger.logCancelConfirmEmail()

    override fun emailConfirmed(email: String, inEuropeanUnion: Boolean) {
        logger.logConfirmEmail()
    }

    override fun getTrackingInstallationId() = preferencesManager.installationTrackingId

    override suspend fun validateEmail(email: String): CreateAccountEmailContract.PendingAccount =
        try {
            when (val result = accountCreationEmailRepository.validate(email)) {
                is AccountCreationEmailRepository.Result.Success -> {
                    logger.logValidEmail()
                    result.toPendingAccount()
                }
                is AccountCreationEmailRepository.Result.Warning -> {
                    logger.logUnlikelyEmail()
                    result.success.toPendingAccount(emailLikelyInvalid = true)
                }
            }
        } catch (e: AuthenticationEmptyEmailException) {
            logger.logEmptyEmail()
            throw CreateAccountEmailContract.EmptyEmailException(e)
        } catch (e: AuthenticationInvalidEmailException) {
            if (e.remoteCheck) {
                logger.logInvalidEmailServer()
            } else {
                logger.logInvalidEmailLocal()
            }
            throw CreateAccountEmailContract.InvalidEmailException(e)
        } catch (e: AuthenticationAccountAlreadyExistsException) {
            logger.logAccountExists()
            throw CreateAccountEmailContract.AccountAlreadyExistsException(e)
        } catch (e: AuthenticationContactSsoAdministratorException) {
            throw CreateAccountEmailContract.ContactSsoAdministratorException(e)
        } catch (e: AuthenticationExpiredVersionException) {
            throw CreateAccountEmailContract.ExpiredVersionException(e)
        } catch (e: AuthenticationException) {
            logger.logNetworkError()
            throw CreateAccountBaseContract.NetworkException(e)
        }

    private fun AccountCreationEmailRepository.Result.Success.toPendingAccount(
        emailLikelyInvalid: Boolean = false
    ) = CreateAccountEmailContract.PendingAccount(
        email = login,
        emailLikelyInvalid = emailLikelyInvalid,
        inEuropeanUnion = isEuropeanUnion,
        country = country,
        loginSsoIntent = ssoServiceProviderUrl?.let {
            loginSuccessIntentFactory.createLoginSsoIntent(
                login,
                it,
                ssoIsNitroProvider
            )
        }
    )
}