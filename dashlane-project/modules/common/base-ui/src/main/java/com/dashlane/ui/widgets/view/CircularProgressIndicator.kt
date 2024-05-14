package com.dashlane.ui.widgets.view

import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.dashlane.design.theme.DashlaneTheme

@Composable
fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = DashlaneTheme.colors.textBrandQuiet.value,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth
) {
    androidx.compose.material3.CircularProgressIndicator(
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth
    )
}
