package com.dashlane.login.pages.token.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.widgets.compose.GenericCodeInputContent

@Composable
fun LoginTokenScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginTokenViewModel,
    login: String,
    goToNext: (RegisteredUserDevice.Remote, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginTokenState.Success -> {
                viewModel.hasNavigated()
                goToNext(state.registeredUserDevice, state.authTicket)
            }

            else -> Unit
        }
    }

    val errorMessage: String? = when ((uiState as? LoginTokenState.Error)?.error) {
        LoginTokenError.InvalidToken -> stringResource(id = R.string.token_failed_resend_or_try_later)
        LoginTokenError.Network -> stringResource(id = R.string.cannot_connect_to_server)
        LoginTokenError.Offline -> stringResource(id = R.string.offline)
        null -> null
    }

    LoginTotpContent(
        modifier = modifier,
        login = login,
        otp = (uiState as? LoginTokenState.DebugToken)?.data?.token,
        isLoading = uiState is LoginTokenState.Loading,
        errorMessage = errorMessage,
        isTokenError = (uiState as? LoginTokenState.Error)?.error is LoginTokenError.InvalidToken,
        onOtpComplete = viewModel::onTokenCompleted,
        onDialogConfirmClick = viewModel::onHelpClicked
    )
}

@Suppress("LongMethod")
@Composable
fun LoginTotpContent(
    modifier: Modifier = Modifier,
    login: String,
    otp: String? = null,
    isLoading: Boolean,
    errorMessage: String?,
    isTokenError: Boolean,
    onOtpComplete: (String) -> Unit,
    onDialogConfirmClick: () -> Unit,
) {
    var isHelpDialogDisplayed by rememberSaveable { mutableStateOf(false) }

    GenericCodeInputContent(
        modifier = modifier,
        text = stringResource(id = R.string.receive_sec_code),
        login = login,
        otp = otp,
        isLoading = isLoading,
        errorMessage = errorMessage,
        isTokenError = isTokenError,
        onOtpComplete = onOtpComplete
    )

    if (isHelpDialogDisplayed) {
        EmailCodeHelpAlertDialog(
            confirmButtonClick = onDialogConfirmClick,
            dismissButtonClick = { isHelpDialogDisplayed = false }
        )
    }
}

@Composable
fun EmailCodeHelpAlertDialog(
    confirmButtonClick: () -> Unit,
    dismissButtonClick: () -> Unit
) {
    Dialog(
        title = stringResource(id = R.string.login_token_where_is_popup_title),
        description = {
            Text(text = stringResource(id = R.string.login_token_where_is_popup_message),)
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.login_token_where_is_popup_resend)),
        mainActionClick = confirmButtonClick,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.close)),
        additionalActionClick = dismissButtonClick,
        onDismissRequest = dismissButtonClick
    )
}

@Preview
@Composable
fun LoginTotpContentPreview() {
    DashlanePreview {
        LoginTotpContent(
            login = "randomemail@provider.com",
            isLoading = true,
            onOtpComplete = {},
            onDialogConfirmClick = {},
            errorMessage = "Error",
            isTokenError = false
        )
    }
}

@Preview
@Composable
fun EmailCodeHelpAlertDialogPreview() {
    DashlaneTheme {
        EmailCodeHelpAlertDialog(
            confirmButtonClick = { },
            dismissButtonClick = {}
        )
    }
}
