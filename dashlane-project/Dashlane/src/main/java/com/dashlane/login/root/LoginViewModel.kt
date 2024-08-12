package com.dashlane.login.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.user.UserAccountInfo
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.login.SsoInfo
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.toSecurityFeatures
import com.dashlane.session.SessionRestorer
import com.dashlane.user.Username
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val sessionRestorer: SessionRestorer,
    private val lockTypeManager: LockTypeManager,
) : ViewModel() {

    private val stateFlow = MutableStateFlow(LoginState())
    val uiState = stateFlow.asStateFlow()

    fun localLoginStarted(userAccountInfo: UserAccountInfo) {
        viewModelScope.launch {
            val registeredUserDevice = RegisteredUserDevice.Local(
                login = userAccountInfo.username,
                securityFeatures = userAccountInfo.securitySettings.toSecurityFeatures(),
                accessKey = userAccountInfo.accessKey
            )
            loginRepository.updateRegisteredUserDevice(registeredUserDevice)
            stateFlow.update { state -> state.copy(registeredUserDevice = registeredUserDevice) }
        }
    }

    fun deviceRegistered(registeredUserDevice: RegisteredUserDevice, authTicket: String?) {
        viewModelScope.launch {
            loginRepository.updateRegisteredUserDevice(registeredUserDevice)
            loginRepository.updateAuthTicket(authTicket)
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

    fun updateSsoInfo(ssoInfo: SsoInfo?) {
        viewModelScope.launch {
            loginRepository.updateSsoInfo(ssoInfo)
        }
    }
}