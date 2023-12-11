package com.dashlane.createaccount.passwordless

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.password.generator.PasswordGenerator
import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.password.generator.generate
import com.dashlane.security.SecurityHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MplessAccountCreationViewModel @Inject constructor(
    private val securityHelper: SecurityHelper,
    passwordGenerator: PasswordGenerator
) : ViewModel() {

    private val _userDataStateFlow = MutableStateFlow(
        UserData(
            login = "",
            pinCode = "",
            useBiometrics = false,
            masterPassword = passwordGenerator.generate(
                PasswordGeneratorCriteria(
                    length = 40,
                    letters = true,
                    digits = true,
                    symbols = true,
                    ambiguousChars = true
                )
            ).encodeUtf8ToObfuscated(),
            termsOfServicesAccepted = false,
            privacyPolicyAccepted = false
        )
    )
    val userDataStateFlow = _userDataStateFlow.asStateFlow()

    fun initLogin(userLogin: String) {
        viewModelScope.launch {
            _userDataStateFlow.emit(_userDataStateFlow.value.copy(login = userLogin))
        }
    }

    fun onNewPin(pinCode: String) {
        viewModelScope.launch {
            _userDataStateFlow.emit(_userDataStateFlow.value.copy(pinCode = pinCode))
        }
    }

    fun onEnableBiometrics(useBiometrics: Boolean) {
        viewModelScope.launch {
            _userDataStateFlow.emit(_userDataStateFlow.value.copy(useBiometrics = useBiometrics))
        }
    }

    fun isUserAllowedToUsePin(): Boolean = securityHelper.allowedToUsePin()

    fun updateTos(isTosChecked: Boolean) {
        viewModelScope.launch {
            _userDataStateFlow.emit(_userDataStateFlow.value.copy(termsOfServicesAccepted = isTosChecked))
        }
    }

    fun updatePrivacyPolicy(isPrivacyPolicyChecked: Boolean) {
        viewModelScope.launch {
            _userDataStateFlow.emit(_userDataStateFlow.value.copy(privacyPolicyAccepted = isPrivacyPolicyChecked))
        }
    }
}
