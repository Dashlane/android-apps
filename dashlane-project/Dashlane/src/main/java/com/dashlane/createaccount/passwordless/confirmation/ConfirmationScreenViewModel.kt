package com.dashlane.createaccount.passwordless.confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.user.UserAccountInfo
import com.dashlane.authentication.AuthenticationExpiredVersionException
import com.dashlane.authentication.create.AccountCreator
import com.dashlane.createaccount.passwordless.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfirmationScreenViewModel @Inject constructor(
    private val accountCreator: AccountCreator
) : ViewModel() {

    private val stateFlow = MutableStateFlow<ConfirmationState>(ConfirmationState.Initial)
    val uiState = stateFlow.asStateFlow()

    fun createAccount(userData: UserData) {
        viewModelScope.launch {
            stateFlow.emit(ConfirmationState.Loading)

            runCatching {
                accountCreator.createAccount(
                    username = userData.login,
                    password = userData.masterPassword,
                    accountType = UserAccountInfo.AccountType.InvisibleMasterPassword,
                    termsState = AccountCreator.TermsState(
                        conditions = userData.termsOfServicesAccepted,
                        offers = userData.privacyPolicyAccepted
                    ),
                    biometricEnabled = userData.useBiometrics,
                    resetMpEnabled = false,
                    pinCode = userData.pinCode,
                )
            }
                .onSuccess {
                    stateFlow.emit(ConfirmationState.AccountCreated)
                }
                .onFailure { exception ->
                    handleAccountCreationError(exception)
                }
        }
    }

    private suspend fun handleAccountCreationError(exception: Throwable) {
        when (exception) {
            is AccountCreator.CannotInitializeSessionException -> stateFlow.emit(ConfirmationState.Error.NetworkError())
            is AuthenticationExpiredVersionException -> stateFlow.emit(ConfirmationState.Error.ExpiredVersion())
            else -> {
                stateFlow.emit(ConfirmationState.Error.NetworkError())
            }
        }
    }

    fun hasNavigated() {
        viewModelScope.launch {
            stateFlow.emit(ConfirmationState.Initial)
        }
    }
}