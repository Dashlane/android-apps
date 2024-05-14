@file:OptIn(ExperimentalComposeUiApi::class)

package com.dashlane.accountrecoverykey.activation.generate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.dashlane.account.UserAccountInfo
import com.dashlane.accountrecoverykey.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.widgets.compose.CircularProgressIndicator
import com.dashlane.ui.widgets.compose.GenericErrorContent

@Composable
fun AccountRecoveryKeyGenerateScreen(
    modifier: Modifier = Modifier,
    viewModel: AccountRecoveryKeyGenerateViewModel,
    goToConfirm: () -> Unit,
    cancel: () -> Unit
) {
    BackHandler(enabled = true) {
        viewModel.onBackPressed()
    }

    LaunchedEffect(viewModel) {
        viewModel.viewStarted()
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is AccountRecoveryKeyGenerateState.GoToConfirm -> {
                viewModel.hasNavigated()
                goToConfirm()
            }
            is AccountRecoveryKeyGenerateState.Cancel -> {
                viewModel.hasNavigated()
                cancel()
            }
            else -> Unit
        }
    }

    when (uiState) {
        is AccountRecoveryKeyGenerateState.GoToConfirm,
        is AccountRecoveryKeyGenerateState.Initial,
        is AccountRecoveryKeyGenerateState.KeyGenerated,
        is AccountRecoveryKeyGenerateState.Loading,
        is AccountRecoveryKeyGenerateState.Cancel -> {
            AccountRecoveryKeyGenerateContent(
                modifier = modifier,
                isLoading = uiState is AccountRecoveryKeyGenerateState.Loading,
                accountType = uiState.data.accountType,
                accountRecoveryKey = uiState.data.accountRecoveryKey ?: "",
                onContinueClicked = viewModel::continueClicked,
                onCopy = { viewModel.copy(uiState.data.accountRecoveryKey ?: "") }
            )
        }

        is AccountRecoveryKeyGenerateState.Error -> {
            GenericErrorContent(
                textPrimary = stringResource(id = R.string.generic_error_retry_button),
                textSecondary = stringResource(id = R.string.generic_error_cancel_button),
                onClickPrimary = viewModel::retryClicked,
                onClickSecondary = cancel
            )
        }
    }

    if (uiState.data.cancelDialogShown) {
        CancelDialog(
            onDismiss = viewModel::cancelDialogDismissed,
            onCancel = viewModel::cancelConfirmed
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Suppress("LongMethod")
@Composable
fun AccountRecoveryKeyGenerateContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    accountType: UserAccountInfo.AccountType,
    accountRecoveryKey: String,
    onContinueClicked: () -> Unit,
    onCopy: () -> Unit
) {
    var multiplier by remember { mutableFloatStateOf(1f) }

    Column(
        modifier = modifier
            .semantics { testTagsAsResourceId = true }
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = stringResource(id = R.string.account_recovery_key_generate_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(top = 72.dp, bottom = 16.dp)
        )
        Text(
            text = when (accountType) {
                UserAccountInfo.AccountType.InvisibleMasterPassword -> stringResource(id = R.string.account_recovery_key_generate_mpless_description_1)
                UserAccountInfo.AccountType.MasterPassword -> stringResource(id = R.string.account_recovery_key_generate_description_1)
            },
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .padding(bottom = 16.dp)
                .background(
                    color = DashlaneTheme.colors.containerAgnosticNeutralSupershy,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(top = 16.dp, bottom = 16.dp, start = 4.dp, end = 4.dp)
                .combinedClickable(
                    onClick = onCopy,
                    onLongClick = onCopy
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text(
                    modifier = Modifier.testTag("arkCode"),
                    text = styledAccountRecoveryKey(accountRecoveryKey),
                    textAlign = TextAlign.Center,
                    color = DashlaneTheme.colors.textNeutralStandard,
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    style = DashlaneTheme.typography.bodyStandardRegular.copy(fontSize = DashlaneTheme.typography.bodyStandardRegular.fontSize * multiplier),
                    onTextLayout = { if (it.hasVisualOverflow) multiplier *= 0.95f }
                )
            }
        }
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = stringResource(id = R.string.account_recovery_key_generate_description_2),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
        )

        Spacer(modifier = Modifier.weight(15f))

        ButtonMedium(
            modifier = Modifier
                .align(Alignment.End),
            onClick = onContinueClicked,
            intensity = Intensity.Catchy,
            layout = ButtonLayout.TextOnly(
                text = stringResource(id = R.string.account_recovery_key_generate_button)
            )
        )
    }
}

@Composable
private fun styledAccountRecoveryKey(accountRecoveryKey: String): AnnotatedString {
    return buildAnnotatedString {
        accountRecoveryKey
            .forEach { char ->
                if (char.isDigit()) {
                    withStyle(style = SpanStyle(color = DashlaneTheme.colors.textOddityPasswordDigits.value)) {
                        append(char)
                    }
                } else {
                    withStyle(style = SpanStyle(color = DashlaneTheme.colors.textNeutralCatchy.value)) {
                        append(char.uppercase())
                    }
                }
            }
    }
}

@Composable
fun CancelDialog(
    onDismiss: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(
        title = stringResource(id = R.string.account_recovery_key_generate_cancel_dialog_title),
        description = {
            Text(text = stringResource(id = R.string.account_recovery_key_generate_cancel_dialog_message))
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.account_recovery_key_generate_cancel_dialog_positive_button)),
        mainActionClick = onDismiss,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.account_recovery_key_generate_cancel_dialog_negative_button)),
        additionalActionClick = onCancel,
        onDismissRequest = onDismiss,
        isDestructive = false,
        properties = DialogProperties()
    )
}

@Preview
@Composable
fun AccountRecoveryKeyCreationContentPreview() {
    DashlanePreview {
        AccountRecoveryKeyGenerateContent(
            accountType = UserAccountInfo.AccountType.MasterPassword,
            accountRecoveryKey = "AA11-AA11-AA11-AA11-AA11-AA11-AA11",
            onContinueClicked = {},
            onCopy = {}
        )
    }
}

@Preview
@Composable
fun CancelDialogPreview() {
    DashlanePreview {
        CancelDialog(
            onDismiss = {},
            onCancel = {}
        )
    }
}
