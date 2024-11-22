package com.dashlane.login.pages.biometric.recovery

import androidx.activity.compose.BackHandler
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.changemasterpassword.success.ReminderDialog
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.login.LoginStrategy
import com.dashlane.ui.common.compose.components.GenericErrorContent
import com.dashlane.ui.widgets.compose.DashlaneSyncProgress
import kotlinx.coroutines.delay

@Composable
fun BiometricRecoveryScreen(
    modifier: Modifier = Modifier,
    viewModel: BiometricRecoveryViewModel,
    onSuccess: (LoginStrategy.Strategy) -> Unit,
    onCancel: () -> Unit
) {
    BackHandler {
        
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigationState by viewModel.navigationState.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(viewModel) {
        viewModel.viewStarted()
    }

    LaunchedEffect(navigationState) {
        when (val state = navigationState) {
            is BiometricRecoveryNavigationState.Success -> {
                delay(1_000) 
                onSuccess(state.strategy)
            }
            BiometricRecoveryNavigationState.Cancel -> onCancel()
            null -> Unit
        }
    }

    when {
        uiState.isError -> {
            GenericErrorContent(
                modifier = modifier,
                textPrimary = stringResource(id = R.string.generic_error_retry_button),
                textSecondary = stringResource(id = R.string.generic_error_cancel_button),
                onClickPrimary = viewModel::retry,
                onClickSecondary = viewModel::cancelClicked
            )
        }
        else -> {
            BiometricRecoveryContent(
                modifier = modifier,
                progress = uiState.progress,
                hasFinishedLoading = navigationState is BiometricRecoveryNavigationState
            )
        }
    }

    if (uiState.showReminderDialog) {
        ReminderDialog(viewModel::confirmReminder)
    }
}

@Composable
@VisibleForTesting
fun BiometricRecoveryContent(
    modifier: Modifier = Modifier,
    progress: Int?,
    hasFinishedLoading: Boolean
) {
    DashlaneSyncProgress(
        modifier = modifier.fillMaxSize(),
        color = DashlaneTheme.colors.oddityBrand,
        hasFinishedLoading = hasFinishedLoading,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        progress = progress,
        progressText = stringResource(id = R.string.login_account_recovery_key_enter_progress_title),
        successText = stringResource(id = R.string.login_account_recovery_key_enter_success_title)
    )
}

@Preview
@Composable
private fun RecoveryContentPreview() {
    DashlanePreview {
        BiometricRecoveryContent(
            progress = 75,
            hasFinishedLoading = false
        )
    }
}
