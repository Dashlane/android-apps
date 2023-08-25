package com.dashlane.accountrecoverykey.activation.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity

@Composable
fun AccountRecoveryKeyActivationIntroScreen(
    modifier: Modifier = Modifier,
    onGenerateKeyClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_key),
                contentDescription = null,
            )
            Text(
                text = stringResource(id = R.string.account_recovery_key_activation_intro_title),
                style = DashlaneTheme.typography.titleSectionLarge,
                color = DashlaneTheme.colors.textNeutralCatchy,
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )
            Text(
                text = stringResource(id = R.string.account_recovery_key_activation_intro_description),
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard,
            )
        }
        ButtonMedium(
            modifier = Modifier
                .align(Alignment.End),
            onClick = onGenerateKeyClicked,
            intensity = Intensity.Catchy,
            layout = ButtonLayout.TextOnly(
                text = stringResource(id = R.string.account_recovery_key_activation_intro_button)
            )
        )
    }
}

@Preview
@Composable
fun AccountRecoveryKeyActivationIntroContentPreview() {
    DashlaneTheme { AccountRecoveryKeyActivationIntroScreen(onGenerateKeyClicked = {}) }
}
