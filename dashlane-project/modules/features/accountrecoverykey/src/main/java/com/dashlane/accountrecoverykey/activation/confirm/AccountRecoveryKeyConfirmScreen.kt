package com.dashlane.accountrecoverykey.activation.confirm

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
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
import com.dashlane.ui.common.compose.components.CircularProgressIndicator
import com.dashlane.ui.widgets.compose.GenericErrorContent

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AccountRecoveryKeyConfirmScreen(
    modifier: Modifier = Modifier.semantics {
        testTagsAsResourceId = true
    },
    viewModel: AccountRecoveryKeyConfirmViewModel,
    back: () -> Unit,
    success: () -> Unit,
    cancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = true) {
        viewModel.onBackPressed()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AccountRecoveryKeyConfirmState.KeyConfirmed -> success()
            is AccountRecoveryKeyConfirmState.Cancel -> cancel()
            is AccountRecoveryKeyConfirmState.Back -> back()

            else -> Unit
        }
    }

    when (val state = uiState) {
        is AccountRecoveryKeyConfirmState.Initial,
        is AccountRecoveryKeyConfirmState.Loading,
        is AccountRecoveryKeyConfirmState.KeyConfirmed,
        is AccountRecoveryKeyConfirmState.Back,
        is AccountRecoveryKeyConfirmState.Cancel,
        is AccountRecoveryKeyConfirmState.KeyError -> {
            AccountRecoveryKeyConfirmContent(
                modifier = modifier,
                isLoading = uiState is AccountRecoveryKeyConfirmState.Loading,
                isError = uiState is AccountRecoveryKeyConfirmState.KeyError,
                keyToBeConfirmed = state.data.accountRecoveryKey ?: "",
                onConfirmClicked = viewModel::confirmClicked
            )
        }
        is AccountRecoveryKeyConfirmState.SyncError -> {
            GenericErrorContent(
                modifier = modifier,
                textPrimary = stringResource(id = R.string.generic_error_retry_button),
                textSecondary = stringResource(id = R.string.generic_error_cancel_button),
                onClickPrimary = viewModel::retryClicked,
                onClickSecondary = viewModel::cancelClicked
            )
        }
    }
}

@Composable
fun AccountRecoveryKeyConfirmContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isError: Boolean,
    keyToBeConfirmed: String = "",
    onConfirmClicked: (String) -> Unit
) {
    var value by rememberSaveable { mutableStateOf(keyToBeConfirmed) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

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
                .padding(bottom = 16.dp)
                .testTag("confirmRecoveryKeyTextField"),
            value = value,
            isError = isError,
            feedbackText = if (isError) stringResource(id = R.string.account_recovery_key_confirm_error) else null,
            onValueChange = { value = it },
            label = stringResource(id = R.string.account_recovery_key_confirm_label)
        )

        Spacer(modifier = Modifier.weight(15f))

        ButtonMedium(
            modifier = Modifier
                .align(Alignment.End),
            onClick = { onConfirmClicked(value) },
            intensity = Intensity.Catchy,
            enabled = value.isNotEmpty(),
            layout = ButtonLayout.TextOnly(
                text = stringResource(id = R.string.account_recovery_key_confirm_button)
            )
        )
    }

    if (isLoading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
    }
}

@Preview
@Composable
fun AccountRecoveryKeyConfirmContentPreview() {
    DashlanePreview {
        AccountRecoveryKeyConfirmContent(
            isLoading = true,
            isError = true,
            keyToBeConfirmed = "keyToBeConfirmed",
            onConfirmClicked = {}
        )
    }
}
