package com.dashlane.ui.common.compose.components.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.theme.DashlaneTheme

object DashlaneTopAppBar {
    data class NavigationAction(
        val iconToken: IconToken,
        val contentDescription: String,
        val onClick: () -> Unit,
    )

    @OptIn(ExperimentalMaterial3Api::class)
    val defaultColors: TopAppBarColors
        @Composable
        get() = TopAppBarColors(
            containerColor = DashlaneTheme.colors.containerAgnosticNeutralStandard,
            titleContentColor = DashlaneTheme.colors.textNeutralCatchy.value,
            scrolledContainerColor = DashlaneTheme.colors.containerAgnosticNeutralStandard,
            navigationIconContentColor = DashlaneTheme.colors.textNeutralCatchy.value,
            actionIconContentColor = DashlaneTheme.colors.textNeutralStandard.value,
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashlaneTopAppBar(
    text: String,
    modifier: Modifier = Modifier,
    navigationAction: DashlaneTopAppBar.NavigationAction? = null,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = DashlaneTopAppBar.defaultColors,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    DashlaneTopAppBar(
        title = {
            Text(
                modifier = Modifier,
                text = text,
                color = LocalContentColor.current,
                style = DashlaneTheme.typography.titleSectionMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        modifier = modifier,
        navigationAction = navigationAction,
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashlaneTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationAction: DashlaneTopAppBar.NavigationAction? = null,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = DashlaneTopAppBar.defaultColors,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        title = {
            CompositionLocalProvider(
                LocalTextStyle provides DashlaneTheme.typography.titleSectionMedium,
            ) {
                title()
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (navigationAction != null) {
                IconButton(
                    colors = IconButtonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        contentColor = LocalContentColor.current,
                        disabledContentColor = DashlaneTheme.colors.textOddityDisabled.value,
                    ),
                    onClick = navigationAction.onClick,
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(navigationAction.iconToken.resource),
                        contentDescription = navigationAction.contentDescription,
                    )
                }
            }
        },
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = scrollBehavior,
    )
}