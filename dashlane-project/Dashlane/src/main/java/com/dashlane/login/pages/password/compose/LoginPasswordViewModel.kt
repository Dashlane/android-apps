package com.dashlane.login.pages.password.compose

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.accountrecoverykey.AccountRecoveryState
import com.dashlane.authentication.AuthenticationDeviceCredentialsInvalidException
import com.dashlane.authentication.AuthenticationEmptyPasswordException
import com.dashlane.authentication.AuthenticationInvalidPasswordException
import com.dashlane.authentication.DataLossTrackingLogger
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.login.AuthenticationPasswordRepository
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.crypto.keys.AppKey
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.debug.services.DaDaDaLogin
import com.dashlane.hardwaresecurity.CryptoObjectHelper
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPass
import com.dashlane.lock.LockSetting
import com.dashlane.lock.LockType
import com.dashlane.login.LoginLogger
import com.dashlane.login.LoginMode
import com.dashlane.login.LoginStrategy
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryKeyRepository
import com.dashlane.login.pages.password.LoginPasswordRepository
import com.dashlane.login.pages.password.toVerification
import com.dashlane.login.root.LoginRepository
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionRestorer
import com.dashlane.session.SessionTrasher
import com.dashlane.user.UserAccountInfo
import com.dashlane.user.Username
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting

@OptIn(FlowPreview::class)
@HiltViewModel
class LoginPasswordViewModel @Inject constructor(
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val sessionManager: SessionManager,
    private val sessionRestorer: SessionRestorer,
    private val sessionTrasher: SessionTrasher,
    private val passwordRepository: AuthenticationPasswordRepository,
    private val loginPasswordRepository: LoginPasswordRepository,
    private val biometricRecovery: BiometricRecovery,
    private val cryptoObjectHelper: CryptoObjectHelper,
    private val loginAccountRecoveryKeyRepository: LoginAccountRecoveryKeyRepository,
    private val lockManager: LockManager,
    private val preferencesManager: PreferencesManager,
    private val loginRepository: LoginRepository,
    private val loginLogger: LoginLogger,
    private val dadadaLogin: DaDaDaLogin
) : ViewModel() {

    private val _stateFlow = MutableViewStateFlow<LoginPasswordState.View, LoginPasswordState.SideEffect>(LoginPasswordState.View())
    val stateFlow: ViewStateFlow<LoginPasswordState.View, LoginPasswordState.SideEffect> = _stateFlow

    fun viewStarted(lockSetting: LockSetting) {
        viewModelScope.launch {
            if (_stateFlow.value.password.text.isNotEmpty()) return@launch

            val registeredUserDevice = loginRepository.getRegisteredUserDevice() ?: run {
                globalPreferencesManager.isUserLoggedOut = true
                _stateFlow.send(LoginPasswordState.SideEffect.Logout(null, null))
                return@launch
            }

            val email = registeredUserDevice.login

            val canSwitch = lockSetting.unlockReason.let {
                it == null || it is LockEvent.Unlock.Reason.AppAccess || it is LockEvent.Unlock.Reason.AccessFromExternalComponent
            }

            val loginHistory = if (canSwitch) {
                listOf(email) + (globalPreferencesManager.getUserListHistory() - email).filter { it != "" }
            } else {
                emptyList()
            }

            
            val prefillPassword = if (dadadaLogin.isEnabled) dadadaLogin.defaultPassword ?: "" else ""

            _stateFlow.update { state ->
                state.copy(
                    email = email,
                    lockSetting = lockSetting,
                    loginHistory = loginHistory,
                    password = state.password.copy(text = prefillPassword, selection = TextRange(prefillPassword.length)),
                )
            }

            _stateFlow.update { state ->
                state.copy(
                    isBiometricRecoveryEnabled = isBiometricRecoveryEnabled(registeredUserDevice),
                    isARKEnabled = getAccountRecoveryKeyState(registeredUserDevice)
                )
            }
        }
    }

    fun passwordChanged(password: TextFieldValue) {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(password = password, error = null) }
        }
    }

    fun changeAccount(email: String?) {
        viewModelScope.launch {
            _stateFlow.send(LoginPasswordState.SideEffect.ChangeAccount(email = email ?: ""))
        }
    }

    fun cancel() {
        viewModelScope.launch {
            val lockSetting = _stateFlow.value.lockSetting ?: return@launch
            when {
                lockSetting.isShowMPForRemember -> {
                    preferencesManager[_stateFlow.value.email].credentialsSaveDate = Instant.now()
                    _stateFlow.send(LoginPasswordState.SideEffect.Fallback)
                }
                lockSetting.isLockCancelable -> _stateFlow.send(LoginPasswordState.SideEffect.Cancel)
            }
        }
    }

    fun arkComplete(password: String?) {
        viewModelScope.launch {
            password?.let {
                _stateFlow.update { state -> state.copy(password = state.password.copy(password)) }
                login()
            }
        }
    }

    @Suppress("LongMethod")
    fun login() {
        flow<LoginPasswordState> {
            val registeredUserDevice = loginRepository.getRegisteredUserDevice() ?: return@flow
            val password = _stateFlow.value.password.text
            if (password.isEmpty()) throw AuthenticationEmptyPasswordException()

            sessionManager.session
                ?.takeIf { it.userId == registeredUserDevice.login }
                ?.let {
                    val appKey = AppKey.Password(password, registeredUserDevice.serverKey)
                    if (lockManager.unlock(session = it, pass = LockPass.ofPassword(appKey))) {
                        sendUnlockMP(_stateFlow.value.lockSetting?.unlockReason)

                        emit(
                            LoginPasswordState.SideEffect.LoginSuccess(
                                strategy = LoginStrategy.Strategy.Unlock,
                                ssoInfo = loginRepository.getSsoInfo()
                            )
                        )
                        return@flow
                    } else {
                        lockManager.addFailUnlockAttempt()
                        throw AuthenticationInvalidPasswordException()
                    }
                }

            val result = passwordRepository.validate(registeredUserDevice, password.encodeUtf8ToObfuscated())

            val (session, strategy) = when (result) {
                is AuthenticationPasswordRepository.Result.Local -> {
                    val session = loginPasswordRepository.createSessionForLocalPassword(registeredUserDevice = registeredUserDevice, result = result)
                    val strategy = loginPasswordRepository.getLocalStrategy(session = session)
                    session to strategy
                }
                is AuthenticationPasswordRepository.Result.Remote -> {
                    val session = loginPasswordRepository.createSessionForRemotePassword(
                        result = result,
                        accountType = UserAccountInfo.AccountType.MasterPassword
                    )
                    val strategy = loginPasswordRepository.getRemoteStrategy(session = session, securityFeatures = result.securityFeatures)
                    session to strategy
                }
            }

            val unlocked = lockManager.unlock(session = session, pass = LockPass.ofPassword(appKey = result.password))
            if (!unlocked) throw AuthenticationInvalidPasswordException()

            sendUnlockMP(_stateFlow.value.lockSetting?.unlockReason)

            emit(LoginPasswordState.SideEffect.LoginSuccess(strategy, loginRepository.getSsoInfo()))
        }
            .catch { e ->
                emit(handleErrors(e = e))
            }
            .onStart { emit(_stateFlow.value.copy(isLoading = true)) }
            .onEach { state ->
                when (state) {
                    is LoginPasswordState.View -> _stateFlow.update { state }
                    is LoginPasswordState.SideEffect -> _stateFlow.send(state)
                }
            }
            .launchIn(viewModelScope)
    }

    fun forgot() {
        viewModelScope.launch {
            val registeredUserDevice = loginRepository.getRegisteredUserDevice() ?: return@launch
            val hasBiometricReset = isBiometricRecoveryEnabled(registeredUserDevice)
            val hasAccountRecoveryKeyEnabled = getAccountRecoveryKeyState(registeredUserDevice)

            when {
                hasBiometricReset && hasAccountRecoveryKeyEnabled -> _stateFlow.update { state -> state.copy(recoveryDialogShown = true) }
                hasBiometricReset -> _stateFlow.send(LoginPasswordState.SideEffect.GoToBiometricRecovery)
                hasAccountRecoveryKeyEnabled -> {
                    val authTicket = loginRepository.getAuthTicket()
                    val state = LoginPasswordState.SideEffect.GoToARK(registeredUserDevice = registeredUserDevice, authTicket = authTicket)
                    _stateFlow.send(state)
                }
                else -> _stateFlow.update { state -> state.copy(helpDialogShown = true) }
            }
        }
    }

    fun bottomSheetDismissed() {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(helpDialogShown = false, recoveryDialogShown = false) }
        }
    }

    fun biometricRecovery() {
        viewModelScope.launch {
            val registeredUserDevice = loginRepository.getRegisteredUserDevice() ?: return@launch
            sessionRestorer.restoreSession(
                Username.ofEmail(registeredUserDevice.login),
                registeredUserDevice.serverKey,
                acceptLoggedOut = true
            )
            preferencesManager[registeredUserDevice.login].isMpResetRecoveryStarted = true
            _stateFlow.send(LoginPasswordState.SideEffect.GoToBiometricRecovery)
            _stateFlow.update { state -> state.copy(recoveryDialogShown = false, helpDialogShown = false) }
        }
    }

    fun ark() {
        viewModelScope.launch {
            val registeredUserDevice = loginRepository.getRegisteredUserDevice() ?: return@launch
            _stateFlow.send(
                LoginPasswordState.SideEffect.GoToARK(
                    registeredUserDevice = registeredUserDevice,
                    authTicket = loginRepository.getAuthTicket()
                )
            )
            _stateFlow.update { state -> state.copy(recoveryDialogShown = false, helpDialogShown = false) }
        }
    }

    fun cannotLogin() {
        viewModelScope.launch {
            _stateFlow.send(LoginPasswordState.SideEffect.GoToCannotLoginHelp)
            _stateFlow.update { state -> state.copy(helpDialogShown = false) }
        }
    }

    fun forgotMP() {
        viewModelScope.launch {
            _stateFlow.send(LoginPasswordState.SideEffect.GoToForgotMPHelp)
            _stateFlow.update { state -> state.copy(helpDialogShown = false) }
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    fun isBiometricRecoveryEnabled(registeredUserDevice: RegisteredUserDevice): Boolean {
        return _stateFlow.value.isBiometricRecoveryEnabled ?: run {
            val email = registeredUserDevice.login
            biometricRecovery.isSetUpForUser(email) &&
                sessionRestorer.canRestoreSession(user = email, serverKey = registeredUserDevice.serverKey, acceptLoggedOut = false) &&
                
                cryptoObjectHelper.getEncryptCipher(CryptoObjectHelper.BiometricsSeal(email)) is CryptoObjectHelper.CipherInitResult.Success
        }
    }

    @VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun getAccountRecoveryKeyState(registeredUserDevice: RegisteredUserDevice): Boolean {
        return _stateFlow.value.isARKEnabled ?: run {
            loginAccountRecoveryKeyRepository.getAccountRecoveryStatus(registeredUserDevice)
                .getOrElse { throwable ->
                    AccountRecoveryState.Success(false, enabled = false)
                }
                .enabled
        }
    }

    private suspend fun handleErrors(e: Throwable): LoginPasswordState {
        val state = _stateFlow.value
        val registeredUserDevice = loginRepository.getRegisteredUserDevice() ?: return state.copy(error = LoginPasswordError.Generic)
        return when (e) {
            is AuthenticationEmptyPasswordException -> {
                loginLogger.logWrongPassword(registeredUserDevice.toVerification())
                state.copy(isLoading = false, error = LoginPasswordError.EmptyPassword)
            }
            is AuthenticationInvalidPasswordException -> {
                loginLogger.logWrongPassword(registeredUserDevice.toVerification())
                if (lockManager.hasFailedUnlockTooManyTimes()) {
                    sessionManager.session?.let { sessionManager.destroySession(it, true) }
                    LoginPasswordState.SideEffect.Logout(email = registeredUserDevice.login, error = LoginPasswordError.TooManyInvalidPassword)
                } else {
                    state.copy(isLoading = false, error = LoginPasswordError.InvalidPassword)
                }
            }
            is AuthenticationDeviceCredentialsInvalidException -> {
                val reason = when {
                    e.isDataCorruption -> DataLossTrackingLogger.Reason.ACCESS_KEY_UNKNOWN
                    e.isValidPassword -> DataLossTrackingLogger.Reason.PASSWORD_OK_UKI_INVALID
                    else -> DataLossTrackingLogger.Reason.PASSWORD_CHANGED
                }
                val username = Username.ofEmail(registeredUserDevice.login)
                sessionTrasher.trash(username = username, deletePreferences = true)
                globalPreferencesManager.setLastLoggedInUser("")
                LoginPasswordState.SideEffect.Logout(email = registeredUserDevice.login, error = LoginPasswordError.InvalidCredentials)
            }
            else -> {
                loginLogger.logErrorUnknown(loginMode = LoginMode.MasterPassword(registeredUserDevice.toVerification()))
                state.copy(isLoading = false, error = LoginPasswordError.Generic)
            }
        }
    }

    @androidx.annotation.VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun sendUnlockMP(reason: LockEvent.Unlock.Reason?) {
        lockManager.hasEnteredMP = true
        
        runCatching {
            lockManager.sendUnlockEvent(
                LockEvent.Unlock(
                    reason = reason ?: LockEvent.Unlock.Reason.AppAccess,
                    lockType = LockType.MasterPassword
                )
            )
        }
    }
}