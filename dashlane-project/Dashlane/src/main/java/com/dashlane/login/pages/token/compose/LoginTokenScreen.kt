package com.dashlane.login.pages.token.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.widgets.compose.CircularProgressIndicator
import com.dashlane.ui.widgets.compose.DashlaneLogo
import com.dashlane.ui.widgets.compose.OtpInput

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

    Column(
        modifier = modifier
            .padding(24.dp)
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
            text = stringResource(id = R.string.receive_sec_code),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(top = 48.dp)
        )
        OtpInput(
            modifier = Modifier.padding(top = 16.dp),
            onOtpComplete = onOtpComplete,
            otp = otp,
            isError = isTokenError,
            error = if (isTokenError) errorMessage else null
        )
        if (!isTokenError && errorMessage != null) {
            Text(
                modifier = Modifier.padding(top = 2.dp),
                text = errorMessage,
                color = DashlaneTheme.colors.textDangerQuiet,
                style = DashlaneTheme.typography.bodyHelperRegular
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }

    if (isLoading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
    }

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
