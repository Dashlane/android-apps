package com.dashlane.ui.common.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dashlane.design.component.Icon
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.common.compose.components.components.DashlaneTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarScaffold(
    topBarState: TopBarState,
    back: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        containerColor = DashlaneTheme.colors.backgroundAlternate,
        topBar = {
            AnimatedVisibility(
                visible = topBarState.visible,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
                content = {
                    DashlaneTopAppBar(
                        navigationIcon = {
                            if (topBarState.backEnabled) {
                                IconButton(onClick = back) {
                                    Icon(
                                        token = IconTokens.arrowLeftOutlined,
                                        contentDescription = null,
                                        tint = DashlaneTheme.colors.textNeutralCatchy
                                    )
                                }
                            }
                        },
                        text = topBarState.title,
                    )
                }
            )
        },
        content = content
    )
}