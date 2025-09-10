package com.dashlane.wear.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.UserStorage
import com.dashlane.credentialmanager.WearCredentialManagerUtil
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPass
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.State
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.session.Session
import com.dashlane.session.SessionRestorer
import com.dashlane.user.Username
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WearLoginViewModel @Inject constructor(
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val userStorage: UserStorage,
    private val sessionRestorer: SessionRestorer,
    private val loginRepository: WearLoginRepository,
    private val lockManager: LockManager,
    private val credentialManagerUtil: WearCredentialManagerUtil,
) : ViewModel() {
    private val _stateFlow: MutableViewStateFlow<LoginState.View, LoginState.SideEffect> =
        MutableViewStateFlow(LoginState.View())
    val stateFlow: ViewStateFlow<LoginState.View, LoginState.SideEffect> = _stateFlow

    fun viewStarted() {
        viewModelScope.launch {
            _stateFlow.update { it.copy(error = null) }
            val localUserName = globalPreferencesManager.getDefaultUsername()
            val localUserDevice: UserStorage.UserDevice? = localUserName
                ?.let { user ->
                    userStorage.getUser(user.email)
                }
            if (localUserDevice == null) {
                showOnboardingScreen()
                return@launch
            }

            when {
                localUserDevice.isServerKeyNeeded -> {
                    restoreFromServerKey(localUserName)
                }

                else -> tryRestoreSession(localUserName)
            }
        }
    }

    private suspend fun tryRestoreSession(username: Username) {
        sessionRestorer.restore(username = username)
            ?.let { onRestoreSuccessful(it) }
            ?: onSessionRestorationError(username)
    }

    private suspend fun restoreFromServerKey(username: Username) {
        val login = username.email
        val serverKey = loginRepository.getRegisteredUserDevice()?.serverKey
        if (serverKey == null) {
            loginRepository.updateUserLogin(login)
            _stateFlow.send(LoginState.SideEffect.GoSecondFactor)
            return
        }

        sessionRestorer.restoreSession(username = username, serverKey = serverKey)
            ?.let { onRestoreSuccessful(it) }
            ?: onSessionRestorationError(username)
    }

    private suspend fun onRestoreSuccessful(session: Session) {
        lockManager.unlock(
            session = session,
            pass = LockPass.ofPassword(session.appKey),
        )
        val canSetupCredManager =
            !globalPreferencesManager.hasActivatedAutofillOnce() && credentialManagerUtil.isCredentialProviderDisabled()
        _stateFlow.send(LoginState.SideEffect.SessionRestored(canSetupCredManager))
    }

    private fun onSessionRestorationError(username: Username) {
            tag = "WearLogin",
            message = "Could not restore user session ${username.email}",
        )
        viewModelScope.launch {
            _stateFlow.update {
                it.copy(error = LoginState.Error.SessionRestorationError)
            }
        }
    }

    private fun showOnboardingScreen() {
        viewModelScope.launch {
            _stateFlow.send(LoginState.SideEffect.GoWelcome)
        }
    }
}

sealed class LoginState : State {
    data class View(val error: Error? = null) : State.View

    sealed class SideEffect : LoginState(), State.SideEffect {
        data object GoWelcome : SideEffect()

        data object GoSecondFactor : SideEffect()

        data class SessionRestored(val canSetupCredentialManager: Boolean) : SideEffect()
    }

    sealed class Error {
        data object SessionRestorationError : Error()
    }
}