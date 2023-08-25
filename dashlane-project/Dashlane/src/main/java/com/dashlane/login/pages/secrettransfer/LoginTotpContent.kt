package com.dashlane.login.pages.secrettransfer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.ui.widgets.compose.DashlaneLoading
import com.dashlane.ui.widgets.compose.OtpInput

@Composable
fun LoginTotpContent(
    modifier: Modifier = Modifier,
    onOtpComplete: (String) -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        DashlaneLogo()
        Text(
            text = stringResource(id = R.string.login_secret_transfer_totp_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(top = 48.dp)
        )
        OtpInput(
            modifier = Modifier.padding(top = 32.dp),
            onOtpComplete = onOtpComplete,
            isError = false
        )
    }
}

@Composable
fun LoginAuthenticatorPushContent(
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
            DashlaneLoading(
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
fun LoginTotpContentPreview() {
    DashlaneTheme(darkTheme = true) {
        LoginTotpContent(onOtpComplete = {})
    }
}

@Preview
@Composable
fun LoginAuthenticatorPushContentPreview() {
    DashlaneTheme(darkTheme = true) {
        LoginAuthenticatorPushContent(onClickUse2FACode = {})
    }
}
