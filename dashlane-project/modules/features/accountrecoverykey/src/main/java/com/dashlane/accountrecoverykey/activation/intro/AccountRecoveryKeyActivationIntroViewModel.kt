package com.dashlane.accountrecoverykey.activation.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.events.user.CreateAccountRecoveryKey
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.user.UserAccountInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AccountRecoveryKeyActivationIntroViewModel @Inject constructor(
    sessionManager: SessionManager,
    preferencesManager: PreferencesManager,
    private val logRepository: LogRepository,
) : ViewModel() {

    private val stateFlow: MutableStateFlow<AccountRecoveryKeyActivationIntroState>
    val uiState: StateFlow<AccountRecoveryKeyActivationIntroState>

    init {
        val session = sessionManager.session ?: throw IllegalStateException("session cannot be null")
        val accountType = preferencesManager[session.username].accountType
            ?.let { UserAccountInfo.AccountType.fromString(it) }
            ?: throw IllegalStateException("accountType cannot be null")

        stateFlow = MutableStateFlow(AccountRecoveryKeyActivationIntroState(accountType = accountType))
        uiState = stateFlow.asStateFlow()

        logRepository.queueEvent(CreateAccountRecoveryKey(flowStep = FlowStep.START))
        logRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.SETTINGS_SECURITY_RECOVERY_KEY_ENABLE)
    }

    fun onBackPressed() {
        logRepository.queueEvent(CreateAccountRecoveryKey(flowStep = FlowStep.CANCEL))
    }

    fun showSkipAlertDialog() {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(showSkipAlertDialog = true) }
        }
    }

    fun hideSkipAlertDialog() {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(showSkipAlertDialog = false) }
        }
    }
}
