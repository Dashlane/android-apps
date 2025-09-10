package com.dashlane.wear.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.wear.ui.DashlaneWearPreview

@Composable
fun NumeratedTextList(
    steps: Array<String>,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
    ) {
        steps.forEachIndexed { index, step ->
            
            WearText(
                text = "${index + 1}. $step",
                maxLines = 3,
                style = DashlaneTheme.typography.bodyReducedRegular,
            )
        }
    }
}

@Composable
@WearPreviewSmallRound
private fun NumeratedTextListPreview() {
    DashlaneWearPreview {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            NumeratedTextList(
                modifier = Modifier.fillMaxWidth(),
                steps = arrayOf(
                    "Hello",
                    "How do you do?",
                    "Are your passwords secure?",
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            )
        }
    }
}