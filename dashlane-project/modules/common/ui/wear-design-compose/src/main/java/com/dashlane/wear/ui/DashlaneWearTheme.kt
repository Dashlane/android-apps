package com.dashlane.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dashlane.design.theme.DashlaneTheme

@Composable
fun DashlaneWearTheme(
    content: @Composable () -> Unit,
) {
    DashlaneTheme(darkTheme = true) {
        DashlaneTheme.colors
        content()
    }
}

val com.dashlane.design.theme.color.Colors.backgroundDefaultWear: Color
    get() = Color.Black

@Composable
fun DashlaneWearPreview(
    content: @Composable () -> Unit,
) {
    DashlaneWearTheme {
        Box(modifier = Modifier.background(color = Color.Black)) {
            content()
        }
    }
}