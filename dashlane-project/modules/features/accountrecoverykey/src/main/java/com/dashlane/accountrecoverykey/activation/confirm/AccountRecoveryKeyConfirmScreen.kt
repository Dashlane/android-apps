package com.dashlane.accountrecoverykey.activation.confirm

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.dashlane.accountrecoverykey.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Text
import com.dashlane.design.component.TextField
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.GenericErrorContent

@Composable
fun AccountRecoveryKeyConfirmScreen(
    modifier: Modifier = Modifier,
    viewModel: AccountRecoveryKeyConfirmViewModel,
    back: () -> Unit,
    success: () -> Unit,
    cancel: () -> Unit
) {
    val uiState by viewModel.stateFlow.viewState.collectAsState()

    BackHandler(enabled = true) {
        viewModel.onBackPressed()
    }

    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                AccountRecoveryKeyConfirmState.SideEffect.Back -> back()
                AccountRecoveryKeyConfirmState.SideEffect.Cancel -> cancel()
                AccountRecoveryKeyConfirmState.SideEffect.KeyConfirmed -> success()
            }
        }
    }

    if (uiState.error is AccountRecoveryKeyConfirmError.SyncError) {
        GenericErrorContent(
            modifier = modifier,
            textPrimary = stringResource(id = R.string.generic_error_retry_button),
            textSecondary = stringResource(id = R.string.generic_error_cancel_button),
            onClickPrimary = viewModel::retryClicked,
            onClickSecondary = viewModel::cancelClicked
        )
    } else {
        AccountRecoveryKeyConfirmContent(
            modifier = modifier,
            isLoading = uiState.isLoading,
            isError = uiState.error is AccountRecoveryKeyConfirmError.KeyError,
            value = uiState.accountRecoveryKey ?: "",
            onKeyChange = viewModel::keyChanged,
            onConfirmClicked = viewModel::confirmClicked
        )
    }
}

@Composable
fun AccountRecoveryKeyConfirmContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isError: Boolean,
    value: String,
    onKeyChange: (String) -> Unit,
    onConfirmClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(id = R.string.account_recovery_key_confirm_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(top = 72.dp, bottom = 16.dp)
        )
        Text(
            text = stringResource(id = R.string.account_recovery_key_confirm_description),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(bottom = 32.dp)
        )
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            value = value,
            isError = isError,
            feedbackText = if (isError) stringResource(id = R.string.account_recovery_key_confirm_error) else null,
            onValueChange = onKeyChange,
            label = stringResource(id = R.string.account_recovery_key_confirm_label)
        )

        Spacer(modifier = Modifier.weight(1f))

        ButtonMedium(
            modifier = Modifier
                .align(Alignment.End),
            onClick = { onConfirmClicked() },
            intensity = Intensity.Catchy,
            enabled = value.isNotEmpty(),
            layout = if (isLoading) ButtonLayout.IndeterminateProgress else ButtonLayout.TextOnly(text = stringResource(id = R.string.account_recovery_key_confirm_button))
        )
    }
}

@Preview
@Composable
private fun AccountRecoveryKeyConfirmContentPreview() {
    DashlanePreview {
        AccountRecoveryKeyConfirmContent(
            isLoading = true,
            isError = true,
            value = "keyToBeConfirmed",
            onKeyChange = {},
            onConfirmClicked = {}
        )
    }
}
