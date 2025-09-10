package com.dashlane.ui.common.compose.components.basescreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.components.DashlaneTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoAppBarScreenWrapper(
    modifier: Modifier = Modifier,
    navigationAction: DashlaneTopAppBar.NavigationAction? = null,
    content: @Composable () -> Unit,
) {
    Scaffold(
        modifier = Modifier.scaffoldBackgroundAndInsets(),
        containerColor = DashlaneTheme.colors.backgroundAlternate,
        topBar = {
            DashlaneTopAppBar(
                text = "",
                navigationAction = navigationAction,
                colors = DashlaneTopAppBar.defaultColors.copy(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
            )
        },
    ) { contentPadding ->
        Box(modifier = modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Preview
@Composable
private fun NoAppBarScreenWrapperPreview() {
    DashlanePreview {
        NoAppBarScreenWrapper(
            navigationAction = DashlaneTopAppBar.NavigationAction(
                iconToken = IconTokens.arrowLeftOutlined,
                contentDescription = "Navigate up",
                onClick = {},
            ),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
            ) {
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "This is a Title",
                    style = DashlaneTheme.typography.titleSectionMedium,
                )
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = """
                        This is a description example. Really this text is quite long, and we hope the user will read it so they understand how the application works, so they don't open a ticket to user support nor give us a 1 star rating on the playstore.
                    """.trimIndent(),
                    style = DashlaneTheme.typography.bodyStandardRegular,
                )
            }
        }
    }
}