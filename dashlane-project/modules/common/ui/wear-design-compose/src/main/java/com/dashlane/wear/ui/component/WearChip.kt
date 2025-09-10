package com.dashlane.wear.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.wear.ui.DashlaneWearPreview

@Composable
fun WearChip(
    label: String,
    onClick: () -> Unit,
    emphasis: Emphasis,
    modifier: Modifier = Modifier,
    icon: @Composable (BoxScope.() -> Unit)? = null,
    secondaryLabel: String? = null,
    enabled: Boolean = true,
    labelMaxLines: Int = 1,
    contentMaxLines: Int = 1,
) {
    
    val labelAlignment = if (icon == null && secondaryLabel == null) {
        TextAlign.Center
    } else {
        TextAlign.Start
    }

    Chip(
        icon = icon,
        onClick = onClick,
        label = {
            WearText(
                modifier = Modifier.fillMaxWidth(),
                text = label,
                maxLines = labelMaxLines,
                overflow = TextOverflow.Ellipsis,
                style = DashlaneTheme.typography.bodyReducedRegular
                    .copy(fontWeight = FontWeight.Bold),
                textAlign = labelAlignment,
            )
        },
        secondaryLabel = secondaryLabel?.let {
            {
                WearText(
                    text = it,
                    maxLines = contentMaxLines,
                    overflow = TextOverflow.Ellipsis,
                    style = DashlaneTheme.typography.bodyReducedRegular,
                    color = DashlaneTheme.colors.textInverseQuiet,
                )
            }
        },
        modifier = modifier,
        colors = ChipDefaults.chipColors(
            contentColor = emphasis.contentColor().value,
            backgroundColor = emphasis.backgroundColor(),
        ),
        enabled = enabled,
    )
}

@Preview
@Composable
private fun WearChipPreview() {
    DashlaneWearPreview {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Emphasis.entries.forEach { emphasis ->
                WearChip(
                    icon = {
                        WearIcon(
                            token = IconTokens.passkeyOutlined,
                            contentDescription = "",
                        )
                    },
                    label = "Log in to Dashlane",
                    secondaryLabel = "Secondary label",
                    onClick = {},
                    emphasis = emphasis,
                )
            }
        }
    }
}

@Preview
@Composable
private fun WearChipSingleLabelPreview() {
    DashlaneWearPreview {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Emphasis.entries.forEach { emphasis ->
                WearChip(
                    label = "Log in to Dashlane",
                    onClick = {},
                    emphasis = emphasis,
                )
            }
        }
    }
}