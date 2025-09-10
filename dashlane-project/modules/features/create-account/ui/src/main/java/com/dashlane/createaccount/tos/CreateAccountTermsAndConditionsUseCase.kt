package com.dashlane.createaccount.tos

import com.dashlane.authentication.AuthenticationExpiredVersionException
import com.dashlane.authentication.create.AccountCreator
import com.dashlane.common.logger.adjust.AttributionsLogDataProvider
import com.dashlane.createaccount.CreateAccountRepository
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AccountCreationStatus
import com.dashlane.hermes.generated.events.user.CreateAccount
import com.dashlane.user.UserAccountInfo
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

fun createAccount(
    createAccountRepository: CreateAccountRepository,
    attributionsLogDataProvider: AttributionsLogDataProvider,
    accountCreator: AccountCreator,
    logRepository: LogRepository,
    state: CreateAccountTermsAndConditionsState.View.Loading,
): Flow<CreateAccountTermsAndConditionsEvent> {
    return flow<CreateAccountTermsAndConditionsEvent> {
        val createAccountData = createAccountRepository.getCreateAccountData()
        val result = createAccountData.result ?: throw IllegalStateException("No pending account")
        val password = createAccountData.password ?: throw IllegalStateException("No password")
        coroutineScope {
            val createAccountDeferred = async {
                accountCreator.createAccount(
                    username = result.login,
                    password = password,
                    accountType = UserAccountInfo.AccountType.MasterPassword,
                    termsState = AccountCreator.TermsState(conditions = true, offers = state.data.isOffersChecked),
                    biometricEnabled = createAccountData.biometricEnabled,
                    resetMpEnabled = createAccountData.biometricRecoveryEnabled,
                    country = result.country,
                )
            }

            
            val logDeferred = async {
                try {
                    val attributionLogData = withTimeout(1_000) { attributionsLogDataProvider.getAttributionLogData() }
                    logRepository.queueEvent(
                        CreateAccount(
                            isMarketingOptIn = attributionLogData.isMarketingOptIn,
                            androidMarketing = attributionLogData.androidAttribution,
                            status = AccountCreationStatus.SUCCESS,
                        ),
                    )
                } catch (e: TimeoutCancellationException) {
                }
            }
            createAccountDeferred.await()
            logDeferred.await()
        }

        createAccountRepository.clearData()

        emit(CreateAccountTermsAndConditionsEvent.AccountCreated)
    }
        .catch { e ->
            val error = when (e) {
                is AccountCreator.CannotInitializeSessionException -> CreateAccountTermsAndConditionsError.Network
                is AuthenticationExpiredVersionException -> CreateAccountTermsAndConditionsError.ExpiredVersion
                else -> CreateAccountTermsAndConditionsError.Generic
            }
            emit(CreateAccountTermsAndConditionsEvent.Error(error = error))
        }
}