package com.dashlane.wear.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Text
import com.dashlane.wear.ui.DashlaneWearPreview

@Composable
fun WearCompactChip(
    text: String,
    onClick: () -> Unit,
    emphasis: Emphasis,
    modifier: Modifier = Modifier,
) {
    CompactChip(
        onClick = onClick,
        label = { Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        modifier = modifier,
        colors = ChipDefaults.chipColors(
            contentColor = emphasis.contentColor().value,
            backgroundColor = emphasis.backgroundColor(),
        ),
    )
}

@Preview
@Composable
private fun WearCompactChipPreview() {
    DashlaneWearPreview {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Emphasis.entries.forEach { emphasis ->
                WearCompactChip(
                    text = "Log in to Dashlane",
                    emphasis = emphasis,
                    onClick = {},
                )
            }
        }
    }
}