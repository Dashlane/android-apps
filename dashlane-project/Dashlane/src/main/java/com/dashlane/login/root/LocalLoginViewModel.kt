package com.dashlane.login.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.user.UserAccountInfo
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.events.AppEvents
import com.dashlane.events.clearLastEvent
import com.dashlane.lock.UnlockEvent
import com.dashlane.login.LoginStrategy
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.toSecurityFeatures
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionRestorer
import com.dashlane.user.Username
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LocalLoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val lockManager: LockManager,
    private val sessionManager: SessionManager,
    private val sessionRestorer: SessionRestorer,
    private val lockTypeManager: LockTypeManager,
    private val appEvents: AppEvents,
) : ViewModel() {

    private val stateFlow = MutableStateFlow(LocalLoginState())
    private val navigationStateFlow = Channel<LocalLoginNavigationState>()

    val uiState = stateFlow.asStateFlow()
    val navigationState = navigationStateFlow.receiveAsFlow()

    fun localLoginStarted(userAccountInfo: UserAccountInfo, lockSetting: LockSetting) {
        viewModelScope.launch {
            val registeredUserDevice = RegisteredUserDevice.Local(
                login = userAccountInfo.username,
                securityFeatures = userAccountInfo.securitySettings.toSecurityFeatures(),
                accessKey = userAccountInfo.accessKey
            )
            loginRepository.updateRegisteredUserDevice(registeredUserDevice)

            stateFlow.update { state -> state.copy(registeredUserDevice = registeredUserDevice, lockSetting = lockSetting) }
        }
    }

    
    
    
    fun otpSuccess(registeredUserDevice: RegisteredUserDevice, authTicket: String?): Int {
        viewModelScope.launch {
            loginRepository.updateRegisteredUserDevice(registeredUserDevice)
            loginRepository.updateAuthTicket(authTicket)
            sessionRestorer.restoreSession(Username.ofEmail(registeredUserDevice.login), registeredUserDevice.serverKey)
        }
        return lockTypeManager.getLockType()
    }

    fun loginSuccess(strategy: LoginStrategy.Strategy?) {
        viewModelScope.launch {
            navigationStateFlow.send(LocalLoginNavigationState.Success(strategy))
        }
    }

    fun cancel() {
        viewModelScope.launch {
            lockManager.sendUnLock(UnlockEvent.Reason.Unknown(), false)
            navigationStateFlow.send(LocalLoginNavigationState.Cancel)
        }
    }

    fun changeAccount(email: String?) {
        viewModelScope.launch {
            navigationStateFlow.send(LocalLoginNavigationState.ChangeAccount(email))
        }
    }

    fun logout(email: String?) {
        viewModelScope.launch {
            appEvents.clearLastEvent<UnlockEvent>()
            sessionManager.session?.let { sessionManager.destroySession(it, true) }
            navigationStateFlow.send(LocalLoginNavigationState.Logout(email))
        }
    }
}