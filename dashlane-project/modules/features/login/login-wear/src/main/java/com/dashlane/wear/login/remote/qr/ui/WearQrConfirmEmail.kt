package com.dashlane.wear.login.remote.qr.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.wear.login.R
import com.dashlane.wear.ui.DashlaneWearPreview
import com.dashlane.wear.ui.component.Emphasis
import com.dashlane.wear.ui.component.WearButton
import com.dashlane.wear.ui.component.WearText
import com.dashlane.wear.ui.screen.WearContentWrapper

@Composable
fun WearQrConfirmEmail(
    modifier: Modifier = Modifier,
    email: String,
    onCancel: () -> Unit,
    onAccept: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WearText(
                modifier = Modifier,
                textAlign = TextAlign.Center,
                text = stringResource(R.string.secret_transfer_login_as),
                style = DashlaneTheme.typography.bodyReducedRegular.copy(fontWeight = FontWeight.Bold),
            )
            WearText(
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center,
                text = email,
                style = DashlaneTheme.typography.bodyReducedRegular,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = 12.dp,
                alignment = Alignment.CenterHorizontally,
            ),
        ) {
            WearButton(
                token = IconTokens.actionCloseOutlined,
                onClick = onCancel,
                contentDescription = "",
                emphasis = Emphasis.Low,
            )
            WearButton(
                token = IconTokens.checkmarkOutlined,
                onClick = { onAccept(email) },
                contentDescription = "",
                emphasis = Emphasis.High,
            )
        }
    }
}

@WearPreviewSmallRound
@Composable
private fun WearQrConfirmEmailPreview() {
    DashlaneWearPreview {
        WearContentWrapper(
            isLogoVisible = true,
        ) { contentPadding ->
            WearQrConfirmEmail(
                modifier = Modifier.padding(
                    top = contentPadding.calculateTopPadding(),
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
                email = "randomemail@provider.com",
                onCancel = {},
                onAccept = {},
            )
        }
    }
}