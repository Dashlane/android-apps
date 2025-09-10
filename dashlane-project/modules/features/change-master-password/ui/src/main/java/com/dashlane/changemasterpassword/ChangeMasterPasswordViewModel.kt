package com.dashlane.changemasterpassword

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.changemasterpassword.ChangeMasterPasswordState.SideEffect
import com.dashlane.changemasterpassword.ChangeMasterPasswordState.View
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
import com.dashlane.passwordstrength.getUiPasswordStrength
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeMasterPasswordViewModel @Inject constructor(
    private val passwordStrengthEvaluator: PasswordStrengthEvaluator,
    private val changeMasterPasswordRepository: ChangeMasterPasswordRepository,
    private val logRepository: LogRepository,
) : ViewModel() {
    private val _stateFlow = MutableViewStateFlow<View, SideEffect>(View())
    val stateFlow: ViewStateFlow<View, SideEffect> = _stateFlow

    fun viewStarted(origin: ChangeMasterPasswordOrigin) {
        when (origin) {
            ChangeMasterPasswordOrigin.AccountRecovery -> {
                logRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.LOGIN_MASTER_PASSWORD_ACCOUNT_RECOVERY_CREATE_NEW_MP)
            }
            ChangeMasterPasswordOrigin.Settings -> {
                logRepository.queueEvent(ChangeMasterPassword(flowStep = FlowStep.START))
            }
            ChangeMasterPasswordOrigin.BiometricRecovery,
            is ChangeMasterPasswordOrigin.Migration,
            -> Unit
        }
    }

    fun onPasswordChange(password: TextFieldValue) {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(password = password) }

            val passwordStrengthScore = passwordStrengthEvaluator.getPasswordStrength(password.text).score
            val uiPasswordStrength = passwordStrengthScore.getUiPasswordStrength()

            var isNextEnabled = false

            if (passwordStrengthScore >= PasswordStrengthScore.SAFELY_UNGUESSABLE) {
                logRepository.queueEvent(
                    ChangeMasterPassword(flowStep = FlowStep.ERROR, errorName = ChangeMasterPasswordError.WEAK_PASSWORD_ERROR),
                )
                isNextEnabled = true
            }

            _stateFlow.update { state ->
                state.copy(
                    passwordStrengthScore = passwordStrengthScore,
                    passwordStrength = uiPasswordStrength,
                    isNextEnabled = isNextEnabled,
                )
            }
        }
    }

    fun onFocusChanged() {
        viewModelScope.launch {
            val data = _stateFlow.value
            if (data.isConfirming) {
                _stateFlow.update { state ->
                    state.copy(
                        isConfirming = false,
                        confirmPassword = TextFieldValue(""),
                        isNextEnabled = data.passwordStrengthScore?.let { it >= PasswordStrengthScore.SAFELY_UNGUESSABLE } ?: false,
                    )
                }
            }
        }
    }

    fun onConfirmPasswordChange(confirmPassword: TextFieldValue) {
        viewModelScope.launch {
            var isMatching = false
            if (confirmPassword.text == _stateFlow.value.password.text) {
                logRepository.queueEvent(
                    ChangeMasterPassword(flowStep = FlowStep.ERROR, errorName = ChangeMasterPasswordError.PASSWORDS_DONT_MATCH),
                )
                isMatching = true
            }

            _stateFlow.update { state -> state.copy(confirmPassword = confirmPassword, isMatching = isMatching) }
        }
    }

    fun onNextClicked() {
        viewModelScope.launch {
            val data = _stateFlow.value
            val passwordStrengthScore = data.passwordStrengthScore
                ?: throw IllegalStateException("passwordStrengthScore should not be null")
            if (!data.isConfirming) {
                if (passwordStrengthScore >= PasswordStrengthScore.SAFELY_UNGUESSABLE) {
                    _stateFlow.update { state -> state.copy(isConfirming = true) }
                } else {
                    _stateFlow.update { state -> state.copy(isNextEnabled = false) }
                }
            } else if (data.isMatching) {
                changeMasterPasswordRepository.updateNewMasterPassword(data.confirmPassword.text.encodeUtf8ToObfuscated())
                _stateFlow.send(SideEffect.Finish)
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
                        confirmPassword = TextFieldValue(""),
                        isNextEnabled = data.passwordStrengthScore?.let { it >= PasswordStrengthScore.SAFELY_UNGUESSABLE } ?: false,
                    )
                }
            } else {
                logRepository.queueEvent(ChangeMasterPassword(flowStep = FlowStep.CANCEL))
                _stateFlow.update { state -> state.copy(password = TextFieldValue("")) }
                _stateFlow.send(SideEffect.NavigateBack)
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