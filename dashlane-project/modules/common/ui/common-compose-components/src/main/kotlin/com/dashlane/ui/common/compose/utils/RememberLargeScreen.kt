package com.dashlane.ui.common.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

private const val LARGE_SCREEN_MIN_WIDTH = 600

@Composable
fun <T> rememberLargeScreen(block: ((Boolean) -> T)): T {
    val configuration = LocalConfiguration.current
    return remember(configuration.screenWidthDp) {
        block(configuration.screenWidthDp > LARGE_SCREEN_MIN_WIDTH)
    }
}
