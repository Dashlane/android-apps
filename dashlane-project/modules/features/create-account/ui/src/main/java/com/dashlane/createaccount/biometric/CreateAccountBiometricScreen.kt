package com.dashlane.createaccount.biometric

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.ui.common.compose.components.LoginSettingsContent

@Composable
fun CreateAccountBiometricScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateAccountBiometricViewModel,
    onSuccess: () -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                CreateAccountBiometricState.SideEffect.Success -> onSuccess()
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()

    LoginSettingsContent(
        modifier = modifier,
        helpShown = uiState.data.helpShown,
        snackBarShown = uiState.data.snackBarShown,
        biometricChecked = uiState.data.biometricChecked,
        biometricRecoveryChecked = uiState.data.biometricRecoveryChecked,
        onBiometricCheckedChange = { viewModel.sendEvent(CreateAccountBiometricEvent.BiometricCheckChanged(it)) },
        onBiometricRecoveryCheckedChange = { viewModel.sendEvent(CreateAccountBiometricEvent.BiometricRecoveryCheckChanged(it)) },
        onHelpButton = { viewModel.sendEvent(CreateAccountBiometricEvent.HelpClicked) },
        onNext = { viewModel.sendEvent(CreateAccountBiometricEvent.NextClicked) },
    )
}