package com.dashlane.wear.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.wear.ui.DashlaneWearPreview

@Composable
fun WearTextContentScreen(modifier: Modifier = Modifier, text: String) {
    WearContentWrapper(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = DashlaneTheme.colors.textNeutralCatchy.value,
                style = DashlaneTheme.typography.bodyStandardRegular,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@WearPreviewSmallRound
@Composable
private fun WearTextContentScreenPreview() {
    DashlaneWearPreview {
        WearTextContentScreen(text = "Loading your data")
    }
}