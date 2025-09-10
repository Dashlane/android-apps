package com.dashlane.wear.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.accountstatus.premiumstatus.utils.PlansUtils
import com.dashlane.credentialmanager.WearCredentialManagerUtil
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.State
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.session.SessionManager
import com.dashlane.wear.R
import com.dashlane.wear.settings.WearSettingsState.SideEffect.AskLogoutConfirmation
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WearSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val accountStatusRepository: AccountStatusRepository,
    private val credentialManagerUtil: WearCredentialManagerUtil,
) : ViewModel() {
    private val _stateFlow =
        MutableViewStateFlow<WearSettingsState.View, WearSettingsState.SideEffect>(
            WearSettingsState.View(
                login = "",
                plan = "",
                showCredentialManagerAction = false,
            ),
        )
    val stateFlow: ViewStateFlow<WearSettingsState.View, WearSettingsState.SideEffect> = _stateFlow

    fun reloadSettings() {
        flow<WearSettingsState> {
            val currentSession = sessionManager.session
            requireNotNull(currentSession)
            val plan =
                accountStatusRepository.accountStatusState.value[currentSession]?.premiumStatus?.let {
                    PlansUtils.getSimplePlanTitle(context, it)
                } ?: context.getString(R.string.plan_free_action_bar_title)
            emit(
                WearSettingsState.View(
                    login = currentSession.username.email,
                    plan = plan,
                    showCredentialManagerAction = credentialManagerUtil.isCredentialProviderDisabled(),
                ),
            )
        }.catch { throwable ->
            
            throw (throwable)
        }.onEach { state ->
            when (state) {
                is WearSettingsState.View -> _stateFlow.update { state }
                is WearSettingsState.SideEffect -> {
                    _stateFlow.send(state)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun openPlan() {
        viewModelScope.launch {
            _stateFlow.send(WearSettingsState.SideEffect.OpenMobilePlans)
        }
    }

    fun askLogoutConfirmation() {
        viewModelScope.launch {
            _stateFlow.send(AskLogoutConfirmation)
        }
    }

    fun setCredentialProvider() = credentialManagerUtil.enableCredentialProvider()
}

sealed class WearSettingsState : State {
    data class View(
        val login: String,
        val plan: String,
        val showCredentialManagerAction: Boolean,
    ) : WearSettingsState(), State.View

    sealed class SideEffect : WearSettingsState(), State.SideEffect {
        data object AskLogoutConfirmation : SideEffect()

        data object OpenMobilePlans : SideEffect()
    }
}