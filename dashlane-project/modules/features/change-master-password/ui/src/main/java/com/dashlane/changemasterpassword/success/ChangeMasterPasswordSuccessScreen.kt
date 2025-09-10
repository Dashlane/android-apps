package com.dashlane.changemasterpassword.success

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.changemasterpassword.ChangeMasterPasswordOrigin
import com.dashlane.changemasterpassword.ui.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.ReminderMasterPasswordDialog
import com.dashlane.ui.widgets.compose.DashlaneSyncProgress

@Composable
fun ChangeMasterPasswordSuccessScreen(
    modifier: Modifier = Modifier,
    viewModel: ChangeMasterPasswordSuccessViewModel,
    origin: ChangeMasterPasswordOrigin,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    logout: () -> Unit,
) {
    BackHandler {
        
    }

    val uiState by viewModel.stateFlow.viewState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.viewStarted(origin)
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                ChangeMasterPasswordSuccessState.SideEffect.Success -> onSuccess()
                ChangeMasterPasswordSuccessState.SideEffect.NavigateBack,
                ChangeMasterPasswordSuccessState.SideEffect.Cancel,
                -> onCancel()
                ChangeMasterPasswordSuccessState.SideEffect.Logout -> logout()
            }
        }
    }

    ChangeMasterPasswordSuccessContent(
        modifier = modifier,
        progress = uiState.progress,
        hasFinishedLoading = uiState.hasFinishedLoading,
    )

    when {
        uiState.showReminderDialog -> ReminderMasterPasswordDialog(viewModel::dismissReminderDialog)
        uiState.error != null -> {
            when (uiState.error) {
                ChangeMasterPasswordSuccessError.CompletedButSyncError -> ReminderMasterPasswordDialog(
                    viewModel::dismissCompletedButSyncErrorDialog,
                )
                ChangeMasterPasswordSuccessError.Generic -> ErrorDialog(viewModel::dismissErrorDialog)
                else -> Unit
            }
        }
    }
}

@Composable
fun ChangeMasterPasswordSuccessContent(
    modifier: Modifier = Modifier,
    progress: Int?,
    hasFinishedLoading: Boolean,
) {
    DashlaneSyncProgress(
        modifier = modifier.fillMaxSize(),
        color = DashlaneTheme.colors.oddityBrand,
        hasFinishedLoading = hasFinishedLoading,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        progress = progress,
        progressText = stringResource(id = R.string.change_master_password_progress_message),
        successText = stringResource(id = R.string.change_master_password_progress_success_message),
    )
}

@Composable
fun ErrorDialog(
    onDismiss: () -> Unit,
) {
    Dialog(
        title = stringResource(id = R.string.change_master_password_pop_failure_title),
        description = { Text(text = stringResource(id = R.string.change_master_password_popup_failure_message)) },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.ok)),
        mainActionClick = onDismiss,
        onDismissRequest = onDismiss,
        isDestructive = false,
    )
}

@Preview
@Composable
private fun ChangeMasterPasswordSuccessPreview() {
    DashlanePreview {
        ChangeMasterPasswordSuccessContent(
            progress = 75,
            hasFinishedLoading = false,
        )
    }
}

@Preview
@Composable
private fun ReminderDialogPreview() {
    DashlanePreview {
        ReminderMasterPasswordDialog {
        }
    }
}

@Preview
@Composable
private fun ErrorDialogPreview() {
    DashlanePreview {
        ErrorDialog {
        }
    }
}