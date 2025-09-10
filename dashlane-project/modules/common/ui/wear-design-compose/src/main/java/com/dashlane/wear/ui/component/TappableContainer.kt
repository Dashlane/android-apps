package com.dashlane.wear.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ripple
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.design.iconography.IconTokens
import com.dashlane.wear.ui.DashlaneWearPreview

@Composable
fun TappableContainer(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
            )
            .background(
                shape = RoundedCornerShape(8.dp),
                color = Color.Transparent,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@WearPreviewSmallRound
@Composable
private fun TappableContainerPreview() {
    DashlaneWearPreview {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TappableContainer(onClick = {}) {
                WearText("1")
            }
            TappableContainer(onClick = {}) {
                WearIcon(
                    token = IconTokens.actionAddOutlined,
                    contentDescription = "",
                    tint = Color.White,
                )
            }
        }
    }
}