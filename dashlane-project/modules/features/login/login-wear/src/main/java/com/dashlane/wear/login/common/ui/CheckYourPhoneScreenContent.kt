package com.dashlane.wear.login.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.design.iconography.IconTokens
import com.dashlane.wear.ui.DashlaneWearPreview
import com.dashlane.wear.ui.component.WearIcon
import com.dashlane.wear.ui.component.WearText
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun CheckYourPhoneScreenContent(
    text: String,
    waitingTime: Duration,
    onDelayComplete: () -> Unit,
) {
    LaunchedEffect(key1 = waitingTime) {
        delay(waitingTime)
        onDelayComplete()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WearIcon(
                token = IconTokens.itemPhoneMobileOutlined,
                contentDescription = "",
            )
            WearText(
                text = text,
                textAlign = TextAlign.Center,
                maxLines = 5,
            )
        }
    }
}

@WearPreviewSmallRound
@Composable
private fun CheckYourPhoneScreenPreview() {
    DashlaneWearPreview {
        CheckYourPhoneScreenContent(
            text = "Check your phone",
            waitingTime = 5.seconds,
            onDelayComplete = { },
        )
    }
}