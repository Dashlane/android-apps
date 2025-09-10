package com.dashlane.accountrecoverykey.activation.generate

import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.CreateKeyErrorName
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.events.user.CreateAccountRecoveryKey
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.user.UserAccountInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun activateArk(
    sessionManager: SessionManager,
    preferencesManager: PreferencesManager,
    accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
    logRepository: LogRepository,
    state: AccountRecoveryKeyGenerateState.Loading,
): Flow<AccountRecoveryKeyGenerateEvent> {
    return flow {
        logRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.SETTINGS_SECURITY_RECOVERY_KEY_STORE)

        val session = sessionManager.session ?: throw IllegalStateException("session cannot be null")
        val accountType = preferencesManager[session.username].accountType
            ?.let { UserAccountInfo.AccountType.fromString(it) }
            ?: throw IllegalStateException("accountType cannot be null")

        accountRecoveryKeyRepository.requestActivation()
            .onSuccess { accountRecoveryKey ->
                val formattedAccountRecoveryKey = accountRecoveryKey.chunked(4).joinToString("-")
                emit(AccountRecoveryKeyGenerateEvent.ArkActivated(accountType, formattedAccountRecoveryKey))
            }
            .onFailure {
                logRepository.queueEvent(
                    CreateAccountRecoveryKey(flowStep = FlowStep.ERROR, createKeyErrorName = CreateKeyErrorName.UNKNOWN),
                )
                emit(AccountRecoveryKeyGenerateEvent.Error(AccountRecoveryKeyGenerateError.Generic))
            }
    }
}