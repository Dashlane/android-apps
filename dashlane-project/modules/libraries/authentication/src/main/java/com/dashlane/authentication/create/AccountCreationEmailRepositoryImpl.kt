package com.dashlane.authentication.create

import com.dashlane.authentication.AuthenticationAccountAlreadyExistsException
import com.dashlane.authentication.AuthenticationContactSsoAdministratorException
import com.dashlane.authentication.AuthenticationEmptyEmailException
import com.dashlane.authentication.AuthenticationExpiredVersionException
import com.dashlane.authentication.AuthenticationInvalidEmailException
import com.dashlane.authentication.toAuthenticationException
import com.dashlane.server.api.endpoints.account.AccountExistsService
import com.dashlane.server.api.endpoints.account.CreateAccountService
import com.dashlane.server.api.endpoints.account.exceptions.ExpiredVersionException
import com.dashlane.server.api.endpoints.account.exceptions.SsoBlockedException
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.util.isValidEmail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class AccountCreationEmailRepositoryImpl(
    private val accountExistsService: AccountExistsService
) : AccountCreationEmailRepository {
    override suspend fun validate(email: String): AccountCreationEmailRepository.Result =
        withContext(Dispatchers.Default) { validateImpl(email) }

    private suspend fun validateImpl(email: String): AccountCreationEmailRepository.Result {
        if (email.isEmpty()) {
            throw AuthenticationEmptyEmailException()
        }
        if (!email.isValidEmail()) {
            throw AuthenticationInvalidEmailException(remoteCheck = false)
        }
        val login = try {
            AccountExistsService.Request.Login(email.lowercase(Locale.US))
        } catch (e: IllegalArgumentException) {
            throw AuthenticationInvalidEmailException(remoteCheck = false, cause = e)
        }
        if (!CreateAccountService.Request.Login.regex.matches(login.value)) {
            throw AuthenticationInvalidEmailException(remoteCheck = false)
        }

        val request = AccountExistsService.Request(
            login = login
        )
        val response = try {
            accountExistsService.execute(request)
        } catch (e: SsoBlockedException) {
            throw AuthenticationContactSsoAdministratorException(cause = e)
        } catch (e: ExpiredVersionException) {
            throw AuthenticationExpiredVersionException(cause = e)
        } catch (e: DashlaneApiException) {
            throw e.toAuthenticationException()
        }

        val responseData = response.data

        if (responseData.accountExists) {
            throw AuthenticationAccountAlreadyExistsException()
        }

        
        if (responseData.sso && responseData.ssoServiceProviderUrl == null) {
            throw AuthenticationContactSsoAdministratorException()
        }

        return when (responseData.emailValidity) {
            AccountExistsService.Data.EmailValidity.VALID -> responseData.toSuccess(login.value)
            AccountExistsService.Data.EmailValidity.UNLIKELY -> AccountCreationEmailRepository.Result.Warning(
                success = responseData.toSuccess(login.value)
            )
            AccountExistsService.Data.EmailValidity.INVALID -> throw AuthenticationInvalidEmailException(
                remoteCheck = true
            )
        }
    }
}

private fun AccountExistsService.Data.toSuccess(
    login: String
) = AccountCreationEmailRepository.Result.Success(
    login = login,
    country = country,
    isEuropeanUnion = isEuropeanUnion,
    ssoServiceProviderUrl = ssoServiceProviderUrl,
    ssoIsNitroProvider = ssoIsNitroProvider ?: false,
    isB2B = isProposed || isAccepted
)
