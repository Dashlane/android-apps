package com.dashlane.authenticator.suggestions.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.authenticator.R
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.IntroScreenContent

@Composable
internal fun AuthenticatorNoLoginOnboarding(
    modifier: Modifier = Modifier,
    onAddLoginClick: () -> Unit,
) {
    IntroScreenContent(
        modifier = modifier,
        iconToken = IconTokens.featureAuthenticatorOutlined,
        title = stringResource(R.string.authenticator_no_login_onboarding_title),
        items = listOf(
            IntroScreenContent.Item(
                icon = IconTokens.vaultOutlined,
                title = stringResource(R.string.authenticator_no_login_onboarding_item_1_title),
                description = stringResource(R.string.authenticator_no_login_onboarding_item_1_description),
            ),
            IntroScreenContent.Item(
                icon = IconTokens.protectionOutlined,
                title = stringResource(R.string.authenticator_no_login_onboarding_item_2_title),
                description = stringResource(R.string.authenticator_no_login_onboarding_item_2_description),
            ),
            IntroScreenContent.Item(
                icon = IconTokens.featurePasswordHealthOutlined,
                title = stringResource(R.string.authenticator_no_login_onboarding_item_3_title),
                description = stringResource(R.string.authenticator_no_login_onboarding_item_3_description),
            ),
        ),
        action = IntroScreenContent.Action(
            text = stringResource(R.string.authenticator_no_login_onboarding_add_login_button),
            onClick = onAddLoginClick,
        ),
    )
}

@Preview
@Composable
private fun AuthenticatorNoLoginOnboardingPreview() {
    DashlanePreview {
        AuthenticatorNoLoginOnboarding(
            onAddLoginClick = {},
        )
    }
}