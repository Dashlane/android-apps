package com.dashlane.authenticator.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.dashlane.authenticator.R
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreen
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreenDefaults

@Composable
internal fun AuthenticatorBoardingScreen(
    credentialName: String?,
    onScanQrCodeClick: () -> Unit,
    onEnterSetupCodeClick: () -> Unit,
    onLinkClick: () -> Unit,
) {
    val title = if (credentialName != null) {
        stringResource(R.string.authenticator_intro_title, credentialName)
    } else {
        stringResource(R.string.authenticator_intro_title_unknown)
    }
    val showEnterSetupCode = credentialName != null
    BoardingScreen(
        decoration = BoardingScreenDefaults.decoration(iconToken = IconTokens.featureAuthenticatorOutlined),
        title = title,
        description = stringResource(R.string.authenticator_intro_body),
        actions = BoardingScreenDefaults.Actions(
            primaryButtonText = stringResource(R.string.authenticator_intro_positive_button),
            onPrimaryButtonClick = onScanQrCodeClick,
            secondaryButtonText = stringResource(R.string.authenticator_intro_neutral_button).takeIf { showEnterSetupCode },
            onSecondaryButtonClick = onEnterSetupCodeClick.takeIf { showEnterSetupCode },
        ),
    ) {
        LinkButton(
            text = stringResource(R.string.authenticator_intro_learn_more_link),
            destinationType = LinkButtonDestinationType.EXTERNAL,
            onClick = onLinkClick,
        )
    }
}

private class CredentialNameParameterProvider : PreviewParameterProvider<String?> {
    override val values: Sequence<String?>
        get() = sequenceOf("LinkedIn", null)
}

@Preview
@Composable
private fun AuthenticatorBoardingScreenPreview(
    @PreviewParameter(CredentialNameParameterProvider::class) credentialName: String?,
) {
    DashlanePreview {
        AuthenticatorBoardingScreen(
            credentialName = credentialName,
            onScanQrCodeClick = {},
            onEnterSetupCodeClick = {},
            onLinkClick = {},
        )
    }
}