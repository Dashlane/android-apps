package com.dashlane.ui.screens.fragments.userdata.sharing.center.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.BooleanProvider
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.IntroScreenContent

@Composable
internal fun SharingCenterEmptyContent(
    showAddLogin: Boolean,
    onAddLoginClick: () -> Unit,
) {
    IntroScreenContent(
        contentPadding = PaddingValues(16.dp),
        iconToken = IconTokens.sharedOutlined,
        title = stringResource(R.string.sharing_center_empty_screen_title),
        items = listOf(
            IntroScreenContent.Item(
                icon = IconTokens.actionShareOutlined,
                title = stringResource(R.string.sharing_center_empty_screen_item_1_title),
                description = stringResource(R.string.sharing_center_empty_screen_item_1_description),
            ),
        ),
        action = IntroScreenContent.Action(
            text = stringResource(R.string.sharing_center_empty_screen_add_login_button),
            onClick = onAddLoginClick,
        ).takeIf { showAddLogin },
    )
}

@Preview
@Composable
private fun SharingCenterEmptyContentPreview(
    @PreviewParameter(BooleanProvider::class) showAddLogin: Boolean,
) {
    DashlanePreview {
        SharingCenterEmptyContent(
            showAddLogin = showAddLogin,
            onAddLoginClick = {},
        )
    }
}