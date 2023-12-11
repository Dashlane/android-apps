package com.dashlane.accountrecoverykey.activation.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.account.UserAccountInfo
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.events.user.CreateAccountRecoveryKey
import com.dashlane.preference.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AccountRecoveryKeyActivationIntroViewModel @Inject constructor(
    private val logRepository: LogRepository,
    userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val stateFlow: MutableStateFlow<AccountRecoveryKeyActivationIntroState>

    init {
        val accountType = UserAccountInfo.AccountType.fromString(userPreferencesManager.accountType)
        stateFlow =
            MutableStateFlow(AccountRecoveryKeyActivationIntroState.Default(AccountRecoveryKeyActivationIntroData(accountType = accountType)))

        logRepository.queueEvent(CreateAccountRecoveryKey(flowStep = FlowStep.START))
        logRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.SETTINGS_SECURITY_RECOVERY_KEY_ENABLE)
    }

    val uiState = stateFlow.asStateFlow()

    fun onBackPressed() {
        logRepository.queueEvent(CreateAccountRecoveryKey(flowStep = FlowStep.CANCEL))
    }

    fun showSkipAlertDialog() {
        viewModelScope.launch {
            stateFlow.emit(AccountRecoveryKeyActivationIntroState.SkipAlertDialogVisible(stateFlow.value.data))
        }
    }

    fun hideSkipAlertDialog() {
        viewModelScope.launch {
            stateFlow.emit(AccountRecoveryKeyActivationIntroState.Default(stateFlow.value.data))
        }
    }
}
