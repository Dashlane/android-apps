package com.dashlane.disabletotp

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.activatetotp.R
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreen
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreenDefaults

@Composable
internal fun DisableTotpEnforcedIntroScreen(
    onDisableTotpClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    BoardingScreen(
        decoration = BoardingScreenDefaults.decoration(IconTokens.featureAuthenticatorOutlined),
        title = stringResource(R.string.disable_totp_enforced_intro_title),
        description = stringResource(R.string.disable_totp_enforced_intro_description),
        actions = BoardingScreenDefaults.Actions(
            primaryButtonText = stringResource(R.string.disable_totp_enforced_intro_cta_positive),
            onPrimaryButtonClick = onDisableTotpClick,
            secondaryButtonText = stringResource(R.string.disable_totp_enforced_intro_cta_negative),
            onSecondaryButtonClick = onCancelClick,
        ),
    )
}

@Preview
@Composable
private fun DisableTotpEnforcedIntroScreenPreview() {
    DashlanePreview {
        DisableTotpEnforcedIntroScreen(
            onDisableTotpClick = {},
            onCancelClick = {},
        )
    }
}