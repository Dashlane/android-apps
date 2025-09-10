package com.dashlane.wear.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.wear.ui.DashlaneWearPreview
import com.dashlane.wear.ui.component.WearText

@Composable
fun WearGenericLoadingScreen(
    modifier: Modifier = Modifier,
    text: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        text?.let {
            Spacer(modifier = Modifier.size(12.dp))
            WearText(
                text = it,
                style = DashlaneTheme.typography.bodyReducedRegular,
            )
        }
    }
}

@WearPreviewSmallRound
@Composable
private fun WearGenericLoadingScreenPreview() {
    DashlaneWearPreview {
        WearGenericLoadingScreen()
    }
}

@WearPreviewSmallRound
@Composable
private fun WearGenericLoadingWithMessageScreenPreview() {
    DashlaneWearPreview {
        WearGenericLoadingScreen(text = "Loading Passkeys")
    }
}