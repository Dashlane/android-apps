package com.dashlane.login.pages.email.compose

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.user.UserAccountInfo
import com.dashlane.account.UserAccountStorage
import com.dashlane.user.UserSecuritySettings
import com.dashlane.authentication.AuthenticationAccountNotFoundException
import com.dashlane.authentication.AuthenticationContactSsoAdministratorException
import com.dashlane.authentication.AuthenticationDeactivatedUserException
import com.dashlane.authentication.AuthenticationEmptyEmailException
import com.dashlane.authentication.AuthenticationExpiredVersionException
import com.dashlane.authentication.AuthenticationInvalidEmailException
import com.dashlane.authentication.AuthenticationNetworkException
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.AuthenticationTeamException
import com.dashlane.authentication.CipheredBackupToken
import com.dashlane.authentication.UnauthenticatedUser
import com.dashlane.authentication.login.AuthenticationEmailRepository
import com.dashlane.authentication.login.AuthenticationEmailRepository.Result
import com.dashlane.authentication.login.AuthenticationEmailRepository.Result.RequiresDeviceRegistration
import com.dashlane.authentication.login.AuthenticationEmailRepository.Result.RequiresPassword
import com.dashlane.authentication.login.AuthenticationEmailRepository.Result.RequiresServerKey
import com.dashlane.authentication.login.AuthenticationEmailRepository.Result.RequiresSso
import com.dashlane.common.logger.usersupportlogger.UserSupportFileUploadState
import com.dashlane.common.logger.usersupportlogger.UserSupportFileUploader
import com.dashlane.crashreport.CrashReporter
import com.dashlane.login.LoginLogger
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferNavigation
import com.dashlane.login.root.LoginDestination
import com.dashlane.login.sso.LoginSsoActivity
import com.dashlane.mvvm.State
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.server.api.endpoints.AccountType
import com.dashlane.server.api.endpoints.account.AccountExistsService
import com.dashlane.util.clipboard.ClipboardCopy
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.util.isNotSemanticallyNull
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting

private const val DIAGNOSTIC_KEY = "diagnostic"
private const val DEBUG_KEY = "debug"

@HiltViewModel
class LoginEmailViewModel @Inject constructor(
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val userAccountStorage: UserAccountStorage,
    private val authenticationEmailRepository: AuthenticationEmailRepository,
    private val logger: LoginLogger,
    private val accountExistsService: AccountExistsService,
    private val userSupportFileUploader: UserSupportFileUploader,
    private val crashReporter: CrashReporter,
    private val clipboardCopy: ClipboardCopy,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val email: String? = savedStateHandle[LoginDestination.LOGIN_KEY]
    private val allowSkipEmail: Boolean = savedStateHandle[LoginDestination.ALLOW_SKIP_EMAIL_KEY] ?: false

    private val stateFlow = MutableStateFlow(LoginEmailState())
    private val navigationStateFlow = Channel<LoginEmailNavigationState>()

    val uiState = stateFlow.asStateFlow()
    val navigationState = navigationStateFlow.receiveAsFlow()

    fun viewStarted() {
        viewModelScope.launch {
            if (stateFlow.value.email.isNotSemanticallyNull()) return@launch

            val preferredEmail = email ?: globalPreferencesManager.getUserListHistory().firstOrNull()
            stateFlow.update { state -> state.copy(email = preferredEmail) }

            if (!globalPreferencesManager.isUserLoggedOut && !preferredEmail.isNullOrEmpty() && allowSkipEmail) {
                delay(500) 
                checkEmail(preferredEmail)
            }
        }
    }

    fun emailChanged(email: String) {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(email = email.filterNot { it.isWhitespace() }, error = null) }
        }
    }

    fun onContinue() {
        checkEmail(stateFlow.value.email)
    }

    fun createAccount() {
        viewModelScope.launch {
            navigationStateFlow.send(LoginEmailNavigationState.GoToCreateAccount(email = stateFlow.value.email, skipIfPrefilled = false))
        }
    }

    fun diagnosticConfirmed() {
        uploadUserSupportFile()
    }

    fun diagnosticCopy() {
        viewModelScope.launch {
            uiState.value.crashDeviceId?.let { clipboardCopy.copyToClipboard(data = it, sensitiveData = true) }
            stateFlow.update { state -> state.copy(showDebugSuccessDialog = false) }
        }
    }

    fun diagnosticCancelled() {
        viewModelScope.launch {
            stateFlow.update { state ->
                state.copy(
                    showDebugConfirmationDialog = false,
                    showDebugUploadingDialog = false,
                    showDebugSuccessDialog = false,
                    showDebugFailedDialog = false
                )
            }
        }
    }

    fun ssoErrorDialogCancelled() {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(isSSOAdminDialogShown = false) }
        }
    }

    fun qrCode() {
        viewModelScope.launch {
            navigationStateFlow.send(
                LoginEmailNavigationState.GoToSecretTransfer(
                    email = null,
                    destination = LoginSecretTransferNavigation.qrCodeDestination
                )
            )
        }
    }

    fun ssoComplete(result: LoginSsoActivity.Result) {
        viewModelScope.launch {
            when (result) {
                LoginSsoActivity.Result.Success -> navigationStateFlow.send(LoginEmailNavigationState.SSOSuccess)
                is LoginSsoActivity.Result.Error -> stateFlow.update { state -> state.copy(error = LoginEmailError.SSO) }
            }
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    fun checkEmail(email: String?) {
        flow<State> {
            if (email.isNullOrEmpty()) {
                emit(stateFlow.value.copy(error = LoginEmailError.InvalidEmail, isLoading = false))
                return@flow
            }

            if (DIAGNOSTIC_KEY == email || DEBUG_KEY == email) {
                emit(stateFlow.value.copy(showDebugConfirmationDialog = true, email = "", isLoading = false))
                return@flow
            }

            val backupToken = globalPreferencesManager.getCipheredBackupToken(email)
            val user = if (backupToken != null) {
                UnauthenticatedUser(
                    email = email,
                    cipheredBackupToken = CipheredBackupToken(backupToken, globalPreferencesManager.getBackupTokenDate(email))
                )
            } else {
                UnauthenticatedUser(email)
            }
            val userStatus = try {
                authenticationEmailRepository.getUserStatus(user)
            } catch (e: AuthenticationAccountNotFoundException) {
                if (checkIfAccountIsSSO(email)) {
                    emit(LoginEmailNavigationState.GoToCreateAccount(email, skipIfPrefilled = true))
                } else {
                    emit(stateFlow.value.copy(isLoading = false, error = LoginEmailError.NoAccount))
                }
                return@flow
            }

            val state = handleUserStatus(userStatus)
            emit(state)
        }
            .catch { emit(handleErrors(it)) }
            .onStart { emit(stateFlow.value.copy(isLoading = true, error = null)) }
            .onEach { state ->
                when (state) {
                    is LoginEmailState -> stateFlow.update { state }
                    is LoginEmailNavigationState -> {
                        stateFlow.update { it.copy(isLoading = false) }
                        navigationStateFlow.send(state)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    fun uploadUserSupportFile() {
        userSupportFileUploader.startCrashLogsUpload(crashDeviceId = crashReporter.crashReporterId)
            .flowOn(ioDispatcher)
            .map { state ->
                when (state) {
                    UserSupportFileUploadState.Failed -> stateFlow.value.copy(
                        showDebugConfirmationDialog = false,
                        showDebugUploadingDialog = false,
                        showDebugFailedDialog = true
                    )
                    is UserSupportFileUploadState.Finished -> stateFlow.value.copy(
                        crashDeviceId = state.crashDeviceId,
                        showDebugConfirmationDialog = false,
                        showDebugUploadingDialog = false,
                        showDebugSuccessDialog = true
                    )
                    UserSupportFileUploadState.Uploading -> stateFlow.value.copy(showDebugConfirmationDialog = false, showDebugUploadingDialog = true)
                }
            }
            .catch { emit(stateFlow.value.copy(showDebugConfirmationDialog = false, showDebugUploadingDialog = false, showDebugFailedDialog = true)) }
            .onEach { state ->
                stateFlow.update { state }
            }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    fun handleUserStatus(userStatus: Result): State {
        return when (userStatus) {
            is RequiresDeviceRegistration -> {
                when (userStatus) {
                    is RequiresDeviceRegistration.SecondFactor -> handleUserRequiresDeviceRegistrationSecondFactor(userStatus)
                    is RequiresDeviceRegistration.Sso -> LoginEmailNavigationState.GoToSSO(userStatus.login, userStatus.ssoInfo)
                }
            }
            is RequiresServerKey -> handleUserStatusRequiresServerKey(userStatus)
            is RequiresPassword -> LoginEmailNavigationState.GoToPassword(userStatus.registeredUserDevice, userStatus.ssoInfo)
            is RequiresSso -> LoginEmailNavigationState.GoToSSO(userStatus.login, userStatus.ssoInfo)
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    fun handleUserRequiresDeviceRegistrationSecondFactor(userStatus: RequiresDeviceRegistration.SecondFactor): State {
        val secondFactor = userStatus.secondFactor
        val login = secondFactor.login
        return when (secondFactor) {
            is AuthenticationSecondFactor.EmailToken -> {
                val securitySettings = UserSecuritySettings(
                    isToken = true,
                    isAuthenticatorEnabled = secondFactor.isAuthenticatorEnabled
                )

                val accountType = when (userStatus.accountType) {
                    AccountType.MASTERPASSWORD -> UserAccountInfo.AccountType.MasterPassword
                    AccountType.INVISIBLEMASTERPASSWORD -> UserAccountInfo.AccountType.InvisibleMasterPassword
                }

                userAccountStorage.saveAccountType(username = login, accountType = accountType)
                userAccountStorage.saveSecuritySettings(username = login, securitySettings = securitySettings)
                if (accountType == UserAccountInfo.AccountType.InvisibleMasterPassword) {
                    LoginEmailNavigationState.GoToSecretTransfer(email = login, destination = LoginSecretTransferNavigation.chooseTypeDestination)
                } else {
                    LoginEmailNavigationState.GoToToken(secondFactor = secondFactor, userStatus.ssoInfo)
                }
            }

            is AuthenticationSecondFactor.Totp -> {
                val securitySettings = UserSecuritySettings(
                    isTotp = true,
                    isU2fEnabled = secondFactor.u2f != null,
                    isDuoEnabled = secondFactor.duoPush != null,
                    isAuthenticatorEnabled = secondFactor.isAuthenticatorEnabled
                )
                userAccountStorage.saveSecuritySettings(login, securitySettings)
                LoginEmailNavigationState.GoToOTP(secondFactor = secondFactor, userStatus.ssoInfo)
            }
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    fun handleUserStatusRequiresServerKey(userStatus: RequiresServerKey): State {
        val secondFactor = userStatus.secondFactor
        val login = secondFactor.login
        val securitySettings = UserSecuritySettings(
            isTotp = true,
            isOtp2 = true,
            isU2fEnabled = secondFactor.u2f != null,
            isDuoEnabled = secondFactor.duoPush != null
        )
        userAccountStorage.saveSecuritySettings(login, securitySettings)
        return if (secondFactor.isAuthenticatorEnabled) {
            LoginEmailNavigationState.GoToAuthenticator(secondFactor = secondFactor, userStatus.ssoInfo)
        } else {
            LoginEmailNavigationState.GoToOTP(secondFactor = secondFactor, userStatus.ssoInfo)
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    fun handleErrors(e: Throwable): State {
        val state = stateFlow.value
        return when (e) {
            is AuthenticationAccountNotFoundException -> state.copy(isLoading = false, error = LoginEmailError.NoAccount)
            is AuthenticationEmptyEmailException,
            is IllegalArgumentException,
            is AuthenticationInvalidEmailException -> {
                logger.logWrongEmail()
                state.copy(isLoading = false, error = LoginEmailError.InvalidEmail)
            }
            is AuthenticationContactSsoAdministratorException -> state.copy(isLoading = false, isSSOAdminDialogShown = true)
            is AuthenticationOfflineException -> state.copy(isLoading = false, error = LoginEmailError.Offline)
            is AuthenticationNetworkException -> state.copy(isLoading = false, error = LoginEmailError.Network)
            is AuthenticationExpiredVersionException -> LoginEmailNavigationState.EndOfLife
            is AuthenticationTeamException -> state.copy(isLoading = false, error = LoginEmailError.Team)
            is AuthenticationDeactivatedUserException -> state.copy(
                isLoading = false,
                error = LoginEmailError.UserDeactivated
            )
            else -> state.copy(isLoading = false, error = LoginEmailError.Generic)
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun checkIfAccountIsSSO(email: String): Boolean {
        val login = AccountExistsService.Request.Login(email.lowercase(Locale.US))
        val response = accountExistsService.execute(AccountExistsService.Request(login = login))
        return response.data.sso
    }
}
