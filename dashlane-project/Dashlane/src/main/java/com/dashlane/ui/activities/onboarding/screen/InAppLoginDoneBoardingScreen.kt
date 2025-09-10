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
fun InAppLoginDoneBoardingScreen(primaryButtonClick: () -> Unit) {
    BoardingScreen(
        decoration = BoardingScreenDefaults.decoration(IconTokens.feedbackSuccessOutlined),
        title = stringResource(R.string.onboarding_in_app_login_done_title),
        description = stringResource(R.string.onboarding_in_app_login_done_subtitle),
        actions = BoardingScreenDefaults.Actions(
            primaryButtonText = stringResource(R.string.onboarding_in_app_login_done_positive_button),
            onPrimaryButtonClick = { primaryButtonClick() },
        ),
    )
}

@Preview
@Composable
private fun InAppLoginDoneBoardingScreenPreview() {
    DashlanePreview {
        InAppLoginDoneBoardingScreen {
            
        }
    }
}