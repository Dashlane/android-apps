package com.dashlane.wear.settings.logout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.State
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionTrasher
import com.dashlane.wear.settings.logout.WearLogoutState.SideEffect.LogoutComplete
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WearLogoutViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val sessionTrasher: SessionTrasher,
) : ViewModel() {
    private val _stateFlow =
        MutableViewStateFlow<WearLogoutState.View, WearLogoutState.SideEffect>(
            WearLogoutState.View(),
        )
    val stateFlow: ViewStateFlow<WearLogoutState.View, WearLogoutState.SideEffect> = _stateFlow

    fun onLogoutConfirmed() {
        viewModelScope.launch {
            val currentSession = sessionManager.session
            requireNotNull(currentSession)
            _stateFlow.update { it.copy(isLogoutInProgress = true) }
            sessionTrasher.trash(username = currentSession.username, deletePreferences = true)

            _stateFlow.send(LogoutComplete)
        }
    }
}

sealed class WearLogoutState : State {
    data class View(val isLogoutInProgress: Boolean = false) : WearLogoutState(), State.View

    sealed class SideEffect : WearLogoutState(), State.SideEffect {
        data object LogoutComplete : SideEffect()
    }
}