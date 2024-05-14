package com.dashlane.login.pages.secrettransfer.universal.passphrase

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.login.pages.secrettransfer.qrcode.DashlaneLogo
import com.dashlane.ui.widgets.compose.Passphrase
import com.dashlane.ui.widgets.compose.PassphraseLayout

@Composable
fun PassphraseIdentificationScreen(
    modifier: Modifier = Modifier,
    passphrase: List<Passphrase>
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        DashlaneLogo()
        Text(
            text = stringResource(id = R.string.login_universal_d2d_passphrase_identification_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(top = 72.dp, bottom = 16.dp)
        )

        Text(
            text = stringResource(id = R.string.login_universal_d2d_passphrase_identification_description),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(bottom = 24.dp)
        )

        PassphraseLayout(passphrase = passphrase, onValueChange = null, onKeyboardDone = null)
    }
}

@Preview
@Composable
fun PassphraseVerificationScreenPreview() {
    DashlanePreview {
        PassphraseIdentificationScreen(
            passphrase = listOf(
                Passphrase.Word("carrot"),
                Passphrase.Word("whales"),
                Passphrase.Word("potatoes"),
                Passphrase.Missing(value = "plant", userInput = "", isError = true),
                Passphrase.Word("mascara")
            )
        )
    }
}