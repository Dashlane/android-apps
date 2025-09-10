package com.dashlane.ui.activities.onboarding.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreen
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreenDefaults

@Composable
fun DefaultInAppLoginBoardingScreen(
    onActivateAutofillClick: () -> Unit,
    onSkipClick: () -> Unit,
) {
    BoardingScreen(
        decoration = BoardingScreenDefaults.decoration(IconTokens.featureAutofillOutlined),
        title = stringResource(R.string.most_used_password_autologin_intro_title),
        description = stringResource(R.string.most_used_password_autologin_intro_text),
        actions = BoardingScreenDefaults.Actions(
            primaryButtonText = stringResource(R.string.most_used_password_autologin_positive_button),
            onPrimaryButtonClick = onActivateAutofillClick,
            secondaryButtonText = stringResource(R.string.most_used_password_autologin_intro_negative_button),
            onSecondaryButtonClick = onSkipClick,
        ),
    )
}

@Preview
@Composable
private fun DefaultInAppLoginBoardingScreenScreenPreview() {
    DashlanePreview {
        DefaultInAppLoginBoardingScreen(
            onActivateAutofillClick = {},
            onSkipClick = {},
        )
    }
}