package com.dashlane.login.pages.totp.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.widgets.compose.GenericCodeInputContent

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
        LoginTotpError.Network -> stringResource(id = R.string.cannot_connect_to_server)
        LoginTotpError.Offline -> stringResource(id = R.string.offline)
        null -> null
    }

    LoginTotpContent(
        modifier = modifier,
        login = login,
        isLoading = uiState is LoginTotpState.Loading,
        errorMessage = errorMessage,
        isTokenError = (uiState as? LoginTotpState.Error)?.error is LoginTotpError.InvalidToken,
        onOtpComplete = viewModel::onTokenCompleted,
    )
}

@Composable
fun LoginTotpContent(
    modifier: Modifier = Modifier,
    login: String,
    isLoading: Boolean,
    errorMessage: String?,
    isTokenError: Boolean,
    onOtpComplete: (String) -> Unit,
) {
    GenericCodeInputContent(
        modifier = modifier,
        text = stringResource(id = R.string.otp_enabled_token_desc),
        login = login,
        isLoading = isLoading,
        errorMessage = errorMessage,
        isTokenError = isTokenError,
        onOtpComplete = onOtpComplete,
    )
}

@Preview
@Composable
fun LoginTotpContentPreview() {
    DashlaneTheme(darkTheme = true) {
        LoginTotpContent(
            login = "randomemail@provider.com",
            onOtpComplete = {},
            isLoading = true,
            errorMessage = "Error",
            isTokenError = false
        )
    }
}