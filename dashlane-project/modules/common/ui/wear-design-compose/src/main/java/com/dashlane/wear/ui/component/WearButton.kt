package com.dashlane.wear.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.wear.ui.DashlaneWearPreview

@Composable
fun WearButton(
    modifier: Modifier = Modifier,
    token: IconToken,
    onClick: () -> Unit,
    contentDescription: String,
    emphasis: Emphasis,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = emphasis.backgroundColor(),
        ),
    ) {
        WearIcon(
            modifier = Modifier.padding(8.dp),
            token = token,
            contentDescription = contentDescription,
            tint = emphasis.contentColor().value,
        )
    }
}

@Preview
@Composable
private fun WearButtonPreview() {
    DashlaneWearPreview {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Emphasis.entries.forEach { emphasis ->
                WearButton(
                    token = IconTokens.checkmarkOutlined,
                    onClick = {},
                    contentDescription = "",
                    emphasis = emphasis,
                )
            }
        }
    }
}