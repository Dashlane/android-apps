package com.dashlane.ui.widgets.compose.system

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.dashlane.design.theme.DashlaneTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun StatusBarHandler(
    color: Color = DashlaneTheme.colors.containerAgnosticNeutralStandard
) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = color,
            darkIcons = useDarkIcons
        )
    }
}