package com.dashlane.vpn.thirdparty.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.IntroScreenContent
import com.dashlane.vpn.thirdparty.R

@Composable
internal fun VpnActivateContent(
    onSetupVpnClick: () -> Unit,
) {
    IntroScreenContent(
        contentPadding = PaddingValues(16.dp),
        iconToken = IconTokens.featureVpnOutlined,
        title = stringResource(R.string.vpn_empty_state_title),
        items = listOf(
            IntroScreenContent.Item(
                icon = IconTokens.laptopCheckmarkOutlined,
                title = stringResource(R.string.vpn_empty_state_item_1_title),
                description = stringResource(R.string.vpn_empty_state_item_1_description),
            ),
            IntroScreenContent.Item(
                icon = IconTokens.webOutlined,
                title = stringResource(R.string.vpn_empty_state_item_2_title),
                description = stringResource(R.string.vpn_empty_state_item_2_description),
            ),
        ),
        action = IntroScreenContent.Action(
            text = stringResource(R.string.vpn_empty_state_set_up_vpn_button),
            onClick = onSetupVpnClick,
        ),
    )
}

@Preview
@Composable
private fun VpnActivateContentPreview() {
    DashlanePreview {
        VpnActivateContent(
            onSetupVpnClick = {},
        )
    }
}