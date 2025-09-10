package com.dashlane.ui.common.compose.components.basescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset

@Composable
fun Modifier.scaffoldBackgroundAndInsets(): Modifier {
    return this.then(
        Modifier
            .background(Color.Black)
            .imePadding()
            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)),
    )
}

@Composable
fun Modifier.centerWithTopOffset(offset: Dp): Modifier {
    val offsetPx = with(LocalDensity.current) { offset.roundToPx() }
    return this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight

        val childWidth = placeable.width
        val childHeight = placeable.height

        val centerX = (layoutWidth - childWidth) / 2
        val centerY = (layoutHeight - childHeight) / 2

        val topOffset = if (centerY >= offsetPx) {
            offsetPx
        } else {
            0
        }

        layout(layoutWidth, layoutHeight) {
            placeable.place(
                IntOffset(
                    x = centerX,
                    y = centerY - topOffset,
                ),
            )
        }
    }
}