package com.dashlane.login.pages.totp.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.dashlane.R
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.component.TextField
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.widgets.compose.CircularProgressIndicator
import com.dashlane.ui.widgets.compose.DashlaneLogo

@Composable
fun LoginTotpScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginTotpViewModel,
    login: String,
    goToNext: (RegisteredUserDevice, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginTotpState.Success -> {
                viewModel.hasNavigated()
                goToNext(state.registeredUserDevice, state.authTicket)
            }

            else -> Unit
        }
    }

    val errorMessage: String? = when ((uiState as? LoginTotpState.Error)?.error) {
        LoginTotpError.InvalidToken -> stringResource(id = R.string.totp_failed)
        LoginTotpError.AlreadyUsed -> stringResource(id = R.string.login_totp_error_message_already_used)
        LoginTotpError.Network -> stringResource(id = R.string.cannot_connect_to_server)
        LoginTotpError.Offline -> stringResource(id = R.string.offline)
        null -> null
    }

    LoginTotpContent(
        modifier = modifier,
        login = login,
        otp = uiState.data.otp ?: "",
        isLoading = uiState is LoginTotpState.Loading,
        errorMessage = errorMessage,
        isError = uiState is LoginTotpState.Error,
        onNext = viewModel::onNext,
        onHelp = viewModel::helpClicked,
        onOtpChange = viewModel::onOTPChange,
    )

    when {
        uiState.data.showHelpDialog -> {
            HelpDialog(
                onUseRecoveryCode = viewModel::recoveryCodeClicked,
                onUseTextMessage = viewModel::textMessageClicked,
                onCancel = viewModel::recoveryCancelled
            )
        }
        uiState.data.showRecoveryCodeDialog -> {
            RecoveryCodeDialog(
                message = stringResource(id = R.string.login_totp_enter_token_recovery_dialog_backup_description),
                onInputRecoveryCode = viewModel::onRecoveryTokenComplete,
                onCancel = viewModel::recoveryCancelled
            )
        }
        uiState.data.showTextMessageDialog -> {
            RecoveryCodeDialog(
                message = stringResource(id = R.string.login_totp_enter_token_recovery_dialog_phone_backup_description),
                onInputRecoveryCode = viewModel::onRecoveryTokenComplete,
                onCancel = viewModel::recoveryCancelled
            )
        }
        uiState.data.showSendTextMessageDialog -> {
            SendTextMessageDialog(
                onSendTextMessage = viewModel::sendTextMessageClicked,
                onCancel = viewModel::recoveryCancelled
            )
        }
    }
}

@Composable
fun LoginTotpContent(
    modifier: Modifier = Modifier,
    login: String,
    otp: String,
    isLoading: Boolean,
    errorMessage: String?,
    isError: Boolean,
    onNext: () -> Unit,
    onHelp: () -> Unit,
    onOtpChange: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        DashlaneLogo()
        Text(
            text = login,
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
                .padding(top = 24.dp),
            value = otp,
            onValueChange = { newValue: String -> onOtpChange(newValue.filter { char -> char.isDigit() }) },
            label = stringResource(id = R.string.login_totp_text_field_label),
            isError = isError,
            feedbackText = if (isError) errorMessage else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Next
            ),
        )
        Spacer(modifier = Modifier.weight(1f))

        ButtonMediumBar(
            modifier = modifier
                .align(Alignment.End),
            primaryText = stringResource(id = R.string.login_totp_next_button),
            secondaryText = stringResource(id = R.string.login_totp_use_recovery_code),
            onPrimaryButtonClick = onNext,
            onSecondaryButtonClick = onHelp
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
        isDestructive = false,
        properties = DialogProperties()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryCodeDialog(
    message: String,
    onInputRecoveryCode: (String) -> Unit,
    onCancel: () -> Unit
) {
    var value by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        properties = DialogProperties()
    ) {
        val shape = RoundedCornerShape(size = 12.dp)
        val containerColor = DashlaneTheme.colors.containerAgnosticNeutralSupershy
        val tonalElevation = 1.dp
        Surface(
            shape = shape,
            color = containerColor,
            tonalElevation = tonalElevation,
        ) {
            Column(
                modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 18.dp)
            ) {
                Text(
                    modifier = Modifier
                        .semantics { heading() }
                        .padding(bottom = 8.dp),
                    text = stringResource(id = R.string.disable_totp_enter_token_recovery_dialog_title),
                    color = DashlaneTheme.colors.textNeutralCatchy,
                    style = DashlaneTheme.typography.titleSectionMedium
                )
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(weight = 1f, fill = false)
                        .padding(top = 8.dp, bottom = 8.dp)
                ) {
                    Text(text = message)
                }
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    value = value,
                    onValueChange = { newValue -> value = newValue.filter { char -> char.isLetterOrDigit() } },
                    label = stringResource(id = R.string.login_totp_enter_token_recovery_dialog_backup_hint)
                )
                ButtonMediumBar(
                    primaryButtonLayout = ButtonLayout.TextOnly(stringResource(id = R.string.login_totp_enter_token_recovery_dialog_backup_button_positive)),
                    onPrimaryButtonClick = { onInputRecoveryCode(value) },
                    secondaryButtonLayout = ButtonLayout.TextOnly(stringResource(id = R.string.login_totp_enter_token_recovery_dialog_backup_button_negative)),
                    onSecondaryButtonClick = onCancel
                )
            }
        }
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
        isDestructive = false,
        properties = DialogProperties()
    )
}

@Preview
@Composable
fun LoginTotpContentPreview() {
    DashlanePreview {
        LoginTotpContent(
            login = "randomemail@provider.com",
            otp = "123",
            onOtpChange = {},
            isLoading = true,
            errorMessage = "Error",
            onNext = {},
            onHelp = {},
            isError = false
        )
    }
}

@Preview
@Composable
fun HelpDialogPreview() {
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
fun RecoveryCodeDialogPreview() {
    DashlanePreview {
        RecoveryCodeDialog(
            message = "Message",
            onInputRecoveryCode = {},
            onCancel = {},
        )
    }
}

@Preview
@Composable
fun SendTextMessageDialogPreview() {
    DashlanePreview {
        SendTextMessageDialog(
            onSendTextMessage = {},
            onCancel = {},
        )
    }
}