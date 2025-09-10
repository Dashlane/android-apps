package com.dashlane.wear.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.wear.ui.DashlaneWearPreview

@Composable
fun WearTitleCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emphasis: Emphasis = Emphasis.Low,
    onClick: () -> Unit = {},
) {
    TitleCard(
        modifier = modifier,
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = DashlaneTheme.typography.bodyReducedRegular
                    .copy(fontWeight = FontWeight.Bold),
            )
        },
        content = {
            Text(
                text = content,
                maxLines = 10,
                overflow = TextOverflow.Ellipsis,
                style = DashlaneTheme.typography.bodyReducedRegular,
                color = DashlaneTheme.colors.textInverseQuiet.value,
            )
        },
        enabled = enabled,
        onClick = onClick,
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = emphasis.backgroundColor(),
            endBackgroundColor = emphasis.backgroundColor(),
        ),
    )
}

@Composable
@WearPreviewSmallRound
private fun WearTitleCardPreview() {
    DashlaneWearPreview {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            WearTitleCard(
                title = "Title",
                content = "This is an informative card for Wear apps",
                onClick = {},
            )
        }
    }
}