package com.dashlane.login.pages.authenticator.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.login.pages.secrettransfer.qrcode.DashlaneLogo
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.widgets.compose.IndeterminateLoading

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
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginDashlaneAuthenticatorState.Success -> {
                viewModel.hasNavigated()
                goToNext(state.registeredUserDevice, state.authTicket)
            }

            is LoginDashlaneAuthenticatorState.Canceled -> {
                viewModel.hasNavigated()
                cancel()
            }

            else -> Unit
        }
    }

    LoginDashlaneAuthenticatorContent(
        modifier = modifier,
        onClickUse2FACode = viewModel::useTOTP,
    )
}

@Composable
fun LoginDashlaneAuthenticatorContent(
    modifier: Modifier = Modifier,
    onClickUse2FACode: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        DashlaneLogo()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            IndeterminateLoading(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )
            Text(
                text = stringResource(id = R.string.login_dashlane_authenticator_message_in_progress),
                style = DashlaneTheme.typography.titleSectionLarge,
                textAlign = TextAlign.Center,
                color = DashlaneTheme.colors.textNeutralCatchy,
                modifier = Modifier
                    .padding(top = 24.dp)
            )
        }
        ButtonMedium(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            onClick = onClickUse2FACode,
            intensity = Intensity.Supershy,
            layout = ButtonLayout.TextOnly(
                text = stringResource(id = R.string.login_secret_transfer_totp_push_2fa_code_button)
            )
        )
    }
}

@Preview
@Composable
fun LoginAuthenticatorPushContentPreview() {
    DashlanePreview {
        LoginDashlaneAuthenticatorContent(onClickUse2FACode = {})
    }
}
