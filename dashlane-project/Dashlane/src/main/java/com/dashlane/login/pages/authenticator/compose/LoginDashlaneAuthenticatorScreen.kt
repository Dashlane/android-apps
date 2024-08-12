package com.dashlane.login.pages.authenticator.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.widgets.compose.DashlaneLoading
import com.dashlane.ui.widgets.compose.DashlaneLogo

@Composable
fun LoginDashlaneAuthenticatorScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginDashlaneAuthenticatorViewModel,
    goToNext: (RegisteredUserDevice, String) -> Unit,
    cancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.viewStarted()
        viewModel.navigationState.collect { navigationState ->
            when (navigationState) {
                LoginDashlaneAuthenticatorNavigationState.Canceled -> cancel()
                is LoginDashlaneAuthenticatorNavigationState.Success -> goToNext(navigationState.registeredUserDevice, navigationState.authTicket)
            }
        }
    }

    LoginDashlaneAuthenticatorContent(
        modifier = modifier,
        email = uiState.email ?: "",
        isLoading = uiState.isLoading,
        isSuccess = uiState.isSuccess,
        error = uiState.error,
        onRetry = viewModel::retry,
        onClickUse2FACode = viewModel::useTOTP,
    )
}

@Composable
@Suppress("LongMethod")
fun LoginDashlaneAuthenticatorContent(
    modifier: Modifier = Modifier,
    email: String,
    isLoading: Boolean,
    isSuccess: Boolean,
    error: LoginDashlaneAuthenticatorError?,
    onRetry: () -> Unit,
    onClickUse2FACode: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        DashlaneLogo(color = DashlaneTheme.colors.oddityBrand)
        Text(
            text = email,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(top = 24.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        when {
            isLoading || isSuccess -> LoadingContent(isSuccess)
            error != null -> ErrorContent(errorMessage = error.toErrorMessage())
        }
        Spacer(modifier = Modifier.weight(1f))
        if (error != null) {
            ButtonMediumBar(
                primaryButtonLayout = ButtonLayout.TextOnly(stringResource(id = R.string.login_dashlane_authenticator_resend_request)),
                secondaryButtonLayout = ButtonLayout.TextOnly(stringResource(id = R.string.login_secret_transfer_totp_push_2fa_code_button)),
                onPrimaryButtonClick = onRetry,
                onSecondaryButtonClick = onClickUse2FACode
            )
        } else {
            ButtonMedium(
                modifier = Modifier.align(Alignment.End),
                onClick = onClickUse2FACode,
                intensity = Intensity.Quiet,
                layout = ButtonLayout.TextOnly(
                    text = stringResource(id = R.string.login_secret_transfer_totp_push_2fa_code_button)
                )
            )
        }
    }
}

@Composable
private fun LoadingContent(isSuccess: Boolean) {
    DashlaneLoading(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        hasFinishedLoading = isSuccess,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    Text(
        text = if (isSuccess) {
            stringResource(id = R.string.login_dashlane_authenticator_request_approved)
        } else {
            stringResource(id = R.string.login_dashlane_authenticator_message_in_progress)
        },
        style = DashlaneTheme.typography.titleSectionMedium,
        textAlign = TextAlign.Center,
        color = DashlaneTheme.colors.textNeutralCatchy,
        modifier = Modifier
            .padding(top = 24.dp)
    )
}

@Composable
private fun ColumnScope.ErrorContent(
    errorMessage: String,
) {
    Image(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        painter = painterResource(R.drawable.ic_error_state),
        colorFilter = ColorFilter.tint(DashlaneTheme.colors.textDangerQuiet.value),
        contentDescription = ""
    )
    Text(
        modifier = Modifier
            .padding(top = 24.dp)
            .align(Alignment.CenterHorizontally),
        text = errorMessage,
        style = DashlaneTheme.typography.titleSectionMedium,
        textAlign = TextAlign.Center,
        color = DashlaneTheme.colors.textNeutralCatchy,
    )
}

@Composable
private fun LoginDashlaneAuthenticatorError.toErrorMessage(): String {
    return when (this) {
        LoginDashlaneAuthenticatorError.ExpiredVersion -> stringResource(id = R.string.expired_version_noupdate_title)
        LoginDashlaneAuthenticatorError.Generic -> stringResource(id = R.string.login_dashlane_authenticator_request_rejected)
        LoginDashlaneAuthenticatorError.Timeout -> stringResource(id = R.string.login_dashlane_authenticator_request_timed_out)
    }
}

@Preview
@Composable
fun LoginAuthenticatorPushContentPreview() {
    DashlanePreview {
        LoginDashlaneAuthenticatorContent(
            isLoading = false,
            isSuccess = true,
            email = "randomemail@provider.com",
            error = null,
            onRetry = {},
            onClickUse2FACode = {}
        )
    }
}
