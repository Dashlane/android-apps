package com.dashlane.masterpassword.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.passwordstrength.getPasswordStrengthIndicator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ChangeMasterPasswordViewModel @Inject constructor(
    private val passwordStrengthEvaluator: PasswordStrengthEvaluator,
    private val logRepository: LogRepository
) : ViewModel() {

    private val stateFlow = MutableStateFlow<ChangeMasterPasswordState>(ChangeMasterPasswordState.Initial(ChangeMasterPasswordData()))
    val uiState = stateFlow.asStateFlow()

    init {
        logRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.LOGIN_MASTER_PASSWORD_ACCOUNT_RECOVERY_CREATE_NEW_MP)
    }

    fun hasNavigated() {
        viewModelScope.launch {
            stateFlow.emit(ChangeMasterPasswordState.Initial(ChangeMasterPasswordData()))
        }
    }

    fun onPasswordChange(password: String) {
        viewModelScope.launch {
            stateFlow.emit(ChangeMasterPasswordState.PasswordChanged(stateFlow.value.data.copy(password = password)))

            val passwordStrengthScore = passwordStrengthEvaluator.getPasswordStrength(password).score
            val passwordStrengthIndicator = passwordStrengthScore.getPasswordStrengthIndicator()

            stateFlow.emit(
                ChangeMasterPasswordState.PasswordChanged(
                    stateFlow.value.data.copy(
                        passwordStrengthScore = passwordStrengthScore,
                        passwordStrength = passwordStrengthIndicator,
                        isNextEnabled = passwordStrengthScore >= PasswordStrengthScore.SAFELY_UNGUESSABLE
                    )
                )
            )
        }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        viewModelScope.launch {
            val data = stateFlow.value.data
            val newData = data.copy(confirmPassword = confirmPassword, isMatching = confirmPassword == data.password)
            stateFlow.emit(ChangeMasterPasswordState.ConfimPasswordChanged(newData))
        }
    }

    fun onNextClicked() {
        viewModelScope.launch {
            val data = stateFlow.value.data
            val passwordStrengthScore = data.passwordStrengthScore ?: throw IllegalStateException("passwordStrengthScore should not be null")
            if (!data.isConfirming) {
                if (passwordStrengthScore >= PasswordStrengthScore.SAFELY_UNGUESSABLE) {
                    stateFlow.emit(ChangeMasterPasswordState.ConfimPassword(data.copy(isConfirming = true)))
                } else {
                    stateFlow.emit(ChangeMasterPasswordState.NotStrongEnough(data.copy(isNextEnabled = false)))
                }
            } else if (data.isMatching) {
                stateFlow.emit(ChangeMasterPasswordState.Finish(data, data.confirmPassword.encodeUtf8ToObfuscated()))
            }
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            val data = stateFlow.value.data
            if (data.isConfirming) {
                stateFlow.emit(
                    ChangeMasterPasswordState.Initial(
                        data.copy(
                            isConfirming = false,
                            confirmPassword = "",
                            isNextEnabled = data.passwordStrengthScore?.let { it >= PasswordStrengthScore.SAFELY_UNGUESSABLE } ?: false
                        )
                    )
                )
            } else {
                stateFlow.emit(ChangeMasterPasswordState.NavigateBack(data))
            }
        }
    }
}