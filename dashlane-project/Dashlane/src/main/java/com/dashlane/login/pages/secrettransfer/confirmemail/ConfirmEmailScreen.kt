package com.dashlane.login.pages.secrettransfer.confirmemail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.DashlaneLogoLockup
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import com.dashlane.ui.common.compose.components.GenericErrorContent

@Composable
fun ConfirmEmailScreen(
    modifier: Modifier = Modifier,
    viewModel: ConfirmEmailViewModel,
    secretTransferPayload: SecretTransferPayload,
    goToTOTP: () -> Unit,
    onCancelled: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = uiState) {
        when (uiState) {
            is ConfirmEmailState.AskForTOTP -> {
                viewModel.hasNavigated()
                goToTOTP()
            }
            is ConfirmEmailState.Error,
            is ConfirmEmailState.ConfirmEmail -> Unit

            is ConfirmEmailState.RegisterSuccess -> {
                viewModel.hasNavigated()
                onLoginSuccess()
            }
            is ConfirmEmailState.Cancelled -> {
                viewModel.hasNavigated()
                onCancelled()
            }
        }
    }

    when (uiState) {
        is ConfirmEmailState.Error -> GenericErrorContent(
            title = stringResource(id = R.string.login_secret_transfer_login_error_title),
            message = stringResource(id = R.string.login_secret_transfer_login_error_message),
            textPrimary = stringResource(id = R.string.login_secret_transfer_error_button_retry),
            textSecondary = stringResource(id = R.string.login_secret_transfer_error_button_cancel),
            onClickPrimary = { viewModel.retry(secretTransferPayload) },
            onClickSecondary = viewModel::cancelOnError
        )

        else -> {
            ConfirmEmailContent(
                modifier = modifier,
                email = uiState.data.email,
                onClickConfirm = { viewModel.emailConfirmed(secretTransferPayload) },
                onClickCancel = viewModel::cancel,
            )
        }
    }
}

@Composable
fun ConfirmEmailContent(
    modifier: Modifier = Modifier,
    email: String,
    onClickConfirm: () -> Unit,
    onClickCancel: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            DashlaneLogoLockup(height = 40.dp)
            Text(
                text = (stringResource(id = R.string.login_secret_transfer_step_label, 2, 2)).uppercase(),
                style = DashlaneTheme.typography.bodyHelperRegular,
                color = DashlaneTheme.colors.textNeutralQuiet,
                modifier = Modifier.padding(top = 48.dp)
            )
            Text(
                text = stringResource(id = R.string.login_secret_transfer_confirm_email_title),
                style = DashlaneTheme.typography.titleSectionLarge,
                color = DashlaneTheme.colors.textNeutralCatchy,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.login_secret_transfer_confirm_email_body),
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = email,
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralCatchy,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        ButtonMediumBar(
            primaryText = stringResource(id = R.string.login_secret_transfer_confirm_email_button_confirm),
            secondaryText = stringResource(id = R.string.login_secret_transfer_confirm_email_button_cancel),
            onPrimaryButtonClick = onClickConfirm,
            onSecondaryButtonClick = onClickCancel
        )
    }
}

@Preview
@Composable
private fun ConfirmEmailContentPreview() {
    DashlanePreview {
        ConfirmEmailContent(
            email = "dashlane.com",
            onClickCancel = {},
            onClickConfirm = {}
        )
    }
}
