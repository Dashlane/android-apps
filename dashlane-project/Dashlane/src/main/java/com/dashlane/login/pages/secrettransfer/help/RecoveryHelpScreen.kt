package com.dashlane.login.pages.secrettransfer.help

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.ui.widgets.compose.GenericInfoContent

@Composable
fun RecoveryHelpScreen(
    modifier: Modifier = Modifier,
    viewModel: RecoveryHelpViewModel,
    email: String?,
    onStartRecoveryClicked: (RegisteredUserDevice) -> Unit,
    onLostKeyClicked: () -> Unit
) {
    LaunchedEffect(viewModel) {
        viewModel.viewStarted(email)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is RecoveryHelpState.GoToARK -> {
                viewModel.viewNavigated()
                onStartRecoveryClicked(state.registeredUserDevice)
            }
            is RecoveryHelpState.GoToLostKey -> {
                viewModel.viewNavigated()
                onLostKeyClicked()
            }
            else -> Unit
        }
    }

    GenericInfoContent(
        modifier = modifier,
        icon = null,
        title = stringResource(id = R.string.login_universal_d2d_recovery_help_title),
        description = stringResource(id = R.string.login_universal_d2d_recovery_help_description),
        textPrimary = stringResource(id = R.string.login_universal_d2d_recovery_help_primary_button),
        onClickPrimary = viewModel::arkClicked,
        textSecondary = stringResource(id = R.string.login_universal_d2d_recovery_help_secondary_button),
        onClickSecondary = viewModel::lostKeyClicked
    )
}