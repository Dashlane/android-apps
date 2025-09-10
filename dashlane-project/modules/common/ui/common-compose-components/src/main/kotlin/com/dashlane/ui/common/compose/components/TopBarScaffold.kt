package com.dashlane.ui.common.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.common.compose.components.basescreen.scaffoldBackgroundAndInsets
import com.dashlane.ui.common.compose.components.components.DashlaneTopAppBar
import com.dashlane.ui.commoncomposecomponents.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarScaffold(
    topBarState: TopBarState,
    back: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = Modifier.scaffoldBackgroundAndInsets(),
        containerColor = DashlaneTheme.colors.backgroundAlternate,
        topBar = {
            AnimatedVisibility(
                visible = topBarState.visible,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
                content = {
                    DashlaneTopAppBar(
                        navigationAction = if (topBarState.backEnabled) {
                            DashlaneTopAppBar.NavigationAction(
                                iconToken = IconTokens.arrowLeftOutlined,
                                contentDescription = stringResource(R.string.and_accessibility_back_button),
                                onClick = back,
                            )
                        } else {
                            null
                        },
                        text = topBarState.title,
                    )
                },
            )
        },
        content = content,
    )
}