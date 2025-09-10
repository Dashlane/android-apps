package com.dashlane.wear.login.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.wear.login.R
import com.dashlane.wear.ui.DashlaneWearPreview
import com.dashlane.wear.ui.component.Emphasis
import com.dashlane.wear.ui.component.WearButton
import com.dashlane.wear.ui.component.WearIcon
import com.dashlane.wear.ui.component.WearText
import com.dashlane.wear.ui.screen.WearContentWrapper

@Composable
internal fun WearLoginErrorContent(
    modifier: Modifier = Modifier,
    errorText: String,
    onButtonClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WearIcon(
                token = IconTokens.feedbackWarningFilled,
                tint = DashlaneTheme.colors.containerExpressiveDangerCatchyIdle,
                contentDescription = stringResource(R.string.error),
            )
            Spacer(modifier = Modifier.size(4.dp))
            WearText(
                textAlign = TextAlign.Center,
                text = errorText,
                style = DashlaneTheme.typography.bodyReducedRegular,
                maxLines = 5,
            )
        }

        WearButton(
            token = IconTokens.actionCloseOutlined,
            onClick = onButtonClick,
            emphasis = Emphasis.Low,
            contentDescription = stringResource(R.string.and_accessibility_close),
        )
    }
}

@WearPreviewSmallRound
@Composable
private fun WearLoginErrorContentPreview() {
    DashlaneWearPreview {
        WearContentWrapper { contentPadding ->
            WearLoginErrorContent(
                modifier = Modifier.padding(
                    top = contentPadding.calculateTopPadding(),
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
                errorText = stringResource(R.string.secret_transfer_error),
                onButtonClick = {},
            )
        }
    }
}