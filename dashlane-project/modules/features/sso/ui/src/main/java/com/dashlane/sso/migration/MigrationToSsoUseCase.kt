package com.dashlane.sso.migration

import com.dashlane.account.UserAccountStorage
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.accountrecoverykey.AccountRecoveryKeySettingStateRefresher
import com.dashlane.authentication.SsoServerKeyFactory
import com.dashlane.authentication.login.AuthenticationAuthTicketHelper
import com.dashlane.authentication.sso.GetSsoInfoResult
import com.dashlane.changemasterpassword.MasterPasswordChanger
import com.dashlane.crypto.keys.AppKey
import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.DeleteKeyReason
import com.dashlane.hermes.generated.definitions.Mode
import com.dashlane.hermes.generated.definitions.Reason
import com.dashlane.hermes.generated.events.user.AskAuthentication
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPass
import com.dashlane.lock.LockType
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.endpoints.AccountType
import com.dashlane.server.api.endpoints.account.ChangeAccountTypeService
import com.dashlane.session.SessionManager
import com.dashlane.session.authorization
import com.dashlane.user.UserAccountInfo
import com.dashlane.user.Username
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

fun startSsoLogin(
    logRepository: LogRepository,
    state: MigrationToSsoState.View.LoadingSsoLogin,
): Flow<MigrationToSsoEvent> {
    return flow {
        logRepository.queueEvent(AskAuthentication(mode = Mode.SSO, reason = Reason.LOGIN, verificationMode = null))
    }
}


@SuppressWarnings("kotlin:S107")
fun migrateToSso(
    sessionManager: SessionManager,
    authTicketHelper: AuthenticationAuthTicketHelper,
    ssoServerKeyFactory: SsoServerKeyFactory,
    masterPasswordChanger: MasterPasswordChanger,
    preferencesManager: PreferencesManager,
    accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
    accountRecoveryKeySettingStateRefresher: AccountRecoveryKeySettingStateRefresher,
    lockManager: LockManager,
    userAccountStorage: UserAccountStorage,
    changeAccountTypeService: ChangeAccountTypeService,
    state: MigrationToSsoState.View.LoadingMigration,
): Flow<MigrationToSsoEvent> {
    return flow {

        if (state.result == null || state.result is GetSsoInfoResult.Error) {
            logout(sessionManager)
            emit(MigrationToSsoEvent.Cancel)
            return@flow
        }

        val result = state.result as GetSsoInfoResult.Success
        val login = state.data.login

        if (login != result.login) {
            logout(sessionManager)
            emit(MigrationToSsoEvent.Cancel)
            return@flow
        }

        val userSsoInfo = result.userSsoInfo
        val serviceProviderKey = userSsoInfo.key.decodeBase64ToByteArray()
        val authTicket = state.data.totpAuthTicket ?: authTicketHelper.verifySso(login, userSsoInfo.ssoToken).authTicket
        val serverKey = ssoServerKeyFactory.generateSsoServerKey()
        val ssoKey = AppKey.SsoKey.create(serverKey = serverKey, serviceProviderKey = serviceProviderKey)

        val migrateToSsoResult = masterPasswordChanger.migrateToSsoMember(
            ssoKey = ssoKey,
            authTicket = authTicket,
            ssoServerKey = serverKey,
        )

        if (!migrateToSsoResult) {
            logout(sessionManager)
            emit(MigrationToSsoEvent.Cancel)
            return@flow
        }

        val session = sessionManager.session ?: throw IllegalStateException("Invalid session")
        val shouldLaunchInitialSync = preferencesManager[login].getInt(ConstantsPrefs.TIMESTAMP_LABEL, 0) == 0

        accountRecoveryKeyRepository.disableRecoveryKey(DeleteKeyReason.VAULT_KEY_CHANGED)
        accountRecoveryKeySettingStateRefresher.refresh()

        lockManager.unlock(session, LockPass.ofPassword(ssoKey))
        val locks = lockManager.getLocks(Username.ofEmail(login))

        val accountType = userAccountStorage[session.username]?.accountType
        if (accountType is UserAccountInfo.AccountType.InvisibleMasterPassword) {
            val request = ChangeAccountTypeService.Request(newAccountType = AccountType.MASTERPASSWORD)
            changeAccountTypeService.execute(session.authorization, request)
            userAccountStorage.saveAccountType(session.userId, UserAccountInfo.AccountType.MasterPassword)
        }

        emit(
            MigrationToSsoEvent.Success(
                shouldLaunchInitialSync = shouldLaunchInitialSync,
                goToApplicationLock = LockType.PinCode !in locks && LockType.Biometric !in locks,
            ),
        )
    }
        .catch { exception ->
            logout(sessionManager)
            emit(MigrationToSsoEvent.Error(MigrationToSsoError.Generic))
        }
}

fun logout(
    sessionManager: SessionManager,
    state: MigrationToSsoState.View.Logout,
): Flow<MigrationToSsoEvent> {
    return flow {
        logout(sessionManager)
        emit(MigrationToSsoEvent.Cancel)
    }
}

private suspend fun logout(
    sessionManager: SessionManager,
) {
    sessionManager.session?.let { session ->
        sessionManager.destroySession(session, byUser = false, forceLogout = false)
    }
}