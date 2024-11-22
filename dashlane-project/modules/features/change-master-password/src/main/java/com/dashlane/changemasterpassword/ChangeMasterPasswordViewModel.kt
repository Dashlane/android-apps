package com.dashlane.changemasterpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.ChangeMasterPasswordError
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.events.user.ChangeMasterPassword
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.passwordstrength.getPasswordStrengthIndicator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChangeMasterPasswordViewModel @Inject constructor(
    private val passwordStrengthEvaluator: PasswordStrengthEvaluator,
    private val logRepository: LogRepository
) : ViewModel() {

    private val _stateFlow =
        MutableViewStateFlow<ChangeMasterPasswordState.View, ChangeMasterPasswordState.SideEffect>(ChangeMasterPasswordState.View())
    val stateFlow: ViewStateFlow<ChangeMasterPasswordState.View, ChangeMasterPasswordState.SideEffect> = _stateFlow

    init {
        logRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.LOGIN_MASTER_PASSWORD_ACCOUNT_RECOVERY_CREATE_NEW_MP)
    }

    fun onPasswordChange(password: String) {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(password = password) }

            val passwordStrengthScore = passwordStrengthEvaluator.getPasswordStrength(password).score
            val passwordStrengthIndicator = passwordStrengthScore.getPasswordStrengthIndicator()

            var isNextEnabled = false

            if (passwordStrengthScore >= PasswordStrengthScore.SAFELY_UNGUESSABLE) {
                logRepository.queueEvent(ChangeMasterPassword(flowStep = FlowStep.ERROR, errorName = ChangeMasterPasswordError.WEAK_PASSWORD_ERROR))
                isNextEnabled = true
            }

            _stateFlow.update { state ->
                state.copy(
                    passwordStrengthScore = passwordStrengthScore,
                    passwordStrength = passwordStrengthIndicator,
                    isNextEnabled = isNextEnabled
                )
            }
        }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        viewModelScope.launch {
            var isMatching = false
            if (confirmPassword == _stateFlow.value.password) {
                logRepository.queueEvent(ChangeMasterPassword(flowStep = FlowStep.ERROR, errorName = ChangeMasterPasswordError.PASSWORDS_DONT_MATCH))
                isMatching = true
            }

            _stateFlow.update { state -> state.copy(confirmPassword = confirmPassword, isMatching = isMatching) }
        }
    }

    fun onNextClicked() {
        viewModelScope.launch {
            val data = _stateFlow.value
            val passwordStrengthScore = data.passwordStrengthScore ?: throw IllegalStateException("passwordStrengthScore should not be null")
            if (!data.isConfirming) {
                if (passwordStrengthScore >= PasswordStrengthScore.SAFELY_UNGUESSABLE) {
                    _stateFlow.update { state -> state.copy(isConfirming = true) }
                } else {
                    _stateFlow.update { state -> state.copy(isNextEnabled = false) }
                }
            } else if (data.isMatching) {
                _stateFlow.send(ChangeMasterPasswordState.SideEffect.Finish(data.confirmPassword.encodeUtf8ToObfuscated()))
            }
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            val data = _stateFlow.value
            if (data.isConfirming) {
                _stateFlow.update { state ->
                    state.copy(
                        isConfirming = false,
                        confirmPassword = "",
                        isNextEnabled = data.passwordStrengthScore?.let { it >= PasswordStrengthScore.SAFELY_UNGUESSABLE } ?: false
                    )
                }
            } else {
                logRepository.queueEvent(ChangeMasterPassword(flowStep = FlowStep.CANCEL))
                _stateFlow.update { state -> state.copy(password = "") }
                _stateFlow.send(ChangeMasterPasswordState.SideEffect.NavigateBack)
            }
        }
    }

    fun tipsClicked() {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(isTipsShown = true) }
        }
    }

    fun bottomSheetDismissed() {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(isTipsShown = false) }
        }
    }
}