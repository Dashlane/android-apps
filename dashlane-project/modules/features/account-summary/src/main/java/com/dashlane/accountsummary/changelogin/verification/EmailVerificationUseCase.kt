package com.dashlane.accountsummary.changelogin.verification

import androidx.compose.ui.text.input.TextFieldValue
import com.dashlane.createaccount.AccountCreationSetup
import com.dashlane.crypto.keys.LocalKey
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.data.login.LoginMode
import com.dashlane.database.DatabaseException
import com.dashlane.database.DatabaseProvider
import com.dashlane.debug.services.DaDaDaBase
import com.dashlane.hardwaresecurity.CryptoObjectHelper
import com.dashlane.hardwaresecurity.SecurityHelper
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.ChangeLoginEmailFlowStep
import com.dashlane.hermes.generated.definitions.ChangeLoginError
import com.dashlane.hermes.generated.events.user.UserChangeLoginEmail
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPass
import com.dashlane.lock.LockType.Biometric
import com.dashlane.lock.LockType.PinCode
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.authenticationqa.QaGetAllTokensForTestLoginService
import com.dashlane.server.api.endpoints.user.UserCompleteLoginChange
import com.dashlane.server.api.endpoints.user.UserSendLoginChangeToken
import com.dashlane.server.api.endpoints.user.exceptions.TokenValidationFailedException
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.Session
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionResult
import com.dashlane.session.SessionTrasher
import com.dashlane.session.authorization
import com.dashlane.storage.securestorage.LocalKeyRepository
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.sync.DataSync
import com.dashlane.user.Username
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion

fun logPageView(
    logRepository: LogRepository,
    state: EmailVerificationState.View.Initial,
): Flow<EmailVerificationEvent> {
    return flow<EmailVerificationEvent> {
        logRepository.queueEvent(UserChangeLoginEmail(changeLoginEmailFlowStep = ChangeLoginEmailFlowStep.VERIFY_TOKEN))
    }
}

fun checkDebugToken(
    sessionManager: SessionManager,
    dadadaBase: DaDaDaBase,
    qaGetAllTokensForTestLoginService: QaGetAllTokensForTestLoginService,
    state: EmailVerificationState.View.Initial,
): Flow<EmailVerificationEvent> {
    return flow {
        if (!dadadaBase.isEnabled) return@flow
        val session = sessionManager.session ?: return@flow
        val request = QaGetAllTokensForTestLoginService.Request(session.username.email)
        val response = qaGetAllTokensForTestLoginService.execute(request)
        val token = response.data.changeLoginToken ?: return@flow
        emit(EmailVerificationEvent.TokenChanged(TextFieldValue(token)))
        delay(500)
        emit(EmailVerificationEvent.NextClicked)
    }
        .catch {
        }
}

fun resendEmailToken(
    sessionManager: SessionManager,
    userSendLoginChangeToken: UserSendLoginChangeToken,
    ioCoroutineDispatcher: CoroutineDispatcher,
    state: EmailVerificationState.View.LoadingNewToken,
): Flow<EmailVerificationEvent> {
    return flow {
        val session = sessionManager.session ?: run {
            emit(EmailVerificationEvent.SuccessNewToken)
            return@flow
        }
        userSendLoginChangeToken.execute(session.authorization)

        emit(EmailVerificationEvent.SuccessNewToken)
    }
        .flowOn(ioCoroutineDispatcher)
        .catch { throwable ->
            emit(EmailVerificationEvent.SuccessNewToken)
        }
}

@Suppress("LongMethod", "kotlin:S107", "ComplexMethod")
fun changeLoginEmail(
    sessionManager: SessionManager,
    sessionCredentialsSaver: SessionCredentialsSaver,
    sessionTrasher: SessionTrasher,
    userCompleteLoginChange: UserCompleteLoginChange,
    dataSync: DataSync,
    localKeyRepository: LocalKeyRepository,
    globalPreferencesManager: GlobalPreferencesManager,
    preferencesManager: PreferencesManager,
    lockManager: LockManager,
    securityHelper: SecurityHelper,
    cryptoObjectHelper: CryptoObjectHelper,
    userSecureStorageManager: UserSecureStorageManager,
    databaseProvider: DatabaseProvider,
    accountCreationSetup: AccountCreationSetup,
    logRepository: LogRepository,
    ioCoroutineDispatcher: CoroutineDispatcher,
    state: EmailVerificationState.View.Loading,
): Flow<EmailVerificationEvent> {
    return flow {
        val previousSession = sessionManager.session ?: run {
            emit(EmailVerificationEvent.Error(error = EmailVerificationError.Generic))
            return@flow
        }
        val token = state.data.token?.text ?: throw IllegalStateException("changeLoginEmail token is null")

        completeLoginChange(token, previousSession.authorization, userCompleteLoginChange)

        val previousUser = previousSession.username
        val previousPin = userSecureStorageManager.readPin(previousSession.localKey, previousUser)
        val previousRsaPrivateKey = userSecureStorageManager.readRsaPrivateKey(previousSession.localKey, previousUser)
        val newEmail = state.data.newEmail
        val newUsername = Username.ofEmail(newEmail)

        localKeyRepository.storeLocalKey(
            localKey = previousSession.localKey,
            username = newUsername,
            appKey = previousSession.appKey,
            cryptographyMarker = CryptographyMarker.Flexible.Defaults.argon2d,
        )

        
        previousSession.localKey.clone().use(LocalKey::cryptographyKey).use { key ->
            val success = databaseProvider.rename(userName = previousUser.email, newUserName = newEmail, key = key)
            if (!success) {
                clearLocalDataOnError(
                    previousUsername = previousUser,
                    newUsername = newUsername,
                    sessionTrasher = sessionTrasher,
                    globalPreferencesManager = globalPreferencesManager,
                )
                emit(EmailVerificationEvent.Error(error = EmailVerificationError.InvalidSession))
                return@flow
            }
        }

        val sessionResult = sessionManager.loadSession(
            username = newUsername,
            appKey = previousSession.appKey,
            secretKey = previousSession.secretKey,
            localKey = previousSession.localKey,
            accessKey = previousSession.accessKey,
            loginMode = LoginMode.SessionRestore,
        )
        when (sessionResult) {
            is SessionResult.Error -> {
                    message = "Failed to load session ${sessionResult.errorCode} ${sessionResult.errorReason}",
                    throwable = sessionResult.cause,
                )
                globalPreferencesManager.setLastLoggedInUser(newEmail)
                emit(EmailVerificationEvent.Error(error = EmailVerificationError.InvalidSession))
                return@flow
            }
            is SessionResult.Success -> {
                migrateLocalData(
                    newSession = sessionResult.session,
                    previousUsername = previousUser,
                    previousPin = previousPin,
                    previousRsaPrivateKey = previousRsaPrivateKey,
                    newUsername = newUsername,
                    preferencesManager = preferencesManager,
                    lockManager = lockManager,
                    securityHelper = securityHelper,
                    cryptoObjectHelper = cryptoObjectHelper,
                    sessionCredentialsSaver = sessionCredentialsSaver,
                    sessionTrasher = sessionTrasher,
                    userSecureStorageManager = userSecureStorageManager,
                    globalPreferencesManager = globalPreferencesManager,
                    accountCreationSetup = accountCreationSetup,
                )
                logRepository.queueEvent(UserChangeLoginEmail(changeLoginEmailFlowStep = ChangeLoginEmailFlowStep.SUCCESSFUL_EMAIL_CHANGE))
                emit(EmailVerificationEvent.Success)
            }
        }
    }
        .flowOn(ioCoroutineDispatcher)
        .onCompletion { dataSync.markSyncAllowed() }
        .catch { throwable ->
            val (error, logErrorName) = when (throwable) {
                is TokenValidationFailedException,
                is IllegalStateException,
                is IllegalArgumentException,
                -> EmailVerificationError.InvalidToken to ChangeLoginError.WRONG_TOKEN
                is DashlaneApiException -> EmailVerificationError.Network to ChangeLoginError.GENERIC
                is LocalDataMigrationException,
                is DatabaseException,
                -> EmailVerificationError.InvalidSession to ChangeLoginError.GENERIC
                else -> EmailVerificationError.Generic to ChangeLoginError.GENERIC
            }
            logRepository.queueEvent(UserChangeLoginEmail(logErrorName, ChangeLoginEmailFlowStep.ERROR))
            emit(EmailVerificationEvent.Error(error = error))
        }
}

private suspend fun completeLoginChange(
    token: String,
    authorization: Authorization.User,
    userCompleteLoginChange: UserCompleteLoginChange,
) {
    val validationToken = UserCompleteLoginChange.Request.ValidationToken(token)
    val request = UserCompleteLoginChange.Request(validationToken)
    userCompleteLoginChange.execute(authorization, request)
}


@Suppress("kotlin:S107")
private suspend fun migrateLocalData(
    newSession: Session,
    previousUsername: Username,
    previousPin: String?,
    previousRsaPrivateKey: String?,
    newUsername: Username,
    preferencesManager: PreferencesManager,
    lockManager: LockManager,
    securityHelper: SecurityHelper,
    cryptoObjectHelper: CryptoObjectHelper,
    sessionCredentialsSaver: SessionCredentialsSaver,
    sessionTrasher: SessionTrasher,
    userSecureStorageManager: UserSecureStorageManager,
    globalPreferencesManager: GlobalPreferencesManager,
    accountCreationSetup: AccountCreationSetup,
) {
    try {
        
        preferencesManager.changeEmail(previousEmail = previousUsername, newEmail = newUsername)

        
        if (previousRsaPrivateKey != null) {
            userSecureStorageManager.storeRsaPrivateKey(
                localKey = newSession.localKey,
                username = newUsername,
                data = previousRsaPrivateKey,
            )
        }

        
        val hasBiometricLock = Biometric in lockManager.getLocks(newUsername)
        if (securityHelper.isDeviceSecured(newSession.username)) {
            sessionCredentialsSaver.saveCredentials(newSession)
            if (hasBiometricLock) {
                cryptoObjectHelper.deleteEncryptionKey(CryptoObjectHelper.BiometricsSeal(previousUsername.email))
                cryptoObjectHelper.createEncryptionKey(CryptoObjectHelper.BiometricsSeal(newUsername.email), true)
                lockManager.addLock(newUsername, Biometric)
            }
            if (previousPin != null) {
                userSecureStorageManager.storePin(newSession.localKey, newSession.username, previousPin)
                lockManager.addLock(newUsername, PinCode)
            }
        }
        
        sessionTrasher.trash(previousUsername, deletePreferences = true)
        
        lockManager.unlock(newSession, LockPass.ofPassword(newSession.appKey))
        
        accountCreationSetup.insertVaultEmail(newUsername.email)
    } catch (e: Exception) {
        clearLocalDataOnError(
            previousUsername = previousUsername,
            newUsername = newUsername,
            sessionTrasher = sessionTrasher,
            globalPreferencesManager = globalPreferencesManager,
        )
        throw LocalDataMigrationException(e)
    }
}

private suspend fun clearLocalDataOnError(
    previousUsername: Username,
    newUsername: Username,
    sessionTrasher: SessionTrasher,
    globalPreferencesManager: GlobalPreferencesManager,
) {
    globalPreferencesManager.setLastLoggedInUser(newUsername.email)
    sessionTrasher.trash(previousUsername, deletePreferences = true)
    sessionTrasher.trash(newUsername, deletePreferences = true)
}

internal class LocalDataMigrationException(cause: Exception) : Exception(cause)