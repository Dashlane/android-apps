package com.dashlane.login.progress

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.widgets.compose.DashlaneSyncProgress

@Composable
fun LoginSyncProgressScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginSyncProgressViewModel,
    success: () -> Unit,
    syncError: () -> Unit,
    cancel: () -> Unit,
) {
    BackHandler(enabled = true) {
        
    }

    LaunchedEffect(viewModel) {
        viewModel.viewStarted()
        viewModel.stateFlow.sideEffect.collect { state ->
            when (state) {
                LoginSyncProgressState.SideEffect.Success -> success()
                LoginSyncProgressState.SideEffect.SyncError -> syncError()
                LoginSyncProgressState.SideEffect.Cancel -> cancel()
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()

    LoginSyncProgressContent(
        modifier = modifier,
        hasFinishedLoading = uiState.hasFinishedLoading,
        progress = uiState.progress ?: 0,
        message = uiState.message?.let { stringResource(it) } ?: ""
    )

    when (uiState.error) {
        LoginSyncProgressError.Unregister -> LoginSyncProgressUnregisterErrorDialog(
            onCancel = viewModel::cancel,
            onRetry = viewModel::retry
        )
        else -> Unit
    }
}

@Composable
fun LoginSyncProgressContent(
    modifier: Modifier = Modifier,
    hasFinishedLoading: Boolean,
    progress: Int,
    message: String
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DashlaneSyncProgress(
            modifier = modifier,
            color = DashlaneTheme.colors.oddityBrand,
            hasFinishedLoading = hasFinishedLoading,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            progress = progress,
            progressText = message,
            successText = message
        )
        Text(
            text = stringResource(R.string.login_sync_progress_notes),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralQuiet,
        )
    }
}

@Composable
fun LoginSyncProgressUnregisterErrorDialog(
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(
        title = stringResource(id = R.string.login_sync_progress_unlink_devices_error_title),
        description = { Text(stringResource(id = R.string.login_sync_progress_unlink_devices_error_message)) },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.login_sync_progress_unlink_devices_error_positive_button)),
        mainActionClick = onRetry,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.login_sync_progress_unlink_devices_error_negative_button)),
        additionalActionClick = onCancel,
        onDismissRequest = onCancel,
    )
}

@Preview
@Composable
private fun LoginSyncProgressContentPreview() {
    DashlanePreview {
        LoginSyncProgressContent(
            hasFinishedLoading = false,
            progress = 50,
            message = "Message"
        )
    }
}

@Preview
@Composable
private fun LoginSyncProgressContentSuccessPreview() {
    DashlanePreview {
        LoginSyncProgressContent(
            hasFinishedLoading = true,
            progress = 100,
            message = "Success"
        )
    }
}

@Preview
@Composable
private fun LoginSyncProgressUnregisterErrorDialogPreview() {
    DashlanePreview {
        LoginSyncProgressUnregisterErrorDialog(
            onCancel = {},
            onRetry = {}
        )
    }
}
