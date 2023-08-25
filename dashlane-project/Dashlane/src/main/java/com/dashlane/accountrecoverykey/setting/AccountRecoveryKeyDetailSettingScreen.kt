package com.dashlane.accountrecoverykey.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.Text
import com.dashlane.design.component.Toggle
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.widgets.compose.AlertDialog
import com.dashlane.ui.widgets.compose.CircularProgressIndicator

@Composable
fun AccountRecoveryKeyDetailSettingScreen(
    modifier: Modifier = Modifier,
    viewModel: AccountRecoveryKeyDetailSettingViewModel,
    goToIntro: () -> Unit
) {
    LaunchedEffect(viewModel) {
        viewModel.viewStarted()
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is AccountRecoveryKeyDetailSettingState.GoToIntro -> {
                viewModel.hasNavigated()
                goToIntro()
            }

            else -> Unit
        }
    }

    AccountRecoveryKeyDetailSettingContent(
        modifier = modifier,
        isLoading = uiState is AccountRecoveryKeyDetailSettingState.Loading,
        isConfirmationDisableDialog = uiState.data.isDialogDisplayed,
        enabled = uiState.data.enabled,
        onToggleClicked = viewModel::toggleClicked,
        onDialogConfirmClick = viewModel::confirmDisable,
        onDialogDismissClick = viewModel::cancelDisable
    )
}

@Composable
fun AccountRecoveryKeyDetailSettingContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isConfirmationDisableDialog: Boolean,
    enabled: Boolean,
    onToggleClicked: (Boolean) -> Unit,
    onDialogConfirmClick: () -> Unit,
    onDialogDismissClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.account_recovery_key_detailed_setting_title),
                    style = DashlaneTheme.typography.titleBlockMedium,
                    color = DashlaneTheme.colors.textNeutralCatchy,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(id = R.string.account_recovery_key_detailed_setting_description),
                    style = DashlaneTheme.typography.bodyReducedRegular,
                    color = DashlaneTheme.colors.textNeutralStandard,
                )
            }
            if (isLoading) {
                CircularProgressIndicator(Modifier.size(28.dp))
            } else {
                Toggle(
                    checked = enabled,
                    onCheckedChange = { onToggleClicked(it) },
                )
            }
        }
    }

    if (isConfirmationDisableDialog) {
        DisableARKAlertDialog(
            confirmButtonClick = onDialogConfirmClick,
            dismissButtonClick = onDialogDismissClick
        )
    }
}

@Composable
fun DisableARKAlertDialog(
    confirmButtonClick: () -> Unit,
    dismissButtonClick: () -> Unit
) {
    AlertDialog(
        title = {
            Text(
                text = stringResource(id = R.string.account_recovery_key_detailed_disable_dialog_title),
                style = DashlaneTheme.typography.titleSectionMedium,
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.account_recovery_key_detailed_disable_dialog_description),
                style = DashlaneTheme.typography.bodyStandardRegular,
            )
        },
        confirmButtonText = stringResource(id = R.string.account_recovery_key_detailed_disable_dialog_positive_button),
        confirmButtonClick = confirmButtonClick,
        dismissButtonText = stringResource(id = R.string.account_recovery_key_detailed_disable_dialog_negative_button),
        dismissButtonClick = dismissButtonClick,
        onDismissRequest = dismissButtonClick
    )
}

@Preview
@Composable
fun AccountRecoveryKeyDetailSettingContentPreview() {
    DashlaneTheme {
        AccountRecoveryKeyDetailSettingContent(
            isLoading = false,
            isConfirmationDisableDialog = false,
            enabled = true,
            onToggleClicked = { },
            onDialogConfirmClick = { },
            onDialogDismissClick = {}
        )
    }
}

@Preview
@Composable
fun DisableARKAlertDialogPreview() {
    DashlaneTheme {
        DisableARKAlertDialog(
            confirmButtonClick = { },
            dismissButtonClick = {}
        )
    }
}
