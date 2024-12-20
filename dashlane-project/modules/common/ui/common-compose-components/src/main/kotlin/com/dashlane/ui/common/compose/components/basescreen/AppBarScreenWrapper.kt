package com.dashlane.ui.common.compose.components.basescreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Icon
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.components.DashlaneTopAppBar
import com.dashlane.ui.common.compose.components.components.StatusBarHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarScreenWrapper(
    titleText: String,
    navigationIconToken: IconToken? = null,
    onNavigationClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    StatusBarHandler()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        DashlaneTopAppBar(
            text = titleText,
            navigationIcon = {
                if (navigationIconToken != null) {
                    IconButton(onClick = onNavigationClick) {
                        Icon(
                            token = navigationIconToken,
                            contentDescription = null,
                            tint = DashlaneTheme.colors.textNeutralCatchy
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = DashlaneTheme.colors.containerAgnosticNeutralStandard,
                titleContentColor = DashlaneTheme.colors.textNeutralCatchy.value
            )
        )

        content()
    }
}

@Preview
@Composable
private fun AppBarScreenWrapperPreview() {
    DashlanePreview {
        AppBarScreenWrapper(
            titleText = "App bar Text",
            navigationIconToken = IconTokens.menuOutlined,
            onNavigationClick = {}
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "This is a Title",
                    style = DashlaneTheme.typography.titleSectionMedium
                )
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "This is a description example. Really this text is quite long, and we hope the user will read it so they understand how the application works, so they don't open a ticket to user support nor give us a 1 star rating on the playstore.",
                    style = DashlaneTheme.typography.bodyStandardRegular
                )
            }
        }
    }
}