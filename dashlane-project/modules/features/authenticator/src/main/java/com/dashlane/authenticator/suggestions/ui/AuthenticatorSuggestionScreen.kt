package com.dashlane.authenticator.suggestions.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.HasLogins.CredentialItem
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsViewModel
import com.dashlane.authenticator.suggestions.ui.AuthenticatorHas2FACompatibleLoginsDefaults.DEFAULT_ITEMS_SHOWN
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.screentemplate.BoxColumn

@Composable
fun AuthenticatorSuggestionScreen(
    viewModel: AuthenticatorSuggestionsViewModel,
    onAddLoginClick: (Boolean) -> Unit,
    onAdd2faTokenClick: () -> Unit,
    goToLearnMoreAbout2FA: () -> Unit,
    goToHelpCenter: () -> Unit,
    onSetupOtp: (CredentialItem) -> Unit,
    onSetupComplete: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        AuthenticatorSuggestionsUiState.Progress -> {
            ProgressContent(
                goToLearnMoreAbout2FA = goToLearnMoreAbout2FA,
                goToHelpCenter = goToHelpCenter,
            )
        }

        AuthenticatorSuggestionsUiState.NoLoginOnboarding -> {
            NoLoginOnboardingContent(
                onAddLoginClick = { onAddLoginClick(true) },
            )
        }

        AuthenticatorSuggestionsUiState.No2FACompatibleLogins -> {
            No2FACompatibleLoginsContent(
                onAddLoginClick = { onAddLoginClick(false) },
                onAdd2faTokenClick = onAdd2faTokenClick,
                goToLearnMoreAbout2FA = goToLearnMoreAbout2FA,
                goToHelpCenter = goToHelpCenter,
            )
        }

        is AuthenticatorSuggestionsUiState.HasLogins -> {
            Has2FACompatibleLoginsContent(
                state = state,
                onSetupOtp = onSetupOtp,
                onSeeAllClick = { viewModel.onSeeAll() },
                onSeeLessClick = { viewModel.onSeeLess() },
                onAdd2faTokenClick = onAdd2faTokenClick,
                goToLearnMoreAbout2FA = goToLearnMoreAbout2FA,
                goToHelpCenter = goToHelpCenter,
            )
        }

        AuthenticatorSuggestionsUiState.SetupComplete -> onSetupComplete()

        AuthenticatorSuggestionsUiState.AllSetup -> {
            AllSetupContent(
                onAddLoginClick = { onAddLoginClick(false) },
                goToLearnMoreAbout2FA = goToLearnMoreAbout2FA,
                goToHelpCenter = goToHelpCenter,
            )
        }
    }
}

@Composable
private fun ProgressContent(
    goToLearnMoreAbout2FA: () -> Unit,
    goToHelpCenter: () -> Unit,
) {
    BoxColumn(
        contentPadding = PaddingValues(16.dp),
    ) {
        AuthenticatorFaq(
            goToLearnMoreAbout2FA = goToLearnMoreAbout2FA,
            goToHelpCenter = goToHelpCenter,
        )
    }
}

@Preview
@Composable
private fun ProgressContentPreview() {
    DashlanePreview {
        ProgressContent(
            goToLearnMoreAbout2FA = {},
            goToHelpCenter = {},
        )
    }
}

@Composable
private fun NoLoginOnboardingContent(
    onAddLoginClick: () -> Unit,
) {
    AuthenticatorNoLoginOnboarding(
        onAddLoginClick = onAddLoginClick,
    )
}

@Preview
@Composable
private fun NoLoginOnboardingContentPreview() {
    DashlanePreview {
        NoLoginOnboardingContent(
            onAddLoginClick = {},
        )
    }
}

@Composable
private fun No2FACompatibleLoginsContent(
    onAddLoginClick: () -> Unit,
    onAdd2faTokenClick: () -> Unit,
    goToLearnMoreAbout2FA: () -> Unit,
    goToHelpCenter: () -> Unit,
) {
    BoxColumn(
        contentPadding = PaddingValues(16.dp),
    ) {
        AuthenticatorNo2FACompatibleLogins(
            onAddLoginClick = onAddLoginClick,
            onAdd2faTokenClick = onAdd2faTokenClick,
        )
        AuthenticatorFaq(
            modifier = Modifier.padding(top = 16.dp),
            goToLearnMoreAbout2FA = goToLearnMoreAbout2FA,
            goToHelpCenter = goToHelpCenter,
        )
    }
}

@Preview
@Composable
private fun No2FACompatibleLoginsContentPreview() {
    DashlanePreview {
        No2FACompatibleLoginsContent(
            onAddLoginClick = {},
            onAdd2faTokenClick = {},
            goToLearnMoreAbout2FA = {},
            goToHelpCenter = {},
        )
    }
}

@Composable
private fun Has2FACompatibleLoginsContent(
    state: AuthenticatorSuggestionsUiState.HasLogins,
    onSetupOtp: (CredentialItem) -> Unit,
    onSeeAllClick: () -> Unit,
    onSeeLessClick: () -> Unit,
    onAdd2faTokenClick: () -> Unit,
    goToLearnMoreAbout2FA: () -> Unit,
    goToHelpCenter: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .widthIn(max = 480.dp)
            .fillMaxHeight(),
        contentPadding = PaddingValues(16.dp),
    ) {
        authenticatorHas2FACompatibleLogins(
            logins = state.logins,
            maxItemsShown = state.nbItemsShown,
            onSetupOtp = onSetupOtp,
            onSeeAllClick = onSeeAllClick,
            onSeeLessClick = onSeeLessClick,
            onAdd2faTokenClick = onAdd2faTokenClick,
        )
        
        if (state.logins.size > state.nbItemsShown || state.logins.size <= DEFAULT_ITEMS_SHOWN) {
            item(key = "faq") {
                AuthenticatorFaq(
                    modifier = Modifier.padding(top = 16.dp),
                    goToLearnMoreAbout2FA = goToLearnMoreAbout2FA,
                    goToHelpCenter = goToHelpCenter,
                )
            }
        }
    }
}

@Preview
@Composable
private fun Has2FACompatibleLoginsContentPreview() {
    DashlanePreview {
        var nbItemsShown by remember { mutableIntStateOf(DEFAULT_ITEMS_SHOWN) }
        Has2FACompatibleLoginsContent(
            state = AuthenticatorSuggestionsUiState.HasLogins(
                logins = List(7) { i -> buildCredentialItemForPreview(i + 1) },
                nbItemsShown = nbItemsShown,
            ),
            onSetupOtp = {},
            onSeeAllClick = { nbItemsShown = 6 },
            onSeeLessClick = { nbItemsShown = DEFAULT_ITEMS_SHOWN },
            onAdd2faTokenClick = {},
            goToLearnMoreAbout2FA = {},
            goToHelpCenter = {},
        )
    }
}

@Composable
private fun AllSetupContent(
    onAddLoginClick: () -> Unit,
    goToLearnMoreAbout2FA: () -> Unit,
    goToHelpCenter: () -> Unit,
) {
    BoxColumn(
        contentPadding = PaddingValues(16.dp),
    ) {
        AuthenticatorAllSetup(
            onAddLoginClick = onAddLoginClick,
        )
        AuthenticatorFaq(
            modifier = Modifier.padding(top = 16.dp),
            goToLearnMoreAbout2FA = goToLearnMoreAbout2FA,
            goToHelpCenter = goToHelpCenter,
        )
    }
}

@Preview
@Composable
private fun AllSetupContentPreview() {
    DashlanePreview {
        AllSetupContent(
            onAddLoginClick = {},
            goToLearnMoreAbout2FA = {},
            goToHelpCenter = {},
        )
    }
}