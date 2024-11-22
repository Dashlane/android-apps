package com.dashlane.secrettransfer.view.universal.passphrase

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.secrettransfer.R
import com.dashlane.ui.widgets.compose.Passphrase
import com.dashlane.ui.widgets.compose.PassphraseLayout

@Composable
fun PassphraseVerificationScreen(
    modifier: Modifier = Modifier,
    deviceName: String,
    passphrase: List<Passphrase>,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.secret_transfer_universal_passphrase_verification_screen_title, deviceName),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(top = 40.dp, bottom = 32.dp)
        )

        PassphraseLayout(passphrase = passphrase, onValueChange = onValueChange, onKeyboardDone = onConfirm)

        Spacer(modifier = Modifier.weight(1f))

        ButtonMedium(
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 32.dp),
            onClick = { onConfirm() },
            intensity = Intensity.Catchy,
            layout = ButtonLayout.TextOnly(
                text = stringResource(id = R.string.secret_transfer_universal_passphrase_verification_screen_button)
            )
        )
    }
}

@Preview
@Composable
private fun PassphraseVerificationScreenPreview() {
    DashlanePreview {
        PassphraseVerificationScreen(
            deviceName = "Device Name",
            passphrase = listOf(
                Passphrase.Word("carrot"),
                Passphrase.Word("whales"),
                Passphrase.Word("potatoes"),
                Passphrase.Missing(value = "plant", userInput = "", isError = true),
                Passphrase.Word("mascara")
            ),
            onValueChange = {},
            onConfirm = {}
        )
    }
}