package com.dashlane.authentication.create

import com.dashlane.authentication.AuthenticationAccountAlreadyExistsException
import com.dashlane.authentication.AuthenticationContactSsoAdministratorException
import com.dashlane.authentication.AuthenticationEmptyEmailException
import com.dashlane.authentication.AuthenticationExpiredVersionException
import com.dashlane.authentication.AuthenticationInvalidEmailException
import com.dashlane.authentication.toAuthenticationException
import com.dashlane.server.api.endpoints.account.CreateUserService
import com.dashlane.server.api.endpoints.account.RequestAccountCreationService
import com.dashlane.server.api.endpoints.account.exceptions.ExpiredVersionException
import com.dashlane.server.api.endpoints.account.exceptions.SsoBlockedException
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.util.isValidEmail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class AccountCreationEmailRepositoryImpl(
    private val requestAccountCreationService: RequestAccountCreationService,
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
            RequestAccountCreationService.Request.Login(email.lowercase(Locale.US))
        } catch (e: IllegalArgumentException) {
            throw AuthenticationInvalidEmailException(remoteCheck = false, cause = e)
        }
        if (!CreateUserService.Request.Login.regex.matches(login.value)) {
            throw AuthenticationInvalidEmailException(remoteCheck = false)
        }

        val request = RequestAccountCreationService.Request(
            login = login,
        )
        val response = try {
            requestAccountCreationService.execute(request)
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
            RequestAccountCreationService.Data.EmailValidity.VALID -> responseData.toSuccess(login.value)
            RequestAccountCreationService.Data.EmailValidity.UNLIKELY -> AccountCreationEmailRepository.Result.Warning(
                success = responseData.toSuccess(login.value),
            )
            RequestAccountCreationService.Data.EmailValidity.INVALID -> throw AuthenticationInvalidEmailException(
                remoteCheck = true,
            )
        }
    }
}

private fun RequestAccountCreationService.Data.toSuccess(
    login: String,
) = AccountCreationEmailRepository.Result.Success(
    login = login,
    country = country,
    isEuropeanUnion = isEuropeanUnion,
    ssoServiceProviderUrl = ssoServiceProviderUrl,
    ssoIsNitroProvider = ssoIsNitroProvider ?: false,
    isB2B = isProposed || isAccepted,
)