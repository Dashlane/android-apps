package com.dashlane.vpn.thirdparty.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreen
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreenDefaults
import com.dashlane.vpn.thirdparty.R

@Composable
internal fun DisabledVpnB2bScreen(
    onCloseClick: () -> Unit,
) {
    BoardingScreen(
        decoration = BoardingScreenDefaults.decoration(iconToken = IconTokens.featureVpnOutlined),
        title = stringResource(R.string.vpn_intro_title_b2b),
        description = stringResource(R.string.vpn_intro_body_b2b),
        actions = BoardingScreenDefaults.Actions(
            primaryButtonText = null,
            onPrimaryButtonClick = null,
            secondaryButtonText = stringResource(R.string.vpn_intro_button_close_b2b),
            onSecondaryButtonClick = onCloseClick,
        ),
    )
}

@Preview
@Composable
private fun DisabledVpnB2bScreenPreview() {
    DashlanePreview {
        DisabledVpnB2bScreen(
            onCloseClick = {},
        )
    }
}