package com.dashlane.createaccount.email

import com.dashlane.authentication.AuthenticationAccountAlreadyExistsException
import com.dashlane.authentication.AuthenticationContactSsoAdministratorException
import com.dashlane.authentication.AuthenticationEmptyEmailException
import com.dashlane.authentication.AuthenticationException
import com.dashlane.authentication.AuthenticationExpiredVersionException
import com.dashlane.authentication.AuthenticationInvalidEmailException
import com.dashlane.authentication.create.AccountCreationEmailRepository
import com.dashlane.createaccount.CreateAccountRepository
import com.dashlane.data.login.SsoInfo
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AccountCreationStatus
import com.dashlane.hermes.generated.events.user.CreateAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

fun validateEmail(
    createAccountRepository: CreateAccountRepository,
    accountCreationEmailRepository: AccountCreationEmailRepository,
    logRepository: LogRepository,
    state: CreateAccountEmailState.View.Loading,
): Flow<CreateAccountEmailEvent> {
    return flow {
        state.data.result?.let {
            emit(checkPendingAccount(createAccountRepository, it))
            return@flow
        }
        val email = state.data.email
        when (val result = accountCreationEmailRepository.validate(email.text)) {
            is AccountCreationEmailRepository.Result.Success -> emit(
                checkPendingAccount(
                    createAccountRepository,
                    result,
                ),
            )
            is AccountCreationEmailRepository.Result.Warning -> emit(
                CreateAccountEmailEvent.EmailUnlikelyValid(
                    result.success,
                ),
            )
        }
    }
        .catch { e ->
            val error = when (e) {
                is AuthenticationEmptyEmailException -> {
                    logRepository.queueEvent(CreateAccount(false, status = AccountCreationStatus.ERROR_NOT_VALID_EMAIL))
                    CreateAccountEmailError.EmptyEmail
                }

                is AuthenticationInvalidEmailException -> {
                    logRepository.queueEvent(CreateAccount(false, status = AccountCreationStatus.ERROR_NOT_VALID_EMAIL))
                    CreateAccountEmailError.InvalidEmail
                }

                is AuthenticationAccountAlreadyExistsException -> {
                    logRepository.queueEvent(CreateAccount(false, status = AccountCreationStatus.ERROR_ACCOUNT_ALREADY_EXISTS))
                    CreateAccountEmailError.AlreadyExist
                }

                is AuthenticationContactSsoAdministratorException -> CreateAccountEmailError.AdminSso
                is AuthenticationExpiredVersionException -> CreateAccountEmailError.AuthenticationExpiredVersion
                is AuthenticationException -> CreateAccountEmailError.Network
                else -> CreateAccountEmailError.Generic
            }
            emit(CreateAccountEmailEvent.Error(error))
        }
}

private suspend fun checkPendingAccount(
    createAccountRepository: CreateAccountRepository,
    result: AccountCreationEmailRepository.Result.Success,
): CreateAccountEmailEvent {
    val ssoServiceProviderUrl = result.ssoServiceProviderUrl
    return if (ssoServiceProviderUrl != null) {
        CreateAccountEmailEvent.AccountIsSso(
            ssoInfo = SsoInfo(
                login = result.login,
                serviceProviderUrl = ssoServiceProviderUrl,
                isNitroProvider = result.ssoIsNitroProvider,
                migration = null,
            ),
        )
    } else {
        createAccountRepository.updateResult(result)
        CreateAccountEmailEvent.EmailValid(
            result.login,
            result.isB2B,
        )
    }
}