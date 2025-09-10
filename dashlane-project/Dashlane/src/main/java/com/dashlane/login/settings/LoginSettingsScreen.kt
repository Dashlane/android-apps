package com.dashlane.login.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.dashlane.ui.common.compose.components.LoginSettingsContent

@Composable
fun LoginSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginSettingsViewModel,
    success: () -> Unit,
) {
    val uiState by viewModel.stateFlow.viewState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.viewStarted()
        viewModel.stateFlow.sideEffect.collect { state ->
            when (state) {
                LoginSettingsState.SideEffect.Success -> success()
            }
        }
    }

    if (uiState.isLoading) return

    LoginSettingsContent(
        modifier = modifier,
        helpShown = uiState.helpShown,
        snackBarShown = uiState.snackBarShown,
        biometricChecked = uiState.biometricChecked,
        biometricRecoveryChecked = uiState.biometricRecoveryChecked,
        onBiometricCheckedChange = viewModel::onBiometricCheckedChange,
        onBiometricRecoveryCheckedChange = viewModel::onBiometricRecoveryCheckedChange,
        onHelpButton = viewModel::onHelpClicked,
        onNext = viewModel::onNextClicked,
    )
}