package com.dashlane.login.pages.email.compose

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.login.SsoInfo
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.component.TextField
import com.dashlane.design.component.tooling.TextFieldActions
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.login.LoginIntents
import com.dashlane.login.sso.LoginSsoActivity
import com.dashlane.ui.common.compose.components.CircularProgressIndicator
import com.dashlane.ui.widgets.compose.DashlaneLogo
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.util.isNotSemanticallyNull

@Composable
@Suppress("LongMethod")
fun LoginEmailScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginEmailViewModel,
    goToCreateAccount: (String, Boolean) -> Unit,
    goToAuthenticator: (AuthenticationSecondFactor.Totp, SsoInfo?) -> Unit,
    goToOTP: (AuthenticationSecondFactor.Totp, SsoInfo?) -> Unit,
    goToPassword: (RegisteredUserDevice, SsoInfo?) -> Unit,
    goToSecretTransfer: (String?, String) -> Unit,
    goToToken: (AuthenticationSecondFactor.EmailToken, SsoInfo?) -> Unit,
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
                is LoginEmailNavigationState.GoToAuthenticator -> goToAuthenticator(navigationState.secondFactor, navigationState.ssoInfo)
                is LoginEmailNavigationState.GoToCreateAccount -> goToCreateAccount(navigationState.email ?: "", navigationState.skipIfPrefilled)
                is LoginEmailNavigationState.GoToOTP -> goToOTP(navigationState.secondFactor, navigationState.ssoInfo)
                is LoginEmailNavigationState.GoToPassword -> goToPassword(navigationState.registeredUserDevice, navigationState.ssoInfo)
                is LoginEmailNavigationState.GoToSSO -> goToSSO.launch(Pair(navigationState.email ?: "", navigationState.ssoInfo))
                is LoginEmailNavigationState.GoToSecretTransfer -> goToSecretTransfer(navigationState.email ?: "", navigationState.destination)
                is LoginEmailNavigationState.GoToToken -> goToToken(navigationState.secondFactor, navigationState.ssoInfo)
            }
        }
    }

    LoginEmailContent(
        modifier = modifier,
        email = uiState.email ?: "",
        isLoading = uiState.isLoading,
        error = uiState.error?.toErrorMessage(),
        onEmailChange = viewModel::emailChanged,
        onClickContinue = viewModel::onContinue,
        onClickCreateAccount = viewModel::createAccount,
        onClickQrCode = viewModel::qrCode
    )

    if (uiState.showDebugConfirmationDialog) {
        DiagnosticConfirmationDialog(
            onConfirm = viewModel::diagnosticConfirmed,
            onCancel = viewModel::diagnosticCancelled
        )
    }

    if (uiState.showDebugUploadingDialog) {
        DiagnosticUploadingDialog(onCancel = viewModel::diagnosticCancelled)
    }

    if (uiState.showDebugSuccessDialog && uiState.crashDeviceId != null) {
        DiagnosticSuccessDialog(
            crashDeviceId = uiState.crashDeviceId!!,
            onCopy = viewModel::diagnosticCopy,
            onCancel = viewModel::diagnosticCancelled
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
    email: String,
    isLoading: Boolean = false,
    error: String?,
    onEmailChange: (String) -> Unit,
    onClickContinue: () -> Unit,
    onClickCreateAccount: () -> Unit,
    onClickQrCode: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val primaryButtonLayout = if (isLoading) {
        ButtonLayout.IndeterminateProgress
    } else {
        ButtonLayout.TextOnly(text = stringResource(id = R.string.login_email_page_validate_button))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, end = 24.dp)
    ) {
        DashlaneLogo(
            modifier = Modifier
                .padding(start = 24.dp),
            color = DashlaneTheme.colors.oddityBrand
        )
        Spacer(modifier = Modifier.weight(1f))
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .padding(start = 24.dp, top = 16.dp, bottom = 16.dp),
            value = email,
            onValueChange = onEmailChange,
            placeholder = stringResource(id = R.string.login_email_text_field_placeholder),
            label = stringResource(id = R.string.email_address_login_screen),
            isError = error.isNotSemanticallyNull(),
            feedbackText = error,
            actions = TextFieldActions.ClearField(stringResource(id = R.string.and_accessibility_action_text_clear)) {
                onEmailChange("")
                true
            },
            keyboardActions = KeyboardActions(
                onDone = { onClickContinue() }
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                autoCorrect = false,
            )
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
            layout = ButtonLayout.TextOnly(text = stringResource(id = R.string.login_email_page_secret_transfer_button))
        )
        Spacer(modifier = Modifier.weight(1f))
        ButtonMediumBar(
            modifier = Modifier.padding(start = 24.dp),
            primaryButtonLayout = primaryButtonLayout,
            secondaryButtonLayout = ButtonLayout.TextOnly(stringResource(id = R.string.create_account)),
            onPrimaryButtonClick = onClickContinue,
            onSecondaryButtonClick = onClickCreateAccount,
            isPrimaryButtonEnabled = email.isNotSemanticallyNull() && !isLoading
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun DiagnosticConfirmationDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
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
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
fun ContactAdminSsoErrorDialog(onCancel: () -> Unit) {
    Dialog(
        title = stringResource(id = R.string.general_error),
        description = { Text(text = stringResource(id = R.string.sso_contact_administrator_message)) },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.ok)),
        mainActionClick = onCancel,
        onDismissRequest = onCancel,
    )
}

@Composable
fun LoginEmailError.toErrorMessage(): String {
    return when (this) {
        LoginEmailError.Generic -> stringResource(id = R.string.login_button_error)
        LoginEmailError.InvalidEmail -> stringResource(id = R.string.invalid_email)
        LoginEmailError.Network -> stringResource(id = R.string.cannot_connect_to_server)
        LoginEmailError.NoAccount -> stringResource(id = R.string.account_doesn_t_exist_do_you_have_an_account_with_dashlane)
        LoginEmailError.Offline -> stringResource(id = R.string.offline)
        LoginEmailError.Team -> stringResource(id = R.string.login_team_error)
        LoginEmailError.SSO -> stringResource(id = R.string.sso_error_not_correct)
        LoginEmailError.UserDeactivated -> stringResource(id = R.string.login_email_error_user_deactivated)
    }
}

@Preview
@Composable
fun DiagnosticConfirmationDialogPreview() {
    DashlanePreview { DiagnosticConfirmationDialog(onConfirm = { }, onCancel = { }) }
}

@Preview
@Composable
fun DiagnosticUploadingDialogPreview() {
    DashlanePreview { DiagnosticUploadingDialog(onCancel = { }) }
}

@Preview
@Composable
fun DiagnosticSuccessDialogPreview() {
    DashlanePreview { DiagnosticSuccessDialog(crashDeviceId = "crashDeviceId", onCopy = {}, onCancel = { }) }
}

@Preview
@Composable
fun DiagnosticFailedDialogPreview() {
    DashlanePreview { DiagnosticFailedDialog(onCancel = { }) }
}

@Preview
@Composable
fun ContactAdminSsoErrorDialogPreview() {
    DashlanePreview { ContactAdminSsoErrorDialog(onCancel = { }) }
}

@Preview
@Composable
fun LoginEmailContentPreview() {
    DashlanePreview {
        LoginEmailContent(
            email = "randomemail@provider.com",
            error = "Error",
            onEmailChange = { },
            onClickContinue = { },
            onClickCreateAccount = { },
            onClickQrCode = { }
        )
    }
}

class SSOContract : ActivityResultContract<Pair<String, SsoInfo>, LoginSsoActivity.Result>() {
    override fun createIntent(context: Context, input: Pair<String, SsoInfo>): Intent {
        val email = input.first
        val ssoInfo = input.second
        return LoginIntents.createSsoLoginActivityIntent(
            context,
            login = email,
            serviceProviderUrl = ssoInfo.serviceProviderUrl,
            isSsoProvider = ssoInfo.isNitroProvider,
            migrateToMasterPasswordUser = ssoInfo.migration == SsoInfo.Migration.TO_MASTER_PASSWORD_USER
        )
    }

    override fun parseResult(resultCode: Int, intent: Intent?): LoginSsoActivity.Result {
        if (resultCode == Activity.RESULT_CANCELED) return LoginSsoActivity.Result.Error.Unknown

        return intent?.getParcelableExtraCompat<LoginSsoActivity.Result>(LoginSsoActivity.KEY_RESULT)
            ?: LoginSsoActivity.Result.Error.Unknown
    }
}
