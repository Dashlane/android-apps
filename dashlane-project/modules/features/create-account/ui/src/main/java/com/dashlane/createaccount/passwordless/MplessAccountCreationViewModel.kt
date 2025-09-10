package com.dashlane.createaccount.passwordless

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.password.generator.PasswordGenerator
import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.password.generator.generate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MplessAccountCreationViewModel @Inject constructor(
    private val passwordGenerator: PasswordGenerator,
    private val mplessAccountCreationRepository: MplessAccountCreationRepository,
) : ViewModel() {
    fun initLogin(email: String) {
        viewModelScope.launch {
            val password = passwordGenerator.generate(
                PasswordGeneratorCriteria(
                    length = 40,
                    letters = true,
                    digits = true,
                    symbols = true,
                    ambiguousChars = true,
                ),
            ).encodeUtf8ToObfuscated()

            mplessAccountCreationRepository.updateEmail(email)
            mplessAccountCreationRepository.updatePassword(password)
        }
    }

    fun onNewPin(pinCode: String) {
        viewModelScope.launch {
            mplessAccountCreationRepository.updatePinCode(pinCode)
        }
    }

    fun onEnableBiometrics(useBiometrics: Boolean) {
        viewModelScope.launch {
            mplessAccountCreationRepository.updateEnableBiometric(useBiometrics)
        }
    }
}