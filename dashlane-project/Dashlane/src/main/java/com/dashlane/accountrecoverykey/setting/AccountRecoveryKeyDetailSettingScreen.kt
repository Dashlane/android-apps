package com.dashlane.accountrecoverykey.setting

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.account.UserAccountInfo
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.component.Toggle
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.widgets.compose.CircularProgressIndicator

const val ARK_TOGGLE_TEST_TAG = "arkToggle"

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
        accountType = uiState.data.accountType,
        isLoading = uiState is AccountRecoveryKeyDetailSettingState.Loading,
        isConfirmationDisableDialog = uiState.data.isDialogDisplayed,
        enabled = uiState.data.enabled,
        onToggleClicked = viewModel::toggleClicked,
        onDialogConfirmClick = viewModel::confirmDisable,
        onDialogDismissClick = viewModel::cancelDisable
    )
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun AccountRecoveryKeyDetailSettingContent(
    modifier: Modifier = Modifier,
    accountType: UserAccountInfo.AccountType,
    isLoading: Boolean,
    isConfirmationDisableDialog: Boolean,
    enabled: Boolean,
    onToggleClicked: (Boolean) -> Unit,
    onDialogConfirmClick: () -> Unit,
    onDialogDismissClick: () -> Unit
) {
    Column(
        modifier = modifier
            .semantics { testTagsAsResourceId = true }
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
                    text = when (accountType) {
                        UserAccountInfo.AccountType.InvisibleMasterPassword -> stringResource(id = R.string.account_recovery_key_detailed_setting_mpless_description)
                        UserAccountInfo.AccountType.MasterPassword -> stringResource(id = R.string.account_recovery_key_detailed_setting_description)
                    },
                    style = DashlaneTheme.typography.bodyReducedRegular,
                    color = DashlaneTheme.colors.textNeutralStandard,
                )
            }
            Box(
                modifier = Modifier.weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(28.dp))
                } else {
                    Toggle(
                        modifier = modifier.testTag(ARK_TOGGLE_TEST_TAG),
                        checked = enabled,
                        onCheckedChange = { onToggleClicked(it) },
                    )
                }
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
    Dialog(
        title = stringResource(id = R.string.account_recovery_key_detailed_disable_dialog_title),
        description = {
            Text(text = stringResource(id = R.string.account_recovery_key_detailed_disable_dialog_description))
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.account_recovery_key_detailed_disable_dialog_positive_button)),
        mainActionClick = confirmButtonClick,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.account_recovery_key_detailed_disable_dialog_negative_button)),
        additionalActionClick = dismissButtonClick,
        onDismissRequest = dismissButtonClick
    )
}

@Preview
@Composable
fun AccountRecoveryKeyDetailSettingContentPreview() {
    DashlanePreview {
        AccountRecoveryKeyDetailSettingContent(
            accountType = UserAccountInfo.AccountType.InvisibleMasterPassword,
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
    DashlanePreview {
        DisableARKAlertDialog(
            confirmButtonClick = { },
            dismissButtonClick = {}
        )
    }
}
