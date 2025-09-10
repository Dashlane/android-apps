package com.dashlane.accountrecoverykey.activation.confirm

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.accountrecoverykey.ui.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Text
import com.dashlane.design.component.TextField
import com.dashlane.design.component.tooling.GenericSeparatorTransformation
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
    cancel: () -> Unit,
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
            onClickSecondary = viewModel::cancelClicked,
        )
    } else {
        AccountRecoveryKeyConfirmContent(
            modifier = modifier,
            isLoading = uiState.isLoading,
            isError = uiState.error is AccountRecoveryKeyConfirmError.KeyError,
            value = uiState.accountRecoveryKey ?: "",
            onKeyChange = viewModel::keyChanged,
            onConfirmClicked = viewModel::confirmClicked,
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
    onConfirmClicked: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp),
    ) {
        Text(
            modifier = Modifier
                .padding(bottom = 8.dp),
            text = stringResource(id = R.string.account_recovery_key_confirm_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
        )
        Text(
            modifier = Modifier
                .padding(bottom = 24.dp),
            text = stringResource(id = R.string.account_recovery_key_confirm_description),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
        )
        RecoveryKeyTextField(
            label = stringResource(id = R.string.account_recovery_key_confirm_label),
            value = value,
            isError = isError,
            onValueChange = onKeyChange,
        )
        Spacer(modifier = Modifier.weight(1f))
        ButtonMedium(
            modifier = Modifier
                .align(Alignment.End),
            onClick = { onConfirmClicked() },
            intensity = Intensity.Catchy,
            enabled = value.isNotEmpty(),
            layout = if (isLoading) {
                ButtonLayout.IndeterminateProgress
            } else {
                ButtonLayout.TextOnly(text = stringResource(id = R.string.account_recovery_key_confirm_button))
            },
        )
    }
}

@Composable
fun RecoveryKeyTextField(
    label: String,
    value: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .focusRequester(focusRequester)
            .onGloballyPositioned { focusRequester.requestFocus() },
        value = value,
        isError = isError,
        feedbackText = if (isError) stringResource(id = R.string.login_account_recovery_key_confirm_error) else null,
        keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, capitalization = KeyboardCapitalization.Characters),
        visualTransformation = remember { GenericSeparatorTransformation(4, '-') },
        onValueChange = { newValue -> onValueChange(newValue.filter { char -> char.isLetterOrDigit() }) },
        label = label,
    )
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
            onConfirmClicked = {},
        )
    }
}