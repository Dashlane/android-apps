package com.dashlane.wear.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.TextColor

enum class Emphasis(
    val contentColor: @Composable () -> TextColor,
    val backgroundColor: @Composable () -> Color,
) {
    High(
        contentColor = { DashlaneTheme.colors.textInverseCatchy },
        backgroundColor = { DashlaneTheme.colors.containerExpressiveBrandCatchyIdle },
    ),
    Medium(
        contentColor = { DashlaneTheme.colors.textInverseCatchy },
        backgroundColor = { DashlaneTheme.colors.containerExpressiveBrandQuietIdle },
    ),
    Low(
        contentColor = { DashlaneTheme.colors.textInverseCatchy },
        backgroundColor = { DashlaneTheme.colors.containerAgnosticInverseStandard },
    ),
    Danger(
        contentColor = { DashlaneTheme.colors.textInverseCatchy },
        backgroundColor = { DashlaneTheme.colors.containerExpressiveDangerCatchyIdle },
    ),
}