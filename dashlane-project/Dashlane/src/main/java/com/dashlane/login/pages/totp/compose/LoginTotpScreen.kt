package com.dashlane.login.pages.totp.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.DashlaneLogoLockup
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.component.TextField
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.lock.LockType
import com.dashlane.util.isNotSemanticallyNull

@Composable
@Suppress("LongMethod")
fun LoginTotpScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginTotpViewModel,
    verificationMode: VerificationMode,
    goToNext: (List<LockType>) -> Unit,
) {
    val uiState by viewModel.stateFlow.viewState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.viewStarted(verificationMode = verificationMode)
        viewModel.stateFlow.sideEffect.collect { state ->
            when (state) {
                is LoginTotpState.SideEffect.Success -> goToNext(state.locks)
            }
        }
    }

    LoginTotpContent(
        modifier = modifier,
        email = uiState.email,
        otp = uiState.otp ?: "",
        isLoading = uiState.isLoading,
        errorMessage = uiState.error?.toErrorMessage(),
        onNext = viewModel::onNext,
        onHelp = viewModel::helpClicked,
        onOtpChange = viewModel::onOTPChange,
    )

    when {
        uiState.showHelpDialog -> {
            HelpDialog(
                onUseRecoveryCode = viewModel::recoveryCodeClicked,
                onUseTextMessage = viewModel::textMessageClicked,
                onCancel = viewModel::recoveryCancelled
            )
        }
        uiState.showRecoveryCodeDialog -> {
            RecoveryCodeDialog(
                message = stringResource(id = R.string.login_totp_enter_token_recovery_dialog_backup_description),
                isLoading = uiState.isLoading,
                errorMessage = if (uiState.isRecoveryError) stringResource(id = R.string.login_totp_enter_token_recovery_dialog_backup_error) else null,
                onInputRecoveryCode = viewModel::onRecoveryTokenComplete,
                onCancel = viewModel::recoveryCancelled
            )
        }
        uiState.showTextMessageDialog -> {
            RecoveryCodeDialog(
                message = stringResource(id = R.string.login_totp_enter_token_recovery_dialog_phone_backup_description),
                isLoading = uiState.isLoading,
                errorMessage = if (uiState.isRecoveryError) stringResource(id = R.string.login_totp_enter_token_recovery_dialog_backup_error) else null,
                onInputRecoveryCode = viewModel::onRecoveryTokenComplete,
                onCancel = viewModel::recoveryCancelled
            )
        }
        uiState.showSendTextMessageDialog -> {
            SendTextMessageDialog(
                onSendTextMessage = viewModel::sendTextMessageClicked,
                onCancel = viewModel::recoveryCancelled
            )
        }
    }
}

@Composable
@Suppress("LongMethod")
fun LoginTotpContent(
    modifier: Modifier = Modifier,
    email: String,
    otp: String,
    isLoading: Boolean,
    errorMessage: String?,
    onNext: () -> Unit,
    onHelp: () -> Unit,
    onOtpChange: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    var textFieldLoaded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        DashlaneLogoLockup(height = 40.dp)
        Text(
            text = email,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(top = 24.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(id = R.string.otp_enabled_token_desc),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(top = 48.dp)
        )
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onGloballyPositioned {
                    if (!textFieldLoaded) {
                        focusRequester.requestFocus()
                        textFieldLoaded = true
                    }
                }
                .padding(top = 24.dp),
            value = otp,
            onValueChange = { newValue: String -> onOtpChange(newValue.filter { char -> char.isDigit() }) },
            label = stringResource(id = R.string.login_totp_text_field_label),
            isError = errorMessage != null,
            feedbackText = errorMessage,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Next
            ),
        )
        Spacer(modifier = Modifier.weight(1f))
        ButtonMediumBar(
            primaryButtonLayout = if (isLoading) ButtonLayout.IndeterminateProgress else ButtonLayout.TextOnly(text = stringResource(id = R.string.login_totp_next_button)),
            isPrimaryButtonEnabled = otp.isNotSemanticallyNull() && !isLoading,
            onPrimaryButtonClick = onNext,
            secondaryButtonLayout = ButtonLayout.TextOnly(text = stringResource(id = R.string.login_totp_use_recovery_code)),
            onSecondaryButtonClick = onHelp
        )
    }
}

@Composable
fun HelpDialog(
    onUseRecoveryCode: () -> Unit,
    onUseTextMessage: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(
        title = stringResource(id = R.string.login_totp_enter_token_recovery_dialog_title),
        description = {
            Text(text = stringResource(id = R.string.login_totp_enter_token_recovery_dialog_choice_description))
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.login_totp_enter_token_recovery_dialog_choice_button_positive)),
        mainActionClick = onUseRecoveryCode,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.login_totp_enter_token_recovery_dialog_choice_button_negative)),
        additionalActionClick = onUseTextMessage,
        onDismissRequest = onCancel,
    )
}

@Composable
fun RecoveryCodeDialog(
    message: String,
    isLoading: Boolean,
    errorMessage: String?,
    onInputRecoveryCode: (String) -> Unit,
    onCancel: () -> Unit
) {
    var value by rememberSaveable { mutableStateOf("") }

    Dialog(
        onDismissRequest = onCancel,
        title = stringResource(id = R.string.disable_totp_enter_token_recovery_dialog_title),
        mainActionLayout = if (isLoading) ButtonLayout.IndeterminateProgress else ButtonLayout.TextOnly(stringResource(id = R.string.login_totp_enter_token_recovery_dialog_backup_button_positive)),
        mainActionClick = { onInputRecoveryCode(value) },
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.login_totp_enter_token_recovery_dialog_backup_button_negative)),
        additionalActionClick = onCancel
    ) {
        Text(text = message)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = value,
            isError = errorMessage != null,
            feedbackText = errorMessage,
            onValueChange = { newValue -> value = newValue.filter { char -> char.isLetterOrDigit() } },
            label = stringResource(id = R.string.login_totp_enter_token_recovery_dialog_backup_hint)
        )
    }
}

@Composable
fun SendTextMessageDialog(
    onSendTextMessage: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(
        title = stringResource(id = R.string.disable_totp_enter_token_recovery_dialog_title),
        description = {
            Text(text = stringResource(id = R.string.login_totp_enter_token_recovery_dialog_phone_description))
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.login_totp_enter_token_recovery_dialog_phone_button_positive)),
        mainActionClick = onSendTextMessage,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.login_totp_enter_token_recovery_dialog_phone_button_negative)),
        additionalActionClick = onCancel,
        onDismissRequest = onCancel,
    )
}

@Composable
fun LoginTotpError.toErrorMessage(): String {
    return when (this) {
        LoginTotpError.InvalidTokenLockedOut -> stringResource(id = R.string.totp_failed_locked_out)
        LoginTotpError.InvalidToken -> stringResource(id = R.string.totp_failed)
        LoginTotpError.AlreadyUsed -> stringResource(id = R.string.login_totp_error_message_already_used)
        LoginTotpError.Network -> stringResource(id = R.string.cannot_connect_to_server)
        LoginTotpError.Offline -> stringResource(id = R.string.offline)
    }
}

@Preview
@Composable
private fun LoginTotpContentPreview() {
    DashlanePreview {
        LoginTotpContent(
            email = "randomemail@provider.com",
            otp = "123",
            onOtpChange = {},
            isLoading = true,
            errorMessage = "Error",
            onNext = {},
            onHelp = {},
        )
    }
}

@Preview
@Composable
private fun HelpDialogPreview() {
    DashlanePreview {
        HelpDialog(
            onUseTextMessage = {},
            onUseRecoveryCode = {},
            onCancel = {}
        )
    }
}

@Preview
@Composable
private fun RecoveryCodeDialogPreview() {
    DashlanePreview {
        RecoveryCodeDialog(
            message = "Message",
            isLoading = true,
            errorMessage = "Error",
            onInputRecoveryCode = {},
            onCancel = {},
        )
    }
}

@Preview
@Composable
private fun SendTextMessageDialogPreview() {
    DashlanePreview {
        SendTextMessageDialog(
            onSendTextMessage = {},
            onCancel = {},
        )
    }
}
