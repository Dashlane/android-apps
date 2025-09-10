package com.dashlane.authenticator.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.dashlane.authenticator.R
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.BooleanProvider
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreen
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreenDefaults

@Composable
fun AuthenticatorResultScreen(
    success: Boolean,
    credentialName: String?,
    onOkClick: () -> Unit,
    onRetryClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    val safeCredentialName = credentialName ?: stringResource(R.string.authenticator_default_account_name)
    if (success) {
        BoardingScreen(
            decoration = BoardingScreenDefaults.decoration(iconToken = IconTokens.feedbackSuccessOutlined),
            title = stringResource(R.string.authenticator_success_intro_title, safeCredentialName),
            description = stringResource(R.string.authenticator_success_intro_body),
            actions = BoardingScreenDefaults.Actions(
                primaryButtonText = stringResource(R.string.authenticator_success_intro_positive_button),
                onPrimaryButtonClick = onOkClick,
                secondaryButtonText = null,
                onSecondaryButtonClick = null,
            ),
        )
    } else {
        BoardingScreen(
            decoration = BoardingScreenDefaults.decoration(
                iconToken = IconTokens.feedbackFailOutlined,
                error = true,
            ),
            title = stringResource(R.string.authenticator_error_intro_title, safeCredentialName),
            description = stringResource(R.string.authenticator_error_intro_body),
            actions = BoardingScreenDefaults.Actions(
                primaryButtonText = stringResource(R.string.authenticator_error_intro_positive_button),
                onPrimaryButtonClick = onRetryClick,
                secondaryButtonText = stringResource(R.string.authenticator_error_intro_negative_button),
                onSecondaryButtonClick = onCancelClick,
            ),
        )
    }
}

@Preview
@Composable
private fun AuthenticatorResultIntroScreenPreview(
    @PreviewParameter(BooleanProvider::class) success: Boolean,
) {
    DashlanePreview {
        AuthenticatorResultScreen(
            success = success,
            credentialName = "LinkedIn",
            onOkClick = {},
            onRetryClick = {},
            onCancelClick = {},
        )
    }
}