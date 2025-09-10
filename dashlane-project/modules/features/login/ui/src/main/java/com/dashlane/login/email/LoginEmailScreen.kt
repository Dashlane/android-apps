package com.dashlane.login.email

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.data.login.AuthenticationSecondFactor
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.DashlaneLogoLockup
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.IndeterminateLoader
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.login.email.LoginEmailError.Generic
import com.dashlane.login.email.LoginEmailError.InvalidEmail
import com.dashlane.login.email.LoginEmailError.Network
import com.dashlane.login.email.LoginEmailError.NoAccount
import com.dashlane.login.email.LoginEmailError.Offline
import com.dashlane.login.email.LoginEmailError.SSO
import com.dashlane.login.email.LoginEmailError.Team
import com.dashlane.login.email.LoginEmailError.UserDeactivated
import com.dashlane.login.ui.R
import com.dashlane.sso.login.RemoteLoginSsoActivity.Result.Error.CannotOpenServiceProvider
import com.dashlane.sso.login.RemoteLoginSsoActivity.Result.Error.ForbiddenNavigation
import com.dashlane.sso.login.RemoteLoginSsoActivity.Result.Error.InvalidSso
import com.dashlane.sso.login.RemoteLoginSsoActivity.Result.Error.SamlResponseNotFound
import com.dashlane.sso.login.SSOContract
import com.dashlane.ui.common.compose.components.ContactAdminSsoErrorDialog
import com.dashlane.ui.common.compose.components.EmailTextField
import com.dashlane.util.isNotSemanticallyNull

@Composable
@Suppress("LongMethod")
fun LoginEmailScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginEmailViewModel,
    goToCreateAccount: (String, Boolean) -> Unit,
    goToOTP: (AuthenticationSecondFactor.Totp) -> Unit,
    goToPassword: () -> Unit,
    goToSecretTransfer: (String?, Boolean) -> Unit,
    goToToken: (AuthenticationSecondFactor.EmailToken) -> Unit,
    ssoSuccess: () -> Unit,
    endOfLife: () -> Unit,
) {
    LaunchedEffect(key1 = viewModel) {
        viewModel.viewStarted()
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val goToSSO = rememberLauncherForActivityResult(SSOContract(), onResult = viewModel::ssoComplete)

    LaunchedEffect(key1 = viewModel) {
        viewModel.viewStarted()
        viewModel.navigationState.collect { navigationState ->
            when (navigationState) {
                LoginEmailNavigationState.EndOfLife -> endOfLife()
                LoginEmailNavigationState.SSOSuccess -> ssoSuccess()
                is LoginEmailNavigationState.GoToCreateAccount -> goToCreateAccount(
                    navigationState.email ?: "",
                    navigationState.skipIfPrefilled,
                )
                is LoginEmailNavigationState.GoToOTP -> goToOTP(navigationState.secondFactor)
                is LoginEmailNavigationState.GoToPassword -> goToPassword()
                is LoginEmailNavigationState.GoToSSO -> goToSSO.launch(navigationState.ssoInfo)
                is LoginEmailNavigationState.GoToSecretTransfer -> goToSecretTransfer(
                    navigationState.email ?: "",
                    navigationState.showQrCode,
                )
                is LoginEmailNavigationState.GoToToken -> goToToken(navigationState.secondFactor)
            }
        }
    }

    LoginEmailContent(
        modifier = modifier,
        email = uiState.email ?: TextFieldValue(""),
        isLoading = uiState.isLoading,
        error = uiState.error?.toErrorMessage(),
        onEmailChange = viewModel::emailChanged,
        onClickContinue = viewModel::onContinue,
        onClickCreateAccount = viewModel::createAccount,
        onClickQrCode = viewModel::qrCode,
    )

    if (uiState.showDebugConfirmationDialog) {
        DiagnosticConfirmationDialog(
            onConfirm = viewModel::diagnosticConfirmed,
            onCancel = viewModel::diagnosticCancelled,
        )
    }

    if (uiState.showDebugUploadingDialog) {
        DiagnosticUploadingDialog(onCancel = viewModel::diagnosticCancelled)
    }

    if (uiState.showDebugSuccessDialog && uiState.crashDeviceId != null) {
        DiagnosticSuccessDialog(
            crashDeviceId = uiState.crashDeviceId!!,
            onCopy = viewModel::diagnosticCopy,
            onCancel = viewModel::diagnosticCancelled,
        )
    }

    if (uiState.showDebugFailedDialog) {
        DiagnosticFailedDialog(onCancel = viewModel::diagnosticCancelled)
    }

    if (uiState.isSSOAdminDialogShown) {
        ContactAdminSsoErrorDialog(onCancel = viewModel::ssoErrorDialogCancelled)
    }
}

@Composable
@Suppress("LongMethod")
fun LoginEmailContent(
    modifier: Modifier = Modifier,
    email: TextFieldValue,
    isLoading: Boolean = false,
    error: String?,
    onEmailChange: (TextFieldValue) -> Unit,
    onClickContinue: () -> Unit,
    onClickCreateAccount: () -> Unit,
    onClickQrCode: () -> Unit,
) {
    val primaryButtonLayout = if (isLoading) {
        ButtonLayout.IndeterminateProgress
    } else {
        ButtonLayout.TextOnly(text = stringResource(id = R.string.login_email_page_validate_button))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, end = 24.dp),
    ) {
        DashlaneLogoLockup(
            modifier = Modifier.padding(start = 24.dp),
            height = 40.dp,
        )
        Spacer(modifier = Modifier.weight(1f))
        EmailTextField(
            modifier = Modifier.padding(start = 24.dp),
            label = stringResource(id = R.string.email_address_login_screen),
            placeholder = stringResource(id = R.string.login_email_text_field_placeholder),
            email = email,
            error = error,
            onEmailChange = onEmailChange,
            onClickContinue = onClickContinue,
        )
        Text(
            modifier = Modifier
                .padding(start = 28.dp),
            text = stringResource(id = R.string.login_email_page_secret_transfer_label),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
        )
        ButtonMedium(
            modifier = Modifier
                .padding(bottom = 16.dp, start = 12.dp),
            onClick = onClickQrCode,
            intensity = Intensity.Supershy,
            layout = ButtonLayout.TextOnly(text = stringResource(id = R.string.login_email_page_secret_transfer_button)),
        )
        Spacer(modifier = Modifier.weight(1f))
        ButtonMediumBar(
            modifier = Modifier.padding(start = 24.dp),
            primaryButtonLayout = primaryButtonLayout,
            secondaryButtonLayout = ButtonLayout.TextOnly(stringResource(id = R.string.create_account)),
            onPrimaryButtonClick = onClickContinue,
            onSecondaryButtonClick = onClickCreateAccount,
            isPrimaryButtonEnabled = email.text.isNotSemanticallyNull() && !isLoading,
        )
    }
}

@Composable
fun DiagnosticConfirmationDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Dialog(
        title = stringResource(id = R.string.user_support_file_confirmation_description),
        description = { },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.ok)),
        mainActionClick = onConfirm,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.cancel)),
        additionalActionClick = onCancel,
        onDismissRequest = onCancel,
    )
}

@Composable
fun DiagnosticUploadingDialog(onCancel: () -> Unit) {
    Dialog(
        title = stringResource(id = R.string.user_support_file_upload_description),
        description = {
            Box(modifier = Modifier.fillMaxWidth()) {
                IndeterminateLoader(modifier = Modifier.align(Alignment.Center))
            }
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.cancel)),
        mainActionClick = onCancel,
        onDismissRequest = onCancel,
    )
}

@Composable
fun DiagnosticSuccessDialog(
    crashDeviceId: String,
    onCopy: () -> Unit,
    onCancel: () -> Unit,
) {
    Dialog(
        title = stringResource(id = R.string.user_support_file_finish_title),
        description = { Text(text = crashDeviceId) },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.user_support_file_copy)),
        mainActionClick = onCopy,
        onDismissRequest = onCancel,
    )
}

@Composable
fun DiagnosticFailedDialog(onCancel: () -> Unit) {
    Dialog(
        title = stringResource(id = R.string.user_support_file_fail_title),
        description = { },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.ok)),
        mainActionClick = onCancel,
        onDismissRequest = onCancel,
    )
}

@Composable
fun LoginEmailError.toErrorMessage(): String {
    return when (this) {
        Generic -> stringResource(id = R.string.login_button_error)
        InvalidEmail -> stringResource(id = R.string.invalid_email)
        Network -> stringResource(id = R.string.cannot_connect_to_server)
        NoAccount -> stringResource(id = R.string.account_doesn_t_exist_do_you_have_an_account_with_dashlane)
        Offline -> stringResource(id = R.string.offline)
        Team -> stringResource(id = R.string.login_team_error)
        is SSO -> {
            when (this.ssoError) {
                CannotOpenServiceProvider -> stringResource(id = R.string.sso_error_service_provider_not_found)
                ForbiddenNavigation -> stringResource(id = R.string.sso_error_forbidden_navigation)
                SamlResponseNotFound -> stringResource(id = R.string.sso_error_saml_not_found)
                InvalidSso -> stringResource(id = R.string.sso_error_not_correct)
                else -> stringResource(id = R.string.sso_error_not_correct)
            }
        }
        UserDeactivated -> stringResource(id = R.string.login_email_error_user_deactivated)
    }
}

@Preview
@Composable
private fun DiagnosticConfirmationDialogPreview() {
    DashlanePreview { DiagnosticConfirmationDialog(onConfirm = { }, onCancel = { }) }
}

@Preview
@Composable
private fun DiagnosticUploadingDialogPreview() {
    DashlanePreview { DiagnosticUploadingDialog(onCancel = { }) }
}

@Preview
@Composable
private fun DiagnosticSuccessDialogPreview() {
    DashlanePreview { DiagnosticSuccessDialog(crashDeviceId = "crashDeviceId", onCopy = {}, onCancel = { }) }
}

@Preview
@Composable
private fun DiagnosticFailedDialogPreview() {
    DashlanePreview { DiagnosticFailedDialog(onCancel = { }) }
}

@Preview
@Composable
private fun ContactAdminSsoErrorDialogPreview() {
    DashlanePreview { ContactAdminSsoErrorDialog(onCancel = { }) }
}

@Preview
@Composable
private fun LoginEmailContentPreview() {
    DashlanePreview {
        LoginEmailContent(
            email = TextFieldValue("randomemail@provider.com"),
            error = "Error",
            onEmailChange = { },
            onClickContinue = { },
            onClickCreateAccount = { },
            onClickQrCode = { },
        )
    }
}