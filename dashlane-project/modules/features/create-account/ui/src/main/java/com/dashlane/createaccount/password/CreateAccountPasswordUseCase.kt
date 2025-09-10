package com.dashlane.createaccount.password

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.dashlane.createaccount.CreateAccountRepository
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.debug.services.DaDaDaLogin
import com.dashlane.hardwaresecurity.BiometricAuthModule
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
import com.dashlane.passwordstrength.getUiPasswordStrength
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

fun checkDebugPassword(
    daDaDaLogin: DaDaDaLogin,
    state: CreateAccountPasswordState.View.Initial,
): Flow<CreateAccountPasswordEvent> {
    return flow<CreateAccountPasswordEvent> {
        if (daDaDaLogin.isEnabled) {
            daDaDaLogin.defaultPassword
                ?.let {
                    val password = TextFieldValue(it, selection = TextRange(it.length))
                    emit(CreateAccountPasswordEvent.PasswordChanged(password))
                }
        }
    }
        .catch {
            
        }
}

fun passwordChanged(
    passwordStrengthEvaluator: PasswordStrengthEvaluator,
    state: CreateAccountPasswordState.View.CheckPasswordStrength,
): Flow<CreateAccountPasswordEvent> {
    return flow<CreateAccountPasswordEvent> {
        val passwordStrengthScore = passwordStrengthEvaluator.getPasswordStrength(state.data.password.text).score
        val uiPasswordStrength = passwordStrengthScore.getUiPasswordStrength()

        emit(CreateAccountPasswordEvent.PasswordStrengthChanged(passwordStrengthScore, uiPasswordStrength))
    }
        .catch {
            emit(CreateAccountPasswordEvent.Error(CreateAccountPasswordError.Generic))
        }
}

fun checkDebugPasswordForConfirm(
    daDaDaLogin: DaDaDaLogin,
    state: CreateAccountPasswordState.View.TransitionToConfirm,
): Flow<CreateAccountPasswordEvent> {
    return flow<CreateAccountPasswordEvent> {
        if (daDaDaLogin.isEnabled && state.data.confirmPassword.text.isEmpty()) {
            emit(CreateAccountPasswordEvent.ConfirmPasswordChanged(state.data.password))
        } else {
            emit(CreateAccountPasswordEvent.ConfirmPasswordChanged(TextFieldValue("")))
        }
    }
        .catch {
            emit(CreateAccountPasswordEvent.ConfirmPasswordChanged(TextFieldValue("")))
        }
}

fun savePassword(
    createAccountRepository: CreateAccountRepository,
    biometricAuthModule: BiometricAuthModule,
    state: CreateAccountPasswordState.View.SavePassword,
): Flow<CreateAccountPasswordEvent> {
    return flow<CreateAccountPasswordEvent> {
        createAccountRepository.updatePassword(state.data.password.text.encodeUtf8ToObfuscated())
        val hasBiometrics = biometricAuthModule.isHardwareSetUp() && !biometricAuthModule.isOnlyWeakSupported()
        emit(CreateAccountPasswordEvent.PasswordSaved(hasBiometrics = hasBiometrics))
    }
        .catch {
            emit(CreateAccountPasswordEvent.Error(CreateAccountPasswordError.Generic))
        }
}