package com.dashlane.sso.login

import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.authentication.AuthenticationExpiredVersionException
import com.dashlane.authentication.AuthenticationInvalidSsoException
import com.dashlane.authentication.AuthenticationNetworkException
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.AuthenticationUnknownException
import com.dashlane.authentication.create.AccountCreator
import com.dashlane.authentication.login.AuthenticationSsoRepository
import com.dashlane.authentication.login.AuthenticationSsoRepository.ValidateResult
import com.dashlane.authentication.sso.GetSsoInfoResult
import com.dashlane.data.login.LoginMode
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.Mode
import com.dashlane.hermes.generated.definitions.Status
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.hermes.generated.events.user.Login
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPass
import com.dashlane.lock.LockType
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionInitializer
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionResult
import com.dashlane.sso.login.RemoteLoginSsoActivity.Companion.KEY_IS_SSO_PROVIDER
import com.dashlane.sso.login.RemoteLoginSsoActivity.Companion.KEY_LOGIN
import com.dashlane.sso.login.RemoteLoginSsoActivity.Companion.KEY_MIGRATE_TO_MASTER_PASSWORD_USER
import com.dashlane.sso.login.RemoteLoginSsoActivity.Companion.KEY_SERVICE_PROVIDER_URL
import com.dashlane.sso.login.RemoteLoginSsoActivity.Result.Error.CannotOpenServiceProvider
import com.dashlane.sso.login.RemoteLoginSsoActivity.Result.Error.ExpiredVersion
import com.dashlane.sso.login.RemoteLoginSsoActivity.Result.Error.ForbiddenNavigation
import com.dashlane.sso.login.RemoteLoginSsoActivity.Result.Error.Network
import com.dashlane.sso.login.RemoteLoginSsoActivity.Result.Error.Offline
import com.dashlane.sso.login.RemoteLoginSsoActivity.Result.Error.SamlResponseNotFound
import com.dashlane.sso.login.RemoteLoginSsoActivity.Result.Error.Unknown
import com.dashlane.sync.DataSync
import com.dashlane.user.UserAccountInfo
import com.dashlane.user.UserSecuritySettings
import com.dashlane.user.Username
import com.dashlane.util.generateUniqueIdentifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemoteLoginSsoViewModel @Inject constructor(
    private val ssoRepository: AuthenticationSsoRepository,
    private val sessionInitializer: SessionInitializer,
    private val preferencesManager: PreferencesManager,
    private val lockManager: LockManager,
    private val sessionManager: SessionManager,
    private val userAccountStorage: UserAccountStorage,
    private val accountCreator: AccountCreator,
    private val dataSync: DataSync,
    private val accountStatusRepository: AccountStatusRepository,
    private val logRepository: LogRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _stateFlow = MutableViewStateFlow<RemoteLoginSsoState.View, RemoteLoginSsoState.SideEffect>(RemoteLoginSsoState.View())
    val stateFlow: ViewStateFlow<RemoteLoginSsoState.View, RemoteLoginSsoState.SideEffect> = _stateFlow

    fun viewStarted() {
        viewModelScope.launch {
            if (_stateFlow.value.loginSsoInfo != null) return@launch

            val login: String? = savedStateHandle[KEY_LOGIN]
            val serviceProviderUrl: String? = savedStateHandle[KEY_SERVICE_PROVIDER_URL]
            val isNitroProvider: Boolean = savedStateHandle[KEY_IS_SSO_PROVIDER] ?: false
            val migrateToMasterPasswordUser: Boolean = savedStateHandle[KEY_MIGRATE_TO_MASTER_PASSWORD_USER] ?: false

            if (login == null || serviceProviderUrl == null) {
                _stateFlow.send(RemoteLoginSsoState.SideEffect.Cancel)
                return@launch
            }

            val loginSsoInfo = LoginSsoInfo(
                email = login,
                serviceProviderUrl = serviceProviderUrl,
                isNitroProvider = isNitroProvider,
            )

            _stateFlow.update { state ->
                state.copy(
                    loginSsoInfo = loginSsoInfo,
                    migrateToMasterPasswordUser = migrateToMasterPasswordUser,
                )
            }
            _stateFlow.send(RemoteLoginSsoState.SideEffect.StartSso(loginSsoInfo))
        }
    }

    fun ssoComplete(getSsoInfoResult: GetSsoInfoResult?) {
        flow {
            if (getSsoInfoResult == null) {
                emit(RemoteLoginSsoState.SideEffect.Cancel)
                return@flow
            }

            if (getSsoInfoResult !is GetSsoInfoResult.Success) {
                finishWithSsoError(getSsoInfoResult)
                return@flow
            }

            val email = _stateFlow.value.loginSsoInfo?.email ?: return@flow
            val userSsoInfo = getSsoInfoResult.userSsoInfo

            if (email != getSsoInfoResult.login) {
                emit(RemoteLoginSsoState.SideEffect.FinishWithResult(RemoteLoginSsoActivity.Result.Error.InvalidSso))
                return@flow
            }

            if (userSsoInfo.exists) {
                userAccountStorage.saveSecuritySettings(email, UserSecuritySettings(isSso = true))

                val result = ssoRepository.validate(
                    login = email,
                    ssoToken = userSsoInfo.ssoToken,
                    serviceProviderKey = userSsoInfo.key,
                )

                when (result) {
                    is ValidateResult.Remote -> handleValidateRemoteSuccess(result)
                    is ValidateResult.Local -> handleValidateLocalSuccess(result)
                }
            } else {
                emit(_stateFlow.value.copy(userSsoInfo = userSsoInfo, termsOfServiceShown = true))
            }
        }
            .catch { exception ->
                val result = parseException(exception)
                emit(RemoteLoginSsoState.SideEffect.FinishWithResult(result))
            }
            .onEach { state ->
                when (state) {
                    is RemoteLoginSsoState.View -> _stateFlow.update { state }
                    is RemoteLoginSsoState.SideEffect -> _stateFlow.send(state)
                }
            }
            .launchIn(viewModelScope)
    }

    fun bottomSheetDismissed() {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(termsOfServiceShown = false) }
            _stateFlow.send(RemoteLoginSsoState.SideEffect.Cancel)
        }
    }

    fun onHelpClicked(uri: Uri) {
        viewModelScope.launch {
            _stateFlow.send(RemoteLoginSsoState.SideEffect.OpenHelp(uri))
        }
    }

    fun tosCheckedChanged(checked: Boolean) {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(termsOfServicesAccepted = checked) }
        }
    }

    fun offersCheckedChanged(checked: Boolean) {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(privacyPolicyAccepted = checked) }
        }
    }

    fun createAccount() {
        flow {
            emit(_stateFlow.value.copy(termsOfServiceShown = false))

            val userSsoInfo = _stateFlow.value.userSsoInfo ?: return@flow
            userAccountStorage.saveSecuritySettings(userSsoInfo.login, UserSecuritySettings(isSso = true))

            accountCreator.createAccountSso(
                username = userSsoInfo.login,
                ssoToken = userSsoInfo.ssoToken,
                serviceProviderKey = userSsoInfo.key,
                termsState = AccountCreator.TermsState(
                    conditions = _stateFlow.value.termsOfServicesAccepted,
                    offers = _stateFlow.value.privacyPolicyAccepted,
                ),
            )

            emit(RemoteLoginSsoState.SideEffect.GoToOnboardingLock)
        }
            .catch { exception ->
                val result = parseException(exception)
                emit(RemoteLoginSsoState.SideEffect.FinishWithResult(result))
            }
            .onEach { state ->
                when (state) {
                    is RemoteLoginSsoState.View -> _stateFlow.update { state }
                    is RemoteLoginSsoState.SideEffect -> _stateFlow.send(state)
                }
            }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    suspend fun handleValidateRemoteSuccess(result: ValidateResult.Remote) {
        val login = result.login
        val settings = result.settings.run {
            if (anonymousUserId == null) {
                
                copy { anonymousUserId = generateUniqueIdentifier() }
            } else {
                this
            }
        }

        val sessionResult = sessionInitializer.createSession(
            username = Username.ofEmail(login),
            accessKey = result.accessKey,
            secretKey = result.secretKey,
            localKey = result.localKey,
            appKey = result.ssoKey,
            remoteKey = result.remoteKey,
            userSettings = settings,
            sharingPublicKey = result.sharingKeys?.public?.value,
            sharingPrivateKey = result.sharingKeys?.private?.value,
            userAnalyticsId = result.userAnalyticsId,
            deviceAnalyticsId = result.deviceAnalyticsId,
            loginMode = LoginMode.Sso,
            accountType = UserAccountInfo.AccountType.MasterPassword,
        )

        preferencesManager[result.login].userSettingsBackupTimeMillis = result.settingsDate.toEpochMilli()

        when (sessionResult) {
            is SessionResult.Error -> finishWithError(sessionResult)
            is SessionResult.Success -> {
                runCatching { accountStatusRepository.refreshFor(sessionResult.session) }

                lockManager.unlock(session = sessionResult.session, pass = LockPass.ofPassword(result.ssoKey))

                val locks = lockManager.getLocks(Username.ofEmail(login))
                val shouldLaunchInitialSync = preferencesManager[login].getInt(ConstantsPrefs.TIMESTAMP_LABEL, 0) == 0
                val migrateToMasterPasswordUser = _stateFlow.value.migrateToMasterPasswordUser
                val hasNoPinOrBiometric = LockType.PinCode !in locks && LockType.Biometric !in locks

                val sideEffect = when {
                    migrateToMasterPasswordUser -> {
                        RemoteLoginSsoState.SideEffect.GoToMigrationToMasterPasswordUser(
                            authTicket = result.authTicket,
                            shouldLaunchInitialSync = shouldLaunchInitialSync,
                            hasPinOrBiometric = hasNoPinOrBiometric,
                        )
                    }
                    hasNoPinOrBiometric -> RemoteLoginSsoState.SideEffect.GoToOnboardingLock
                    else -> RemoteLoginSsoState.SideEffect.GoToInitialSync
                }
                _stateFlow.send(sideEffect)
            }
        }
    }

    @VisibleForTesting
    suspend fun handleValidateLocalSuccess(result: ValidateResult.Local) {
        val login = result.login
        val sessionResult = sessionManager.loadSession(
            username = Username.ofEmail(login),
            appKey = result.ssoKey,
            secretKey = result.secretKey,
            localKey = result.localKey,
            loginMode = LoginMode.Sso,
        )

        when (sessionResult) {
            is SessionResult.Error -> finishWithError(sessionResult)
            is SessionResult.Success -> {
                runCatching { accountStatusRepository.refreshFor(sessionResult.session) }

                val shouldLaunchInitialSync = preferencesManager[login].getInt(ConstantsPrefs.TIMESTAMP_LABEL, 0) == 0
                val migrateToMasterPasswordUser = _stateFlow.value.migrateToMasterPasswordUser

                lockManager.unlock(session = sessionResult.session, pass = LockPass.ofPassword(result.ssoKey))

                val sideEffect = when {
                    migrateToMasterPasswordUser -> {
                        val locks = lockManager.getLocks(Username.ofEmail(login))
                        RemoteLoginSsoState.SideEffect.GoToMigrationToMasterPasswordUser(
                            authTicket = result.authTicket,
                            shouldLaunchInitialSync = shouldLaunchInitialSync,
                            hasPinOrBiometric = LockType.PinCode !in locks && LockType.Biometric !in locks,
                        )
                    }
                    shouldLaunchInitialSync -> RemoteLoginSsoState.SideEffect.GoToInitialSync
                    else -> {
                        dataSync.sync(Trigger.LOGIN)
                        RemoteLoginSsoState.SideEffect.GoToHome
                    }
                }
                _stateFlow.send(sideEffect)
            }
        }
    }

    private suspend fun finishWithError(sessionResult: SessionResult.Error) {
        logRepository.queueEvent(Login(mode = Mode.SSO, isBackupCode = false, status = Status.ERROR_UNKNOWN))
            message = "Failed to load session ${sessionResult.errorCode} ${sessionResult.errorReason}",
            throwable = sessionResult.cause,
        )
        _stateFlow.send(RemoteLoginSsoState.SideEffect.FinishWithResult(Unknown))
    }

    private suspend fun finishWithSsoError(ssoResult: GetSsoInfoResult) {
        logRepository.queueEvent(Login(mode = Mode.SSO, isBackupCode = false, status = Status.ERROR_INVALID_SSO))
        val error = when (ssoResult) {
            is GetSsoInfoResult.Error.SamlResponseNotFound -> SamlResponseNotFound
            is GetSsoInfoResult.Error.ForbiddenNavigation -> ForbiddenNavigation
            is GetSsoInfoResult.Error.CannotOpenServiceProvider -> CannotOpenServiceProvider
            else -> Unknown
        }
        _stateFlow.send(RemoteLoginSsoState.SideEffect.FinishWithResult(error))
    }

    private fun parseException(exception: Throwable): RemoteLoginSsoActivity.Result {
        return when (exception) {
            is AuthenticationInvalidSsoException -> {
                logRepository.queueEvent(Login(mode = Mode.SSO, isBackupCode = false, status = Status.ERROR_INVALID_SSO))
                RemoteLoginSsoActivity.Result.Error.InvalidSso
            }
            is AuthenticationOfflineException -> Offline
            is AuthenticationNetworkException -> Network
            is AuthenticationExpiredVersionException -> ExpiredVersion
            is AuthenticationUnknownException,
            -> {
                logRepository.queueEvent(Login(mode = Mode.SSO, isBackupCode = false, status = Status.ERROR_UNKNOWN))
                Unknown
            }
            else -> Unknown
        }
    }
}