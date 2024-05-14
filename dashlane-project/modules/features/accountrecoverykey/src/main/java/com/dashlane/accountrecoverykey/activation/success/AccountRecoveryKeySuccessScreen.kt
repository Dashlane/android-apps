package com.dashlane.accountrecoverykey.activation.success

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.dashlane.accountrecoverykey.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun AccountRecoveryKeySuccessScreen(
    modifier: Modifier = Modifier,
    done: () -> Unit
) {
    BackHandler(enabled = true) {
        done()
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading_success))

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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LottieAnimation(
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp),
                composition = composition
            )
            Text(
                text = stringResource(id = R.string.account_recovery_key_success_title),
                style = DashlaneTheme.typography.titleSectionLarge,
                color = DashlaneTheme.colors.textNeutralCatchy,
                textAlign = TextAlign.Center,
                modifier = modifier
                    .padding(bottom = 32.dp)
                    .testTag("accountRecoveryKeySuccessTitle")
            )
        }
        ButtonMedium(
            modifier = Modifier
                .align(Alignment.End),
            onClick = done,
            intensity = Intensity.Catchy,
            layout = ButtonLayout.TextOnly(
                text = stringResource(id = R.string.account_recovery_key_success_button)
            )
        )
    }
}

@Preview
@Composable
fun AccountRecoveryKeySuccessContentPreview() {
    DashlanePreview { AccountRecoveryKeySuccessScreen(done = {}) }
}
